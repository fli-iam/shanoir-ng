FROM openjdk:8-jdk-alpine
RUN apk add bash

RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-preclinical.jar shanoir-ng-preclinical.jar

COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9915,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-preclinical.jar"]

ENTRYPOINT ["/bin/entrypoint", "java","-Xmx6g", "-Xms1g",  "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-preclinical.jar"]
