FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD shanoir-ng-users-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'

RUN apk add --no-cache mariadb mariadb-client
RUN mysql_install_db

ADD populate.sql /populate.sql

ENTRYPOINT \
	/usr/share/mysql/mysql.server start --user=root \
	&& mysql -u root -e " \
		create database shanoir_ng_users; \
		grant all privileges on shanoir_ng_users.* to 'shanoir'@'localhost' identified by 'shanoir' with grant option;" \
	&& java -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.redis.host=redis