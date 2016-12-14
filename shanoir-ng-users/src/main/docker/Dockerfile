FROM shanoir-ng/debianjava8mariadb:latest

ADD shanoir-ng-users-0.0.1-SNAPSHOT.jar shanoir-ng-users.jar
RUN sh -c 'touch /shanoir-ng-users.jar'

RUN sed -i.bak '/exec \"$@\"/d' ./docker-entrypoint.sh

ADD my.cnf /etc/mysql/conf.d/shanoir-ng-users.cnf
ADD ms.users.sv.conf /etc/supervisor/conf.d/ms.users.sv.conf

ADD setup.sh /setup.sh
RUN chmod +x /setup.sh

CMD ./setup.sh
