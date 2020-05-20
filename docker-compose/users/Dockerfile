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

FROM debian:stretch

RUN set -ex && \
    echo 'deb http://deb.debian.org/debian stretch-backports main' \
      > /etc/apt/sources.list.d/stretch-backports.list && \
    apt update -y && \
    apt install -t \
      stretch-backports \
      openjdk-8-jre-headless \
      ca-certificates-java -y

# Users microservice is build at first, so prepare self-signed certificate for all
# other microservices and Nginx afterwards and put them into the share
RUN apt-get update && apt-get install openssl
RUN echo generating self-signed key and certificate
RUN /usr/bin/openssl genrsa 2048 > /etc/ssl/certs/java/shanoir-ng-nginx.key
RUN /usr/bin/openssl req -new -key /etc/ssl/certs/java/shanoir-ng-nginx.key -batch -subj "/CN=shanoir-ng-nginx" > /etc/ssl/certs/java/shanoir-ng-nginx.csr
RUN /usr/bin/openssl x509 -req -days 730 -in /etc/ssl/certs/java/shanoir-ng-nginx.csr -signkey /etc/ssl/certs/java/shanoir-ng-nginx.key -out /etc/ssl/certs/java/shanoir-ng-nginx.crt
RUN rm /etc/ssl/certs/java/shanoir-ng-nginx.csr

# Import certificate into keystore for Java
RUN keytool -importcert -file /etc/ssl/certs/java/shanoir-ng-nginx.crt -alias shanoir-ng-nginx -keystore /etc/ssl/certs/java/cacerts -storepass changeit -noprompt -trustcacerts

RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-users.jar shanoir-ng-users.jar
COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and development profile purpose
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9911,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-users.jar", "--syncAllUsersToKeycloak=true"]


ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-users.jar"]
