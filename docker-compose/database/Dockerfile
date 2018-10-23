FROM mysql/mysql-server:5.7

ADD 1_create_databases.sh /docker-entrypoint-initdb.d/
ADD 2_add_users.sql /docker-entrypoint-initdb.d/

RUN chmod a+x /docker-entrypoint-initdb.d/1_create_databases.sh
RUN chmod a+x /docker-entrypoint-initdb.d/2_add_users.sql