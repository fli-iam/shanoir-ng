FROM jboss/keycloak:2.5.1.Final

ADD changeDatabase.xsl /opt/jboss/keycloak/
RUN java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone.xml -xsl:/opt/jboss/keycloak/changeDatabase.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone.xml; java -jar /usr/share/java/saxon.jar -s:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml -xsl:/opt/jboss/keycloak/changeDatabase.xsl -o:/opt/jboss/keycloak/standalone/configuration/standalone-ha.xml; rm /opt/jboss/keycloak/changeDatabase.xsl
RUN mkdir -p /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc/main; cd /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc/main && curl -O http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.18/mysql-connector-java-5.1.18.jar
ADD module.xml /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc/main/

USER root

COPY container_init /dk/container_init
RUN chmod +x /dk/container_init

ADD scripts /dk/scripts_init

USER jboss

ARG profile=dev

ADD shanoir-theme /opt/jboss/keycloak/themes/shanoir-theme

COPY shanoir-ng-keycloak-auth.jar /opt/jboss/keycloak/providers/shanoir-ng-keycloak-auth.jar
COPY shanoir-ng-keycloak-init.jar /dk/shanoir-ng-keycloak-init.jar

COPY shanoir-theme/login/theme-$profile.properties /opt/jboss/keycloak/themes/shanoir-theme/login/theme.properties
