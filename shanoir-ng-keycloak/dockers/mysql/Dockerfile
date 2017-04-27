FROM mysql:8.0

COPY my.cnf /etc/mysql/mysql.conf.d/shanoir-ng-users.cnf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_keycloak.sql /dk/shanoirng_keycloak.sql

