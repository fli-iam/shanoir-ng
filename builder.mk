#
# makefile used on the builder at shanoir-2016.irisa.fr
#

# designed to build all images from scratch


# keycloak images
#
# https://github.com/fli-iam/shanoir-ng/wiki/Installation-guide-3%29-Docker-Keycloak

keycloak-mysql:
	(cd shanoir-ng-keycloak/dockers/mysql && docker build -t shanoir-ng/keycloak-mysql .)

keycloak:
	(cd shanoir-ng-keycloak-init && mvn package)
	(cd shanoir-ng-keycloak/dockers/keycloak/ && docker build -t shanoir-ng/keycloak .)

users: keycloak-package
keycloak-package:
	# prepare the package for the shanoir-ng-users package
	(cd shanoir-ng-users/ && mvn install -Pinit-keycloak)
	(cd shanoir-ng-keycloak && mvn package)

# base image
base-image:
	docker build -t shanoir-ng/base shanoir-ng-template/shanoir-ng-base

# base image for the microservices
base-ms-image: base-image
	docker build -t shanoir-ng/base-ms shanoir-ng-template/shanoir-ng-base-ms

# microservices
users studies studycards: %: base-ms-image
	# https://github.com/fli-iam/shanoir-ng/wiki/Installation-guide-4%29-Docker-shanoir-ng-users
	(cd 'shanoir-ng-$@/' && mvn clean package docker:build -DskipTests -Pqualif)

# shanoir-ng-nginx
#
# https://github.com/fli-iam/shanoir-ng/wiki/Installation-guide-6%29-Docker-Nginx-with-statics
nginx: keycloak users studies studycards
	npm set registry https://registry.npmjs.org

	(cd shanoir-ng-front && mvn package -Pqualif)
	(cd shanoir-ng-nginx && docker build -t shanoir-ng/nginx:latest .)

# all images for shanoir NG
all: keycloak users studies studycards nginx
