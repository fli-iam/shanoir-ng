FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-studycards-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-studycards.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-studycards.cnf
COPY ms.studycards.sv.conf /etc/supervisor/conf.d/ms.studycards.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_studycards.sql /dk/shanoirng_studycards.sql

EXPOSE 9900
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
