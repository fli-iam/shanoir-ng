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

FROM alpine as downloader

# Installation of dcm4che into /opt/dcm4che to manage the store-scu into the
# PACS dcm4che3, used last version 5.21.0 as available on the 2020-02-14
# https://sourceforge.net/projects/dcm4che/files/dcm4che3/
RUN wget -qO dcm4che-bin.zip \
        https://downloads.sourceforge.net/project/dcm4che/dcm4che3/5.21.0/dcm4che-5.21.0-bin.zip \
    && mkdir -p /target/opt && cd /target/opt \
    && unzip /dcm4che-bin.zip && mv dcm4che-* dcm4che \
    && rm    /dcm4che-bin.zip


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

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq \
    && apt-get install -qqy \
    pigz \
    gzip \
    locales \
    locales-all

# take care of path
ENV PATH /opt/dcm4che/bin:$PATH
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# install the files from the 'dowloader' stage
COPY --link --from=downloader /target/. /

RUN mkdir -pv /var/log/shanoir-ng-logs
COPY --link shanoir-ng-datasets.jar shanoir-ng-datasets.jar
COPY --link entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and to active dev profile
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=0.0.0.0:9914,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-datasets.jar"]

ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-datasets.jar"]
