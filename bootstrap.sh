# Shanoir NG - Import, manage and share neuroimaging data
# Copyright (C) 2009-2019 Inria - https://www.inria.fr/
# Contact us on https://project.inria.fr/shanoir/
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

#!/bin/sh

print_help()
{
	cat <<EOF
Build and deploy Shanoir
usage:
	$0 --clean|--force|--no-deploy [--no-build] [--no-keycloak] [--no-dcm4chee] [--native] [-h|--help]

CAUTION: THIS COMMAND IS DESTRUCTIVE, do not use it on an existing production
instance. It will overwrite the data hosted in the external volumes declared in
docker compose.yml (note: as a safety precaution, the command will fail if
'--clean' or '--force' option is not used).

Options:
--clean		perform a clean deployment (will run 'docker compose down -v' to destroy all existing volumes)
--force		force deploying over the existing volumes (might be a little faster, use it in dev only)
--no-deploy	skip the deployment stage

--no-build	skip the build stage
--no-keycloak do not run Keycloak (used if Keycloak is external)
--no-dcm4chee do not run dcm4chee (used if dcm4chee is external)
--native	build & run the microservices as a native image (paketo/buildpacks),
		via the docker-compose-dev-native.yml overlay, instead of the regular JVM jar.
		The regular (non-native) JVM images are still built and used once
		to run the SHANOIR_MIGRATION=init schema-bootstrap step, since the
		native image has no /bin/entrypoint wrapper to do that itself;
-h|--help	print this help

EOF
	exit 0
}

wait_tcp_ready()
{
	local container="$1"
	local tcp_port="$2"

	docker compose exec -T "$container" bash -c "
	(	set -e
		while true; do 
			if true < '/dev/tcp/localhost/$tcp_port' ; then
				echo 'connected to $container port $tcp_port'
				exit 0
			fi
			sleep 1
		done
	) 2>/dev/null
	"
}

die()
{
	echo "error: $*" >&2
	exit 1
}

step()
{
	echo "======== $* ========"
}


set -e

build=1
deploy=1
keycloak=1
dcm4chee=1
clean=
force=
native=
while [ $# -ne 0 ] ; do
	case "$1" in
		-h|--help)	print_help	;;
		--clean)	clean=1		;;
		--force)	force=1		;;
		--no-build)	build=		;;
		--no-keycloak)	keycloak=		;;
		--no-dcm4chee)	dcm4chee=		;;
		--no-deploy)	deploy=		;;
		--native)	native=1	;;
		*)		die "unknown option '$1'"
	esac
	shift
done

if [ -z "$clean$force" ] && [ -n "$deploy" ] ; then
	die "you must provide at least --clean, --force or --no-deploy"
fi

# Compose file sets used for the 'docker compose' invocations below.
# - compose_files_base never includes the native overlay: it always resolves
#   to the regular (buildable, entrypoint-wrapper-having) JVM image.
# - compose_files layers docker-compose-dev-native.yml on top when --native is
#   given.
compose_files_base="-f docker-compose-dev.yml"
compose_files="$compose_files_base"
if [ -n "$native" ] ; then
	compose_files="$compose_files -f docker-compose-dev-native.yml"
fi

if [ -n "$build" ] ; then
	#
	# Build stage
	#
	step "build shanoir"

	# 1. build a docker image with the java toolchain
	DEV_IMG=shanoir-ng-dev
	docker build -t "$DEV_IMG" --target=jdk docker-compose

	mkdir -p /tmp/home
	docker run --rm -t -i -v "$PWD:/src" -u "`id -u`:`id -g`" -e HOME="/src/tmp/home" \
		-e MAVEN_OPTS="-Dmaven.repo.local=/src/tmp/home/.m2/repository"	\
		-w /src "$DEV_IMG" sh -c 'cd shanoir-ng-parent && mvn clean install -DskipTests'

	# 3. build the native images (paketo/buildpacks), if requested
	#
	# spring-boot:build-image talks to the *host* docker daemon (it drives
	# buildpacks builds through docker itself), so the socket is mounted into
	# the build container. Depending on your local docker setup this may need
	# root inside the container rather than "-u `id -u`:`id -g`" to have
	# permission to talk to the socket -- adjust if you hit a permission
	# denied error here.
	if [ -n "$native" ] ; then
		step "build shanoir (native: users)"
		docker run --rm -t -i -v "$PWD:/src" -v /var/run/docker.sock:/var/run/docker.sock \
			-e HOME="/src/tmp/home" \
			-e MAVEN_OPTS="-Dmaven.repo.local=/src/tmp/home/.m2/repository" \
			-w /src "$DEV_IMG" sh -c \
			'cd shanoir-ng-parent && mvn -pl shanoir-ng-users -Pnative spring-boot:build-image -DskipTests'
	fi

	# 4. build the (remaining) docker images
	#
	# When --native is set, the merged config points 'users' at the
	# natively-built image rather than a build context, so `docker compose
	# build` has nothing to do for it there -- it's built separately below.
	# We additionally still build the regular (non-native) JVM images
	# via compose_files_base, because it's needed to run the
	# SHANOIR_MIGRATION=init oneshot step (see the deploy loop below): the
	# native image has no /bin/entrypoint wrapper to do that itself.
	step "build docker images"
	if [ -n "$native" ] ; then
		other_services="`docker compose $compose_files config --services | grep -v '^users$'`"
		docker compose $compose_files build $other_services
		docker compose $compose_files_base build users
	else
		docker compose $compose_files build
	fi
