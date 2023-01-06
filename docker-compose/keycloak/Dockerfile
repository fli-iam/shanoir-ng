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

#
# Use builder to integrate custom provider
#
FROM quay.io/keycloak/keycloak:20.0.2 as builder

COPY shanoir-ng-keycloak-auth.jar /opt/keycloak/providers

# Configure a database vendor
ENV KC_DB=mysql

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build

#
# Create actual image, based on builder before
#
FROM quay.io/keycloak/keycloak:20.0.0

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY themes/. /opt/keycloak/themes
COPY cfg/. /opt/keycloak/

COPY entrypoint entrypoint_common oneshot /bin/

# unset the env var, so that the default value is set by the entrypoint script
# (like all other env vars)
ENV PROXY_ADDRESS_FORWARDING ""

ENTRYPOINT ["/bin/entrypoint", "/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm", "--hostname-path=/auth", "--proxy=edge", "--spi-theme-welcome-theme=shanoir-theme"]
