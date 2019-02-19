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

# Debian "Stretch" with Java 8 and MariaDB installed.
# Build image with:  docker build -t shanoir-ng/base-ms .

FROM shanoir-ng/base

# add users shanoir & mysql (with fixed ids)
RUN	groupadd -g 500 -r shanoir && useradd -r -u 500 -g shanoir shanoir &&\
	groupadd -g 501 -r mysql   && useradd -r -u 501 -g mysql   mysql

# install packages (supervisor + mysql + java)
RUN	apt-get update  -qqy	&&\
	DEBIAN_FRONTEND=noninteractive apt-get install -qqy --no-install-recommends	\
		supervisor								\
		mariadb-server python-pymysql python3-pymysql				\
		openjdk-8-jdk-headless openjdk-8-jre-headless ca-certificates-java

# apply patches
COPY patches /tmp/patches
RUN set -e ; for p in /tmp/patches/*.diff ; do patch -F0 -p0 <"$p" ; done ; rm /tmp/patches/*.diff

# static files
COPY files/. /

# MS: microservice name (eg: shanoir-ng-users)
# 	Shall be set in the child image. The name of the mysql database is
# 	derived from this variable (with '-' changed into'_')
#
# The microservice shall be installed as /opt/shanoir-ng/shanoir-ng-msapp.jar
ENV MS=""

EXPOSE 9900
CMD ["run-supervisor"]

