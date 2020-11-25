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

FROM openjdk:8-jdk-alpine
RUN apk add bash

# Installation of dcm4che to manage the store-scu into the PACS
# dcm4che3, used last version 5.21.0 as available on the 2020-02-14
# https://sourceforge.net/projects/dcm4che/files/dcm4che3/
RUN wget -O dcm4che-5.21.0-bin.zip https://downloads.sourceforge.net/project/dcm4che/dcm4che3/5.21.0/dcm4che-5.21.0-bin.zip
RUN unzip dcm4che-5.21.0-bin.zip

# take care of path
ENV PATH /dcm4che-5.21.0/bin:$PATH

RUN mkdir -pv /var/log/shanoir-ng-logs
ADD shanoir-ng-datasets.jar shanoir-ng-datasets.jar
COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and to active dev profile
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9914,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-datasets.jar"]

ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-datasets.jar"]
