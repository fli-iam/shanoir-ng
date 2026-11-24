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
# Deploys (or upgrades) shanoir with docker-compose.yml: pulls the released images
# <SHANOIR_REGISTRY>/<service>:<SHANOIR_IMAGE_TAG> and (re)starts the containers.
# The configuration and the secrets come from .env (see .env.example).
# Run it after 'git pull', so that docker-compose.yml and the images are from the
# same release. For a development deployment (images built from the sources), use
# bootstrap.sh instead.

set -e

cd "$(dirname "$0")"

./check-env.sh

# without this, 'up' keeps the images already on this machine: SHANOIR_IMAGE_TAG=latest
# would never upgrade
docker compose pull

docker compose up -d
