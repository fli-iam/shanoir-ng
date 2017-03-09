FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-centers-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-centers.jar

#COPY shanoir-ng-keycloak.jar /opt/shanoir-ng/shanoir-ng-keycloak.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-centers.cnf
COPY ms.centers.sv.dev.conf /etc/supervisor/conf.d/ms.centers.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_centers.sql /dk/shanoirng_centers.sql

EXPOSE 9900
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
