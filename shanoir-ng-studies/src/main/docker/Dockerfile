FROM shanoir-ng/debianjava8mariadbpython:latest

COPY shanoir-ng-studies-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-studies.jar

COPY my.cnf /etc/mysql/conf.d/shanoir-ng-studies.cnf
COPY ms.studies.sv.conf /etc/supervisor/conf.d/ms.studies.sv.conf

COPY docker-entrypoint.sh /opt/mysql/init/mariadb_init.sh
RUN chmod +x /opt/mysql/init/mariadb_init.sh

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

COPY shanoirng_studies.sql /dk/shanoirng_studies.sql
COPY shanoirng_studies_tables.sql /dk/shanoirng_studies_tables.sql

COPY studiesMigration.py /dk/studiesMigration.py
RUN chmod +x /dk/studiesMigration.py

EXPOSE 9900
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
