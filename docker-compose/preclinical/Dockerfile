FROM debian:stretch

RUN set -ex && \
    echo 'deb http://deb.debian.org/debian stretch-backports main' \
      > /etc/apt/sources.list.d/stretch-backports.list && \
    apt update -y && \
    apt install -t \
      stretch-backports \
      openjdk-11-jre-headless \
      ca-certificates-java -y

RUN apt-get update

RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-preclinical.jar shanoir-ng-preclinical.jar

COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9915,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-preclinical.jar"]

ENTRYPOINT ["/bin/entrypoint", "java","-Xmx6g", "-Xms1g",  "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-preclinical.jar"]
