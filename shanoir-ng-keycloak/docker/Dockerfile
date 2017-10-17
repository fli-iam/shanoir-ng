FROM shanoir-ng/base-ms

ENV	MS="keycloak"	\
	KEYCLOAK_VERSION="3.1.0.Final"	\
	MARIADB_CONNECTOR_VERSION="2.1.0"

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
RUN set -e ; for p in /tmp/patches/*.diff ; do patch -F0 -p0 <"$p" ; done ; rm /tmp/patches/*.diff

# static files
COPY files/. /

# generate the master realm config
RUN set -e ; init-mysql ; run-mysql & pid=$! ;			\
	I_AM_BUILDING_THE_DOCKERFILE=1 run-msapp --image-init ;	\
	kill -9 $pid || true ; wait ; rm -rf /tmp/* /vol

# install shanoir-ng-keycloak-*.jar (they are located outside the files/
# subdirectory to ensure the build fails if they are absent)
#
COPY shanoir-ng-keycloak-auth.jar /opt/keycloak/providers/

EXPOSE 8080

