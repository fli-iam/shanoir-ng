FROM mysql/mysql-server:5.7

ADD 1_add_users.sql /docker-entrypoint-initdb.d/

RUN chmod a+x /docker-entrypoint-initdb.d/1_add_users.sql