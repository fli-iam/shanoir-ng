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

FROM debian:stretch

RUN set -ex && \
    echo 'deb http://deb.debian.org/debian stretch-backports main' \
      > /etc/apt/sources.list.d/stretch-backports.list && \
    apt update -y && \
    apt install -t \
      stretch-backports \
      openjdk-8-jre-headless \
      ca-certificates-java -y

RUN apt-get update -qq \
    && apt-get install -y \
    git \
    curl \
    build-essential \
    cmake \
    pkg-config \
    libgdcm-tools=2.6.6-3 \
    bsdtar \
    unzip \
    pigz \
    gzip \
    wget \
    jq \
    python

# Compile DCM2NIIX from source
ENV DCMCOMMIT=efd36a02e2a8e21acfbc62f41beeacf6731d67ac
RUN curl -#L  https://github.com/rordenlab/dcm2niix/archive/$DCMCOMMIT.zip | bsdtar -xf- -C /usr/local
WORKDIR /usr/local/dcm2niix-${DCMCOMMIT}/build
RUN cmake -DUSE_OPENJPEG=ON ../ && \
    make && \
    make install

WORKDIR /

RUN mkdir -pv /var/log/shanoir-ng-logs

ADD shanoir-ng-import.jar shanoir-ng-import.jar
COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and to active dev profile
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9913,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-import.jar"]
ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/shanoir-ng-import.jar"]
