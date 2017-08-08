FROM shanoir-ng/base-ms

RUN apt-get install -qqy --no-install-recommends python-pymysql

# static files
COPY files/. /

# these files are generated dynamically. We copy them separately from files/
# because if not present, we do not want the error to be silently ignored
COPY shanoir-ng-users-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-msapp.jar
COPY shanoir-ng-keycloak.jar             /opt/shanoir-ng/shanoir-ng-keycloak.jar

# microservice name
ENV MS="shanoir-ng-users"

