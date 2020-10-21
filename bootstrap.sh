#!/bin/sh

print_help()
{
	cat <<EOF
Build and deploy Shanoir NG
usage:
	$0 --clean|--force|--no-deploy [--no-build] [-h|--help]

CAUTION: THIS COMMAND IS DESTRUCTIVE, do not use it on an existing production
instance. It will overwrite the data hosted in the external volumes declared in
docker-compose.yml (note: as a safety precaution, the command will fail if
'--clean' or '--force' option is not used).

Options:
-h|--help	print this help
--no-build	skip the build stage
--no-deploy	skip the deployment stage
--clean		perform a clean deployment (will run 'docker-compose down -v'
		to destroy all existing volumes)
--force		force deploying over the existing volumes (might be a little
		faster, use it in dev only)
EOF
	exit 0
}

wait_tcp_ready()
{
	local container="$1"
	local tcp_port="$2"

	docker-compose exec -T "$container" bash -c "
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
clean=
force=
while [ $# -ne 0 ] ; do
	case "$1" in
		-h|--help)	print_help	;;
		--clean)	clean=1		;;
		--force)	force=1		;;
		--no-build)	build=		;;
		--no-deploy)	deploy=		;;
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

	step "build shanoir"

	# 1. build a docker image with the java toolchain
	DEV_IMG=shanoir-ng-dev
	docker build -t "$DEV_IMG" - <<EOF
FROM debian:stretch
RUN apt-get update && apt-get install -qqy --no-install-recommends openjdk-8-jdk-headless maven bzip2 git
EOF
	# 2. run the maven build
	mkdir -p /tmp/home
	docker run --rm -t -i -v "$PWD:/src" -u "`id -u`:`id -g`" -e HOME="/src/tmp/home" \
		-e MAVEN_OPTS="-Dmaven.repo.local=/src/tmp/home/.m2/repository"	\
		-w /src "$DEV_IMG" sh -c 'cd shanoir-ng-parent && mvn clean install -DskipTests'

	# 3. build the docker images
	docker-compose build
fi

if [ -n "$deploy" ] ; then
	#
	# Clean stage
	#
	if [ -n "$clean" ] ; then
		# full clean (--clean)
		# -> destroy all external volumes
		step "clean"
		docker-compose down -v
	else
		# overwrite (--force)
		# -> just remove all existing containers
		# 
		# Note: we must ensure that all containers are removed because:
		# - 'docker-compose run' should not be used when the
		#   corresponding service is up
		# - 'docker-compose logs' may display old logs if the container
		#   is not destroyed
		step "stop shanoir"
		docker-compose down
	fi

	#
	# Deploy stage
	#

	# 1. databases
	step "init: database keycloak-database"
	docker-compose up -d database keycloak-database
	wait_tcp_ready database          3306
	wait_tcp_ready keycloak-database 3306

	# 2. keycloak
	step "init: keycloak"
	docker-compose run --rm -e SHANOIR_MIGRATION=init keycloak

	step "start: keycloak"
	docker-compose up -d keycloak
	utils/oneshot	'\| *JBoss Bootstrap Environment'				\
			' INFO  \[org.jboss.as\] .* Keycloak .* started in [0-9]*ms'	\
			-- docker-compose logs --no-color --follow keycloak >/dev/null

	# 3. microservices
	for ms in users studies datasets import preclinical
	do
		step "init: $ms"
		docker-compose run --rm -e SHANOIR_MIGRATION=init "$ms"
		step "start: $ms"
		docker-compose up -d "$ms"
	done

	# 4. nginx
	step "start: nginx"
	docker-compose up -d nginx
fi

