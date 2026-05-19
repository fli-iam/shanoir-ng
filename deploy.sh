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

#!/bin/sh
# script that helps with local dev/deploy tasks

export SHANOIR_URL_HOST=shanoir-ng-nginx
export SHANOIR_URL_SCHEME=https
export SHANOIR_VIEWER_OHIF_URL_HOST=viewer
export SHANOIR_VIEWER_OHIF_URL_SCHEME=https
export SHANOIR_PREFIX=
export SHANOIR_ADMIN_EMAIL="nobody@inria.fr"
export SHANOIR_KEYCLOAK_USER=admin
export SHANOIR_KEYCLOAK_PASSWORD="&a1A&a1A"

cd ./shanoir-ng-parent

mvn clean install -DskipTests

cd ..

docker compose build

docker compose up -d