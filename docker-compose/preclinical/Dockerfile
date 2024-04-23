#--------------- common jre base image -------------------------------------
FROM debian:trixie
# NOTE: using trixie for jdk21
RUN echo "deb http://deb.debian.org/debian trixie-proposed-updates main" >> /etc/apt/sources.list \
    && apt-get update -qq \
    && apt-get install -qqy openjdk-21-jre-headless ca-certificates-java
#----------------------------------------------------------------------------


RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-preclinical.jar shanoir-ng-preclinical.jar

COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=0.0.0.0:9915,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-preclinical.jar"]

ENTRYPOINT ["/bin/entrypoint", "java","-Xmx6g", "-Xms1g",  "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-preclinical.jar"]
