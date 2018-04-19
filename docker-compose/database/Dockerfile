FROM mysql/mysql-server:5.7

ADD 1_create_databases.sh /docker-entrypoint-initdb.d/
ADD 2_add_users.sql /docker-entrypoint-initdb.d/

ENV MYSQL_ROOT_PASSWORD=password