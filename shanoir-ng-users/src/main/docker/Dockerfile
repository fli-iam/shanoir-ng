FROM shanoir-ng/base-ms

RUN apt-get install -qqy --no-install-recommends python-pymysql

COPY shanoir-ng-users-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-users.jar

COPY shanoir-ng-keycloak.jar /opt/shanoir-ng/shanoir-ng-keycloak.jar

# enable mysql daemon
RUN  cd /etc/supervisor && cp template.d/mysql.conf conf.d/

# enable microservice
COPY ms.users.sv.dev.conf /etc/supervisor/conf.d/

COPY run-* /usr/local/sbin/
COPY dk/. dk/.

# microservice name
ENV MS="shanoir-ng-users"

EXPOSE 9900
CMD ["run-supervisor"]
