FROM openjdk:8-jdk-alpine

RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-import.jar shanoir-ng-import.jar

# Use the below line for remote debugging
#ENTRYPOINT ["java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9913,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-import.jar"]

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-import.jar"]