#!/bin/bash

cp /opt/jboss/keycloak/themes/shanoir-theme/login/theme-$DOCKER_PROFILE.properties /opt/jboss/keycloak/themes/shanoir-theme/login/theme.properties

exec /opt/jboss/keycloak/bin/standalone.sh $@
exit $?