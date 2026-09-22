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
--no-infra do not run infra-service (solr, rabbitmq, bids-validator)
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
infra=1
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
		--no-infra)  infra=		;;
		--no-deploy)	deploy=		;;
		--native)	native=1	;;
		*)		die "unknown option '$1'"
	esac
	shift
done

if [ -z "$clean$force" ] && [ -n "$deploy" ] ; then
	die "you must provide at least --clean, --force or --no-deploy"
fi

if [ -n "$build" ] ; then
	#
	# Build stage
	#
	step "Build Shanoir Maven projects"

  # Attention: process-aot requires the Spring configuration
  # already during Maven build-time, as it fixes/defines it
  build_sql_init_mode=
	case "${SHANOIR_MIGRATION:-dev}" in
		dev|init)	build_sql_init_mode=always ;;
	esac

	# Maven build: within a Docker image, with the java toolchain
	DEV_IMG=shanoir-ng-dev
	docker build -t "$DEV_IMG" --target=jdk docker-compose
  step "Compile all Maven projects (inside a Docker image)"
	mkdir -p /tmp/home
	docker run --rm -t -i -v "$PWD:/src" -u "`id -u`:`id -g`" -e HOME="/src/tmp/home" \
		-e MAVEN_OPTS="-Dmaven.repo.local=/src/tmp/home/.m2/repository"	\
		${build_sql_init_mode:+-e SPRING_SQL_INIT_MODE="$build_sql_init_mode"} \
		-w /src "$DEV_IMG" sh -c 'cd shanoir-ng-parent && mvn clean install -DskipTests'

  # Always build base images
	step "build all base docker images"
  docker compose -f docker-compose-dev.yml build

fi
if [ -n "$deploy" ] ; then
	#
	# Clean stage
	#
	if [ -n "$clean" ] ; then
		# full clean (--clean)
		# -> destroy all external volumes
		step "Full clean"
		if [ -n "$native" ]; then
      docker compose -f docker-compose-dev.yml -f docker-compose-dev-native.yml down -v
    else
      docker compose -f docker-compose-dev.yml down -v
    fi
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
		if [ -n "$native" ]; then
      docker compose -f docker-compose-dev.yml -f docker-compose-dev-native.yml down
    else
      docker compose -f docker-compose-dev.yml down
    fi
	fi

	#
	# Deploy stage
	#

	# 1. database
	step "init: database"
	docker compose -f docker-compose-dev.yml up -d database
	wait_tcp_ready database 3306

	# 2. keycloak-database + keycloak + init-cert-and-logs
	if [ -n "$keycloak" ] ; then
		step "init: keycloak-database"
		docker compose -f docker-compose-dev.yml up -d keycloak-database
		wait_tcp_ready keycloak-database 3306
		
		step "init: keycloak"
		docker compose -f docker-compose-dev.yml run --rm -e SHANOIR_MIGRATION=init keycloak

		step "start: keycloak"
		docker compose -f docker-compose-dev.yml up -d keycloak
		docker-compose/common/oneshot --pgrp '\| *'				\
				' INFO  \[io.quarkus\] .* Keycloak .* started in [0-9]*'	\
				-- docker compose logs --no-color --follow keycloak >/dev/null

		step "start and stop: init-cert-and-logs"
		docker compose -f docker-compose-dev.yml up -d init-cert-and-logs
	fi

	# 3. infrastructure services: dcm4chee
	if [ -n "$dcm4chee" ] ; then
		step "start: infrastructure services: dcm4chee"
		for infra_ms_dcm4chee in ldap dcm4chee-database dcm4chee-arc
		do
			step "start: $infra_ms_dcm4chee infrastructure microservices dcm4chee"
			docker compose -f docker-compose-dev.yml up -d "$infra_ms_dcm4chee"
		done
	fi
	
	# 4. infrastructure services: others
	if [ -n "$infra" ] ; then
	  step "start: infrastructure services"
	  for infra_ms in rabbitmq solr bids-validator
	  do
		  step "start: $infra_ms infrastructure microservice"
		  docker compose -f docker-compose-dev.yml up -d "$infra_ms"
	  done
  fi
	
	# 5. Shanoir microservices
	step "start: shanoir microservices"
	for ms in users studies datasets import preclinical nifti-conversion
	do
		step "init: $ms microservice"
		docker compose -f docker-compose-dev.yml run --rm -e SHANOIR_MIGRATION=init "$ms"
		if [ -n "$native" ] && [ "$ms" = "users" ] ; then
			# Build the native images (paketo/buildpacks), if requested;
	    # build-image requires a Docker engine to run and produce the native image.
	    # Mounting the host-docker inside the image resulted in permissions denied
	    # or clean cache problems with the re-execution of the script, so we do it locally
      step "Compile Shanoir native images locally"
      command -v mvn >/dev/null 2>&1 \
          || die "mvn not found on PATH: --native builds the 'users' native image locally (see comment above); install a local Maven + matching JDK, or drop --native"
        m2repo="$PWD/tmp/home/.m2/repository"
        MAVEN_OPTS="-Dmaven.repo.local=$m2repo" \
            mvn -f ./shanoir-ng-users/pom.xml \
            -Pnative spring-boot:build-image \
            -DskipTests
      step "Build users-native, use referenced image from local Docker"
			docker compose -f docker-compose-dev.yml -f docker-compose-dev-native.yml build users
			step "start: $ms microservice (native)"
			docker compose -f docker-compose-dev.yml -f docker-compose-dev-native.yml up -d users
			continue
		fi
		  step "start: $ms microservice"
		if [ -n "$native" ]; then
		  docker compose -f docker-compose-dev.yml -f docker-compose-dev-native.yml up -d "$ms"
    else
		  docker compose -f docker-compose-dev.yml up -d "$ms"
		fi
	done

	# 6. nginx
	step "start: nginx"
  if [ -n "$native" ]; then
  	docker compose -f docker-compose-dev.yml -f docker-compose-dev-native.yml up -d nginx
  else
  	docker compose -f docker-compose-dev.yml up -d nginx
  fi

fi
