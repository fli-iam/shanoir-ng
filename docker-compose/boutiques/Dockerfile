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

# RUN groupadd -r boutiques && useradd --no-log-init -r -g boutiques boutiques
# USER boutiques

RUN apk update && apk add bash && apk add docker && apk add python && apk add py-pip

RUN pip install boutiques

RUN mkdir -pv /var/log/shanoir-ng-logs
RUN mkdir -pv /output

ADD shanoir-ng-boutiques.jar shanoir-ng-boutiques.jar

# Use the below line for remote debugging and to active dev profile
# ENTRYPOINT ["java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9916,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-boutiques.jar"]

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-boutiques.jar"]