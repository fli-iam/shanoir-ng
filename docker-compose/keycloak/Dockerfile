FROM jboss/keycloak:3.4.3.Final

COPY themes/. /opt/jboss/keycloak/themes

COPY shanoir-ng-keycloak-auth.jar /opt/jboss/keycloak/standalone/deployments/

COPY cfg/. /opt/jboss/keycloak/

CMD ["-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-Dkeycloak.import=/opt/jboss/keycloak/shanoir-ng-realm.json"]