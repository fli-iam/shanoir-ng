#!/bin/sh
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

print_help()
{
	cat <<EOF
Build and deploy Shanoir
usage:
	$0 --clean|--force|--no-deploy [--no-build] [--no-keycloak] [--no-dcm4chee] [--no-infra] [--native] [-h|--help]

CAUTION: THIS COMMAND IS DESTRUCTIVE, do not use it on an existing production
instance. It will overwrite the data hosted in the external volumes declared in
docker compose.yml (note: as a safety precaution, the command will fail if
'--clean' or '--force' option is not used).

Options:
--clean		perform a clean deployment (will run 'docker compose down -v' to destroy all existing volumes)
--force		force deploying over the existing volumes (might be a little faster, use it in dev only)
--no-deploy	skip the deployment stage

--no-build	skip the build stage
--no-keycloak	do not run Keycloak (used if Keycloak is external)
--no-dcm4chee	do not run dcm4chee (used if dcm4chee is external)
--no-infra	do not run infra-service (solr, rabbitmq, bids-validator)
--native	run users, studies and import as native images (built locally with
		'mvn -Pnative spring-boot:build-image', needs mvn + JDK on the host)
		via the docker-compose-dev-native.yml overlay. Without it, all
		microservices run as regular (AOT-processed) JVM fat jars.
		The JVM images are always built: they run the one-shot
		SHANOIR_MIGRATION=init schema bootstrap, which native images cannot.
-h|--help	print this help

EOF
	exit 0
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

BASE_COMPOSE=docker-compose-dev.yml
NATIVE_COMPOSE=docker-compose-dev-native.yml

# Microservices that have a native image (module dir: ./shanoir-ng-<name>,
# image: shanoir-ng-<name>-native:latest, see the native overlay)
NATIVE_SERVICES="users studies import"
# All Shanoir microservices
MICROSERVICES="users studies datasets import preclinical nifti-conversion"

# Always the plain JVM config (build + one-shot init)
dc_jvm()
{
	docker compose -f "$BASE_COMPOSE" "$@"
}

wait_tcp_ready()
{
	container="$1"
	tcp_port="$2"

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
		--no-keycloak)	keycloak=	;;
		--no-dcm4chee)	dcm4chee=	;;
		--no-infra)	infra=		;;
		--no-deploy)	deploy=		;;
		--native)	native=1	;;
		*)		die "unknown option '$1'"
	esac
	shift
done

if [ -z "$clean$force" ] && [ -n "$deploy" ] ; then
	die "you must provide at least --clean, --force or --no-deploy"
fi

if [ -n "$native" ] && [ -n "$build" ] ; then
	# build-image needs a Docker engine; mounting the host docker inside a
	# container caused permission/cache problems, so native images are built
	# with the local Maven.
	command -v mvn >/dev/null 2>&1 \
		|| die "mvn not found on PATH: --native builds the native images ($NATIVE_SERVICES) locally; install Maven + a matching JDK, or drop --native"
fi

# From here on, a plain 'docker compose ...' uses the right set of files:
# JVM only, or JVM + native overlay (same service names, so ports, volumes,
# depends_on and nginx upstreams are unchanged).
if [ -n "$native" ] ; then
	COMPOSE_FILE="$BASE_COMPOSE:$NATIVE_COMPOSE"
else
	COMPOSE_FILE="$BASE_COMPOSE"
fi
export COMPOSE_FILE

#
# Build stage
#
if [ -n "$build" ] ; then
	build_sql_init_mode=
	# process-aot needs the Spring configuration already at Maven build time
	case "${SHANOIR_MIGRATION:-dev}" in
		dev|init)	build_sql_init_mode=always ;;
	esac

	step "Compile all Maven projects (inside a Docker image)"
	DEV_IMG=shanoir-ng-dev
	docker build -t "$DEV_IMG" --target=jdk docker-compose
	mkdir -p tmp/home
	docker run --rm -t -i -v "$PWD:/src" -u "`id -u`:`id -g`" -e HOME="/src/tmp/home" \
		-e MAVEN_OPTS="-Dmaven.repo.local=/src/tmp/home/.m2/repository" \
		${build_sql_init_mode:+-e SPRING_SQL_INIT_MODE="$build_sql_init_mode"} \
		-w /src "$DEV_IMG" sh -c 'cd shanoir-ng-parent && mvn clean install -DskipTests'

	# JVM images: always built (used for the whole stack, or at least for 'init')
	step "Build JVM docker images"
	dc_jvm build

	if [ -n "$native" ] ; then
		for ms in $NATIVE_SERVICES ; do
			step "Build $ms native image"
			MAVEN_OPTS="-Dmaven.repo.local=$PWD/tmp/home/.m2/repository" \
				mvn -f "./shanoir-ng-$ms/pom.xml" \
				-Pnative spring-boot:build-image -DskipTests
		done
	fi
fi

#
# Deploy stage
#
if [ -n "$deploy" ] ; then
	if [ -n "$clean" ] ; then
		# --clean: destroy all external volumes
		step "Full clean"
		docker compose down -v
	else
		# --force: just remove all existing containers ('compose run' must not
		# be used while the service is up, and old logs must not be displayed)
		step "stop shanoir"
		docker compose down
	fi

	# 1. database
	step "init: database"
	docker compose up -d database
	wait_tcp_ready database 3306

	# 2. init-cert-and-logs (always) + keycloak-database + keycloak
	if [ -n "$keycloak" ] ; then
		step "init: keycloak-database"
		docker compose up -d keycloak-database
		wait_tcp_ready keycloak-database 3306

		step "init: keycloak"
		docker compose run --rm -e SHANOIR_MIGRATION=init keycloak

		step "start: keycloak"
		docker compose up -d keycloak
		docker-compose/common/oneshot --pgrp '\| *'				\
				' INFO  \[io.quarkus\] .* Keycloak .* started in [0-9]*'	\
				-- docker compose logs --no-color --follow keycloak >/dev/null
	fi

	step "start and stop: init-cert-and-logs"
	docker compose up -d init-cert-and-logs

	# 3. dcm4chee
	if [ -n "$dcm4chee" ] ; then
		for svc in ldap dcm4chee-database dcm4chee-arc ; do
			step "start: $svc"
			docker compose up -d "$svc"
		done
	fi

	# 4. other infrastructure services
	if [ -n "$infra" ] ; then
		for svc in rabbitmq solr bids-validator ; do
			step "start: $svc"
			docker compose up -d "$svc"
		done
	fi

	# 5. Shanoir microservices
	# One-shot schema init: always with the JVM image/config, since native
	# images have no init wrapper.
	for ms in $MICROSERVICES ; do
		step "init: $ms"
		dc_jvm run --rm -e SHANOIR_MIGRATION=init "$ms"
	done

	# Start for real: with --native, compose recreates users/studies/import
	# from the native overlay (same names, same ports); others stay JVM.
	step "start: shanoir microservices${native:+ (native: $NATIVE_SERVICES)}"
	docker compose up -d $MICROSERVICES

	# 6. nginx
	step "start: nginx"
	docker compose up -d nginx
fi
