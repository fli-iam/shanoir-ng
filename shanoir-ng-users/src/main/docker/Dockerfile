FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-users-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-users.jar

COPY shanoir-ng-keycloak.jar /opt/shanoir-ng/shanoir-ng-keycloak.jar

# enable mysql daemon
RUN  cd /etc/supervisor && cp template.d/mysql.conf conf.d/
COPY ms.users.sv.conf /etc/supervisor/conf.d/ms.users.sv.conf


COPY dk/. dk/.

# microservice name
ENV MS="shanoir-ng-users"

EXPOSE 9900
