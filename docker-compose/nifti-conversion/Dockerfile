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

FROM debian:trixie

# NOTE: using trixie for jdk21
RUN echo "deb http://deb.debian.org/debian bookworm-proposed-updates main" >> /etc/apt/sources.list \
    && apt-get update -qq \
    && apt-get install -qqy openjdk-21-jdk ca-certificates-java

#32 bits packages are necessary
RUN dpkg --add-architecture i386
RUN apt-get update

RUN apt-get update -qq \
    && apt-get install -y \
    git \
    curl \
    cmake \
    build-essential \
    pkg-config \
    libgdcm-tools \
    libarchive-tools \
    unzip \
    pigz \
    gzip \
    gnupg \
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
    locales-all \
    xvfb \
    python3.11 \
    python3-werkzeug \
    python3-yaml \
    software-properties-common

ENV PATH="/root/miniconda3/bin:${PATH}"
ARG PATH="/root/miniconda3/bin:${PATH}"

#Install miniconda \
RUN wget \
    https://repo.anaconda.com/miniconda/Miniconda3-py311_24.1.2-0-Linux-x86_64.sh \
    && mkdir /root/.conda \
    && bash Miniconda3-py311_24.1.2-0-Linux-x86_64.sh -b \
    && rm -f Miniconda3-py311_24.1.2-0-Linux-x86_64.sh


# Compile DCM2NIIX from source
ENV DCMCOMMIT_VERSION=v1.0.20210317
ENV DCMCOMMIT=1.0.20210317
ENV LC_ALL fr_FR.UTF-8
ENV LANG fr_FR.UTF-8
ENV LANGUAGE fr_FR.UTF-8

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

WORKDIR /opt/nifti-converters
RUN mkdir mriconverter
RUN curl -#L  https://github.com/populse/mri_conv/archive/refs/heads/master.zip | bsdtar -xf- -C mriconverter --strip-components 1

RUN chmod 777 /opt/nifti-converters/mriconverter
RUN chmod 777 /opt/nifti-converters/mriconverter/MRIFileManager/MRIManager.jar

WORKDIR /

RUN mkdir -m 1777 /.dcm2nii_2008-03-31
RUN mkdir -m 1777 /.dcm2nii_2014-08-04

# Install dicomifier
RUN git -C /opt/nifti-converters clone https://github.com/lamyj/dicomifier.git
RUN conda install -c conda-forge dicomifier -y

# install animaConvertImage to convert Analyze format into nifti
RUN install -m 755 external/anima/animaConvertImage /usr/local/bin/

RUN mkdir -pv /var/log/shanoir-ng-logs

ADD nifti-conversion.jar nifti-conversion.jar
COPY entrypoint entrypoint_common oneshot /bin/

ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/nifti-conversion.jar"]
