FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-users-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-users.jar

COPY shanoir-ng-keycloak-init.jar /shanoir-ng-keycloak-init.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-users.cnf
COPY ms.users.sv.conf /etc/supervisor/conf.d/ms.users.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY run.sh /run.sh
RUN chmod +x /run.sh

EXPOSE 9900
CMD ["./run.sh"]
