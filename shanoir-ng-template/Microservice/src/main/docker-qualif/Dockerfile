FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-templates-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-templates.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-templates.cnf
COPY ms.templates.sv.conf /etc/supervisor/conf.d/ms.templates.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_templates.sql /dk/shanoirng_templates.sql

EXPOSE 9900
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
