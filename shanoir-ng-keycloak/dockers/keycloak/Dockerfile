FROM jboss/keycloak-mysql:2.5.1.Final
ARG profile=dev

ADD shanoir-theme /opt/jboss/keycloak/themes/shanoir-theme

COPY shanoir-ng-keycloak-auth.jar /opt/jboss/keycloak/providers/shanoir-ng-keycloak-auth.jar

COPY shanoir-theme/login/theme-$profile.properties /opt/jboss/keycloak/themes/shanoir-theme/login/theme.properties
