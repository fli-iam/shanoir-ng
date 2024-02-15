# Shanoir NG - Import, manage and share neuroimaging data
# Copyright (C) 2009-2019 Inria - https://www.inria.fr/
# Contact us on https://project.inria.fr/shanoir/
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

FROM quay.io/keycloak/keycloak:23.0.5 as base_image

# keycloak options (https://www.keycloak.org/server/all-config)
#
# Here we list only the variables that need to be set at both runtime *and* at
# buildtime ("kc.sh build"). If they do not have the same values then the
# quarkus image has to be rebuilt at runtime, which delays container startup
# with a message like: "(Quarkus augmentation completed in 180315ms)"
#
# The relevant variables can be listed with: /opt/keycloak/bin/kc.sh show-config
#
# All other variables are set in the entrypoint.
ENV KC_DB="mysql" \
    KC_LOG="console,file" \
    KC_HTTP_RELATIVE_PATH="/auth"

#
# Use builder to integrate custom provider
#
FROM base_image as builder

COPY shanoir-ng-keycloak-auth.jar /opt/keycloak/providers

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build

#
# Create actual image, based on builder before
#
FROM base_image

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --chown=keycloak themes/. /opt/keycloak/themes
COPY cfg/. /opt/keycloak/
RUN  mkdir /opt/keycloak/data/log

COPY entrypoint entrypoint_common /bin/

ENTRYPOINT ["/bin/entrypoint", "/opt/keycloak/bin/kc.sh"]
CMD []
