FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-users-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-users.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-users.cnf
COPY ms.users.sv.conf /etc/supervisor/conf.d/ms.users.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_users.sql /dk/shanoirng_users.sql

EXPOSE 9900
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
