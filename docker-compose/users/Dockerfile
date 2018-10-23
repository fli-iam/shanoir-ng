FROM openjdk:8-jdk-alpine

RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-users.jar shanoir-ng-users.jar

# Use the below line for remote debugging
#ENTRYPOINT ["java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9911,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-users.jar", "--kcAdminClientUsername=admin", "--kcAdminClientPassword=&a1A&a1A", "--syncAllUsersToKeycloak=true"]

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-users.jar", "--kcAdminClientUsername=admin", "--kcAdminClientPassword=&a1A&a1A", "--syncAllUsersToKeycloak=true"]