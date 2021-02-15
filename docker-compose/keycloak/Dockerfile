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

FROM jboss/keycloak:12.0.1

COPY themes/. /opt/jboss/keycloak/themes

COPY shanoir-ng-keycloak-auth.jar /opt/jboss/keycloak/standalone/deployments/

COPY cfg/. /opt/jboss/keycloak/

COPY entrypoint entrypoint_common oneshot /bin/

# unset the env var, so that the default value is set by the entrypoint script
# (like all other env vars)
ENV PROXY_ADDRESS_FORWARDING ""

ENTRYPOINT ["/bin/entrypoint", "/opt/jboss/tools/docker-entrypoint.sh", "-b", "0.0.0.0", \
	"-bmanagement", "0.0.0.0", "-Dkeycloak.profile.feature.scripts=enabled"]
