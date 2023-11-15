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
# NOTE: using bookworm-proposed-updates because of https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=1039472
RUN echo "deb http://deb.debian.org/debian bookworm-proposed-updates main" >> /etc/apt/sources.list \
    && apt-get update -qq \
    && apt-get install -qqy openjdk-17-jre-headless ca-certificates-java
#----------------------------------------------------------------------------


#32 bits packages are necessary
RUN dpkg --add-architecture i386

RUN apt-get update -qq \
    && apt-get install -qqy \
    git \
    curl \
    build-essential \
    cmake \
    pkg-config \
    libgdcm-tools \
    libarchive-tools \
    unzip \
    pigz \
    gzip \
    wget \
    jq \
    lib32z1 \
    libgtk2.0-0 \
    libsm6 \
    libxext6 \
    lib32stdc++6 \
    libtiff6 \
    libglib2.0-0:i386 \
    locales \
    locales-all


RUN mkdir -pv /var/log/shanoir-ng-logs

ADD shanoir-ng-import.jar shanoir-ng-import.jar
COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and to active dev profile
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=0.0.0.0:9913,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-import.jar"]
ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-import.jar"]
