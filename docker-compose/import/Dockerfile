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

#--------------- common jre base image -------------------------------------
FROM debian:bookworm
# - disable the automatic "apt-get clean" command (because we mount
#   /var/cache/apt from an external volume to speed-up the build)
# - NOTE: using bookworm-proposed-updates because of
#         https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=1039472
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    rm /etc/apt/apt.conf.d/docker-clean \
    && echo "deb http://deb.debian.org/debian bookworm-proposed-updates main" >> /etc/apt/sources.list \
    && apt-get update -qq \
    && apt-get install -qqy openjdk-17-jre-headless ca-certificates-java
#----------------------------------------------------------------------------

RUN mkdir -pv /var/log/shanoir-ng-logs

ADD shanoir-ng-import.jar shanoir-ng-import.jar
COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and to active dev profile
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=0.0.0.0:9913,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-import.jar"]
ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-import.jar"]