fi
if [ -n "$deploy" ] ; then
	#
	# Clean stage
	#
	if [ -n "$clean" ] ; then
		# full clean (--clean)
		# -> destroy all external volumes
		step "clean"
		docker compose $compose_files down -v
	else
		# overwrite (--force)
		# -> just remove all existing containers
		# 
		# Note: we must ensure that all containers are removed because:
		# - 'docker compose run' should not be used when the
		#   corresponding service is up
		# - 'docker compose logs' may display old logs if the container
		#   is not destroyed
		step "stop shanoir"
		docker compose $compose_files down
	fi

	#
	# Deploy stage
	#

	# 1. database
	step "init: database"
	docker compose $compose_files up -d database
	wait_tcp_ready database 3306

	# 2. keycloak-database + keycloak + init-cert-and-logs
	if [ -n "$keycloak" ] ; then
		step "init: keycloak-database"
		docker compose $compose_files up -d keycloak-database
		wait_tcp_ready keycloak-database 3306
		
		step "init: keycloak"
		docker compose $compose_files run --rm -e SHANOIR_MIGRATION=init keycloak

		step "start: keycloak"
		docker compose $compose_files up -d keycloak
		docker-compose/common/oneshot --pgrp '\| *'				\
				' INFO  \[io.quarkus\] .* Keycloak .* started in [0-9]*'	\
				-- docker compose logs --no-color --follow keycloak >/dev/null

		step "start and stop: init-cert-and-logs"
		docker compose $compose_files up -d init-cert-and-logs
	fi

	# 3. infrastructure services: dcm4chee
	if [ -n "$dcm4chee" ] ; then
		step "start: infrastructure services: dcm4chee"
		for infra_ms_dcm4chee in ldap dcm4chee-database dcm4chee-arc
		do
			step "start: $infra_ms_dcm4chee infrastructure microservices dcm4chee"
			docker compose $compose_files up -d "$infra_ms_dcm4chee"
		done
	fi
	
	# 4. infrastructure services
	step "start: infrastructure services"
	for infra_ms in rabbitmq solr
	do
		step "start: $infra_ms infrastructure microservice"
		docker compose $compose_files up -d "$infra_ms"
	done
	
	# 5. Shanoir microservices
	step "start: shanoir microservices"
	for ms in users studies datasets import preclinical nifti-conversion
	do
		# The native 'users' image has no /bin/entrypoint wrapper (it's built
		# from a paketo runtime base, not our base-microservice image), so it
		# can't run the SHANOIR_MIGRATION=init oneshot dance itself. Instead,
		# run init against the regular (non-native) JVM image first -- this
		# creates the schema on a fresh volume and is a no-op if it already
		# exists -- then start the actual 'users' service as the native image.
		if [ -n "$native" ] && [ "$ms" = "users" ] ; then
			step "init: $ms microservice (via non-native image)"
			docker compose $compose_files_base run --rm -e SHANOIR_MIGRATION=init "$ms"
			step "start: $ms microservice (native)"
			docker compose $compose_files up -d "$ms"
			continue
		fi

		step "init: $ms microservice"
		docker compose $compose_files run --rm -e SHANOIR_MIGRATION=init "$ms"
		step "start: $ms microservice"
		docker compose $compose_files up -d "$ms"
	done

	# 6. nginx
	step "start: nginx"
	docker compose $compose_files up -d nginx
fi
