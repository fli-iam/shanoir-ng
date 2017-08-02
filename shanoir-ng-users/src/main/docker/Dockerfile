FROM shanoir-ng/base-ms

RUN apt-get install -qqy --no-install-recommends python-pymysql

COPY shanoir-ng-users-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-msapp.jar
COPY shanoir-ng-keycloak.jar             /opt/shanoir-ng/shanoir-ng-keycloak.jar

COPY dk/. dk/.

# microservice name
ENV MS="shanoir-ng-users"

