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

FROM node:lts-alpine3.14

RUN apk update && apk add --virtual build-dependencies build-base gcc wget git yarn unzip pigz gzip python2 make

WORKDIR /usr/local/
COPY Viewers-master.zip /usr/local

RUN unzip Viewers-master.zip

WORKDIR /usr/local/Viewers-master/platform/viewer

# Copied from https://v3-docs.ohif.org/2.0/deployment/recipes/build-for-production
	# If you haven't already, enable yarn workspaces
	# Restore dependencies
	# Build source code for production
RUN yarn config set workspaces-experimental true \
	&& yarn install \
	&& yarn run build
	
WORKDIR /usr/local/Viewers-master/platform/viewer/dist/

	# Install http-server as a globally available package
	# Serve the files in our current directory
	# Accessible at: `http://localhost:8080`
RUN yarn global add http-server \
	&& http-server