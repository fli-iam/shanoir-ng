FROM jboss/keycloak

ENV KEYCLOAK_USER=admin KEYCLOAK_PASSWORD=&a1A&a1A MYSQL_ADDR=keycloak-database MYSQL_PORT=3306 MYSQL_DATABASE=keycloak MYSQL_USER=keycloak MYSQL_PASSWORD=password

COPY themes/. /opt/jboss/keycloak/themes

COPY shanoir-ng-keycloak-auth.jar /opt/jboss/keycloak/standalone/deployments/

COPY cfg/. /opt/jboss/keycloak/

CMD ["-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-Dkeycloak.import=/opt/jboss/keycloak/shanoir-ng-realm.json"]