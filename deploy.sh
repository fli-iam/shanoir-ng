#!/bin/sh
# script that helps with local dev/deploy tasks

export SHANOIR_URL_HOST=shanoir-ng-nginx
export SHANOIR_URL_SCHEME=https
export SHANOIR_PREFIX=
export SHANOIR_ADMIN_EMAIL="nobody@inria.fr"
export SHANOIR_KEYCLOAK_USER=admin
export SHANOIR_KEYCLOAK_PASSWORD="&a1A&a1A"

cd ./shanoir-ng-parent

mvn clean install -DskipTests

cd ..

docker-compose build

docker-compose up -d