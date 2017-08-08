FROM shanoir-ng/base-ms

ENV	MS="keycloak"	\
	KEYCLOAK_VERSION="3.1.0.Final"	\
	MARIADB_CONNECTOR_VERSION="2.1.0"

RUN apt-get --no-install-recommends -qqy install curl unzip

# download and unpack keycloak
RUN cd /opt/ &&	curl -sSf -o tmp.zip	\
	http://downloads.jboss.org/keycloak/"$KEYCLOAK_VERSION"/keycloak-"$KEYCLOAK_VERSION".zip	&&\
	unzip -q tmp.zip && mv keycloak-"$KEYCLOAK_VERSION" keycloak

# links to the data & log dirs
RUN	cd /opt/keycloak/standalone	&&\
	chown -R shanoir: .		&&\
	rm -rf data log tmp		&&\
	ln -s /vol/cache/keycloak data	&&\
	ln -s /vol/log/keycloak   log	&&\
	ln -s /tmp/keycloak       tmp

# download and unpack the mariadb connector
#
# Its configuration is provided by:
#  - patches/01_mysql_config.diff
#  - files/opt/keycloak/modules/system/layers/base/org/mariadb/main/module.xml
RUN	mkdir -p   /opt/keycloak/modules/system/layers/base/org/mariadb/main	&&\
	curl -sSf -o /opt/keycloak/modules/system/layers/base/org/mariadb/main/mariadb-java-client.jar	\
	https://downloads.mariadb.com/Connectors/java/connector-java-"$MARIADB_CONNECTOR_VERSION"/mariadb-java-client-"$MARIADB_CONNECTOR_VERSION".jar

# apply patches
COPY patches /tmp/patches
RUN set -e ; for p in /tmp/patches/*.diff ; do patch -F0 -p0 <"$p" ; done

RUN apt-get install --no-install-recommends -qqy  python3 python3-pymysql
# static files
COPY files/. /

# install shanoir-ng-keycloak-*.jar (they are located outside the files/
# subdirectory to ensure the build fails if they are absent)
#
COPY shanoir-ng-keycloak-auth.jar /opt/jboss/keycloak/providers/
COPY shanoir-ng-keycloak-init.jar /opt/cfg/shanoir-ng-keycloak-init.jar

ENV	SMTP_HOST="localhost"	\
	SMTP_PORT="25"		\
	SHANOIR_ADMIN_EMAIL="shanoir@localhost"	\
	SHANOIR_ADMIN_NAME="Shanoir admin"	\
	SHANOIR_SERVER_URL="https://localhost"

EXPOSE 8080


#TODO: remove? 
#
#RUN chmod -R 777 /opt/jboss/keycloak/themes/shanoir-theme
#
#RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
#  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
#  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
#
#ENV MAVEN_HOME /usr/share/maven
#
#RUN cd /opt/jboss && curl -s http://downloads.jboss.org/keycloak/$KEYCLOAK_VERSION/keycloak-demo-$KEYCLOAK_VERSION.zip -o tmp.zip && unzip tmp.zip -d . && mv /opt/jboss/keycloak-demo-$KEYCLOAK_VERSION /opt/jboss/keycloak-demo
#
#RUN mvn package -f /opt/jboss/keycloak-demo/examples/preconfigured-demo/pom.xml && rm -rf ~/.m2/repository
#RUN cd /opt/jboss/keycloak-demo/examples/preconfigured-demo && find -name *.war | grep -v ear | xargs -I {} cp {} /opt/jboss/keycloak-demo/keycloak/standalone/deployments/ && cp /opt/jboss/keycloak-demo/examples/preconfigured-demo/testrealm.json /opt/jboss/keycloak-demo/keycloak/
