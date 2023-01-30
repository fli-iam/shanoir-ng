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

ARG BASE_IMAGE=quay.io/keycloak/keycloak:20.0.0

#
# Use builder to integrate custom provider
#
FROM $BASE_IMAGE as builder

COPY shanoir-ng-keycloak-auth.jar /opt/keycloak/providers

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build

#
# Create actual image, based on builder before
#
FROM $BASE_IMAGE

COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY --chown=keycloak themes/. /opt/keycloak/themes
COPY cfg/. /opt/keycloak/

COPY entrypoint entrypoint_common oneshot /bin/

ENTRYPOINT ["/bin/entrypoint", "/opt/keycloak/bin/kc.sh"]
CMD ["start"]
