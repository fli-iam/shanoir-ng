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

FROM debian:bookworm as base_image

# - disable the automatic "apt-get clean" command (because we mount
#   /var/cache/apt from an external volume to speed-up the build)
# - run "apt-get update" now (to avoid downloading the lists 3 times)
RUN rm /etc/apt/apt.conf.d/docker-clean \
    && apt-get update -qq

FROM base_image as conda

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked,id=apt-conda \
    apt-get -qqy update \
    && apt-get -qqy --no-install-recommends install curl ca-certificates

# Install miniconda
RUN --mount=type=cache,target=/opt/miniconda3/pkgs \
    curl -LSsf -o miniconda.sh https://repo.anaconda.com/miniconda/Miniconda3-py311_24.1.2-0-Linux-x86_64.sh \
    && bash miniconda.sh -fb -p /opt/miniconda3 \
    && rm   miniconda.sh

# Install dicomifier
RUN --mount=type=cache,target=/opt/miniconda3/pkgs \
    /opt/miniconda3/bin/conda install -c conda-forge dicomifier -y


FROM base_image as builder

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked,id=apt-builder \
    apt-get -qqy update \
    && apt-get -qqy install \
        build-essential \
        curl \
        cmake \
        git \
        pkg-config

# Compile DCM2NIIX from source
ENV DCM2NIIX_VERSION=v1.0.20210317
RUN mkdir /src && cd /src \
    && curl -LSsf https://github.com/rordenlab/dcm2niix/archive/refs/tags/$DCM2NIIX_VERSION.tar.gz \
    | tar zx \
    && cd dcm2niix-* && mkdir build \
    && cd build && cmake .. && make -j4 && make install DESTDIR=/target

# Install mri_conv
RUN mkdir -p /target/opt/nifti-converters/mriconverter \
    && cd       /target/opt/nifti-converters/mriconverter \
    && curl -LSsf https://github.com/populse/mri_conv/archive/refs/heads/master.tar.gz \
    | tar zx --strip-components 1 \
    && chmod 0777 . MRIFileManager/MRIManager.jar


FROM base_image as final

# NOTE: using bookworm-proposed-updates because of https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=1039472
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    echo "deb http://deb.debian.org/debian bookworm-proposed-updates main" >> /etc/apt/sources.list \
    && apt-get update -qq \
    && apt-get install -qqy openjdk-17-jre ca-certificates-java

# xvfb+gtk2 needed by mri_conv (headless mode not supported by DicomToNifti)
# see: https://populse.github.io/mri_conv/Installation/installation.html#scriptwithoutGUI
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qqy \
    && apt-get install -qqy \
    libgdcm-tools \
    locales \
    locales-all \
    jq \
    libgtk2.0-0 \
    pigz \
    xvfb

# Copy converters files
COPY --link --chmod=0755 external/dcm2nii/linux/31MARCH2008/dcm2nii /opt/nifti-converters/dcm2nii_2008-03-31
COPY --link --chmod=0755 external/dcm2nii/linux/dcm2nii             /opt/nifti-converters/dcm2nii_2014-08-04
COPY --link --chmod=0755 external/mcverter/linux/mcverter_*         /opt/nifti-converters/
COPY --link external/mcverter/linux/lib/lib*.so.*                   /usr/local/lib/x86_64-linux-gnu/
RUN mkdir -m 1777 \
    /.dcm2nii_2008-03-31 \
    /.dcm2nii_2014-08-04

# install animaConvertImage to convert Analyze format into nifti
COPY --link --chmod=0755 external/anima/animaConvertImage /usr/local/bin/

# install the binaries built in the 'builder' & 'conda' stages
COPY --link --from=builder /target/. /
COPY --link --from=conda   /opt/miniconda3 /opt/miniconda3

# update the ld cache (so that the new libraries can be loaded)
# and make the log dir
RUN ldconfig \
    && mkdir -pv /var/log/shanoir-ng-logs

# install the microservice
COPY --link nifti-conversion.jar nifti-conversion.jar
COPY --link entrypoint entrypoint_common oneshot /bin/

ENV LC_ALL fr_FR.UTF-8
ENV LANG fr_FR.UTF-8
ENV LANGUAGE fr_FR.UTF-8
ENV PATH="/opt/miniconda3/bin:${PATH}"

ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/./urandom", "-Djavax.net.ssl.trustStorePassword=changeit", "-jar", "/nifti-conversion.jar"]
