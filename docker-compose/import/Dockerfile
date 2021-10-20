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
      openjdk-11-jre-headless \
      ca-certificates-java -y

#32 bits packages are necessary
RUN dpkg --add-architecture i386
RUN apt-get update

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
    lib32z1 \
    libgtk2.0-0 \
    libsm6 \
    libxext6 \
    lib32stdc++6 \
    libtiff5 \
    libglib2.0-0:i386 \
    python

# Compile DCM2NIIX from source
ENV DCMCOMMIT_VERSION=v1.0.20210317
ENV DCMCOMMIT=1.0.20210317

WORKDIR /usr/local/
RUN curl -#L  https://github.com/rordenlab/dcm2niix/archive/refs/tags/$DCMCOMMIT_VERSION.zip | bsdtar -xf- -C .
WORKDIR /usr/local/dcm2niix-${DCMCOMMIT}
RUN mkdir build
WORKDIR /usr/local/dcm2niix-${DCMCOMMIT}/build
RUN cmake ..
RUN make install

WORKDIR /

RUN mkdir -p /opt/nifti-converters

# Copy converters files
RUN mkdir external
COPY external ./external
RUN install -m 0755 external/dcm2nii/linux/31MARCH2008/dcm2nii /opt/nifti-converters/dcm2nii_2008-03-31
RUN install -m 0755 external/dcm2nii/linux/dcm2nii /opt/nifti-converters/dcm2nii_2014-08-04

RUN install -m 0755 external/mcverter/linux/mcverter_* /opt/nifti-converters/
RUN cp -n           external/mcverter/linux/lib/lib*.so.* /usr/lib/x86_64-linux-gnu/

RUN mkdir -m 1777 /.dcm2nii_2008-03-31
RUN mkdir -m 1777 /.dcm2nii_2014-08-04

# install dicom tool clidcm
RUN mkdir -p /opt/clidcm/libx64
RUN install -m 644 external/clidcm/bin/linux/libx64/* /opt/clidcm/libx64/
RUN chmod 755 /opt/clidcm/libx64/clidcm
RUN cp -n           external/clidcm/bin/linux/libx64/lib*.so.* /usr/lib/x86_64-linux-gnu/


RUN echo "#!/bin/sh" >/opt/nifti-converters/clidcm
RUN echo "LD_LIBRARY_PATH=/opt/clidcm/libx64/ exec /opt/clidcm/libx64/clidcm \"\$@\"" >/opt/nifti-converters/clidcm

RUN chmod 0755 /opt/nifti-converters/clidcm

# clidcm uses /usr/share/dcmtk/dicom.dic but this file is now provided by libdcmtk2
RUN ln -s libdcmtk2 /usr/share/dcmtk
#RUN test -f /usr/share/dcmtk/dicom.dic

# install dicom tool dcmdjpeg
RUN install -m 755 external/dcmtk/dcmdjpeg/linux/bin/dcmdjpeg /usr/local/bin/
RUN install -m 644 external/dcmtk/dcmdjpeg/linux/lib/dicom.dic /usr/local/lib/

# install dicom tool dcm2pnm
RUN install -m 755 external/dcmtk/dcm2pnm/linux/dcm2pnm /usr/local/bin/


RUN mkdir -pv /var/log/shanoir-ng-logs

ADD shanoir-ng-import.jar shanoir-ng-import.jar
COPY entrypoint entrypoint_common oneshot /bin/

# Use the below line for remote debugging and to active dev profile
#ENTRYPOINT ["/bin/entrypoint", "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9913,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-import.jar"]
ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/shanoir-ng-import.jar"]
