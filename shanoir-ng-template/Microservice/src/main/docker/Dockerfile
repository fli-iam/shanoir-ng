FROM shanoir-ng/debianjava8mariadb:latest

COPY shanoir-ng-template-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-template.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-template.cnf
COPY ms.template.sv.conf /etc/supervisor/conf.d/ms.template.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_template.sql /dk/shanoirng_template.sql

EXPOSE 9900
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
