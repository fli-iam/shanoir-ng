FROM debian:jessie

RUN set -ex && \
    echo 'deb http://deb.debian.org/debian jessie-backports main' \
      > /etc/apt/sources.list.d/jessie-backports.list && \
    apt update -y && \
    apt install -t \
      jessie-backports \
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

# Use the below line for remote debugging and development profile purpose
#ENTRYPOINT ["java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9911,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-users.jar", "--syncAllUsersToKeycloak=true"]

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-users.jar", "--syncAllUsersToKeycloak=true"]