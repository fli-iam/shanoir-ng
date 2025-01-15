# Shanoir NG - Import, manage and share neuroimaging data
# Copyright (C) 2009-2024 Inria - https://www.inria.fr/
# Contact us on https://project.inria.fr/shanoir/
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html


################ base debian image #########################################

FROM debian:bookworm as base-debian

# - disable the automatic "apt-get clean" command (because we mount
#   /var/cache/apt from an external volume to speed-up the build)
# - run "apt-get update" now (to avoid downloading the lists multiple times)
RUN rm /etc/apt/apt.conf.d/docker-clean \
    && apt-get update -qq



################ common image for the java microservices ###################
# - we use the temurin packages because they are more up-to-date that the
#   debian packages
# - we use the system ssl trust store at /etc/ssl/certs/java/cacerts
#   (rather that temurin's default /etc/ssl/certs/adoptium/cacerts)
#   to keep the location independent of the implementation (in case users need
#   to mount it from an external volume)
FROM base-debian as adoptium-key
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq && apt-get install -qqy wget gnupg
RUN wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | gpg --dearmor > /adoptium.gpg

FROM base-debian as base-debian-with-adoptium
COPY --link --from=adoptium-key /adoptium.gpg /etc/apt/keyrings/
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq && apt install -qqy apt-transport-https ca-certificates \
    && echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg]" \
            "https://packages.adoptium.net/artifactory/deb" \
            "$(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" \
    | tee /etc/apt/sources.list.d/adoptium.list

FROM base-debian-with-adoptium as jdk
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq && apt-get install -qqy temurin-21-jdk maven bzip2 git

FROM base-debian-with-adoptium as base-microservice
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq \
    && apt-get install -qqy temurin-21-jre ca-certificates-java \
    && update-ca-certificates

RUN mkdir -pv /var/log/shanoir-ng-logs

COPY --link \
    common/entrypoint_common \
    common/oneshot \
    /usr/bin/

ENTRYPOINT ["/bin/entrypoint", "java", "-Djava.security.egd=file:/dev/urandom", \
    "-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts", \
    "-Djavax.net.ssl.trustStorePassword=changeit"]


################ datasets ##################################################

FROM alpine as datasets-download

# Installation of dcm4che into /opt/dcm4che to manage the store-scu into the
# PACS dcm4che3, used last version 5.21.0 as available on the 2020-02-14
# https://sourceforge.net/projects/dcm4che/files/dcm4che3/
RUN wget -qO dcm4che-bin.zip \
        https://downloads.sourceforge.net/project/dcm4che/dcm4che3/5.21.0/dcm4che-5.21.0-bin.zip \
    && mkdir -p /target/opt && cd /target/opt \
    && unzip /dcm4che-bin.zip && mv dcm4che-* dcm4che \
    && rm    /dcm4che-bin.zip

FROM base-microservice as datasets

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq \
    && apt-get install -qqy \
    pigz \
    gzip \
    locales \
    locales-all

RUN apt-get install -y iputils-ping


# take care of path
ENV PATH /opt/dcm4che/bin:$PATH
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# install the files from the 'dowloader' stage
COPY --link --from=datasets-download /target/. /
COPY --link datasets/shanoir-ng-datasets.jar shanoir-ng-datasets.jar



COPY --link datasets/entrypoint /usr/bin/
COPY --link datasets/wait-for-solr.sh /usr/bin/

 # Use the below line for remote debugging
#CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9914", "-jar", "/shanoir-ng-datasets.jar"]
CMD ["-jar", "/shanoir-ng-datasets.jar"]





################ import ####################################################

FROM base-microservice as import

COPY --link import/entrypoint /usr/bin/
COPY --link import/shanoir-ng-import.jar shanoir-ng-import.jar

# Use the below line for remote debugging
#CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9913", "-jar", "/shanoir-ng-import.jar"]
CMD ["-jar", "/shanoir-ng-import.jar"]



################ nifti-conversion ##########################################

FROM base-debian as nifti-conversion-conda

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


FROM base-debian as nifti-conversion-builder

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


FROM base-microservice as nifti-conversion

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
    temurin-21-jre \
    pigz \
    xvfb

# Copy converters files
COPY --link --chmod=0755 nifti-conversion/external/dcm2nii/linux/31MARCH2008/dcm2nii /opt/nifti-converters/dcm2nii_2008-03-31
COPY --link --chmod=0755 nifti-conversion/external/dcm2nii/linux/dcm2nii             /opt/nifti-converters/dcm2nii_2014-08-04
COPY --link --chmod=0755 nifti-conversion/external/mcverter/linux/mcverter_*         /opt/nifti-converters/
COPY --link              nifti-conversion/external/mcverter/linux/lib/lib*.so.*      /usr/local/lib/x86_64-linux-gnu/
RUN mkdir -m 1777 \
    /.dcm2nii_2008-03-31 \
    /.dcm2nii_2014-08-04

# install animaConvertImage to convert Analyze format into nifti
COPY --link --chmod=0755 nifti-conversion/external/anima/animaConvertImage /usr/local/bin/

# install the binaries built in the 'builder' & 'conda' stages
COPY --link --from=nifti-conversion-builder /target/. /
COPY --link --from=nifti-conversion-conda   /opt/miniconda3 /opt/miniconda3

# update the ld cache (so that the new libraries can be loaded)
RUN ldconfig

# install the microservice
COPY --link nifti-conversion/nifti-conversion.jar nifti-conversion.jar
COPY --link nifti-conversion/entrypoint /usr/bin/

ENV LC_ALL fr_FR.UTF-8
ENV LANG fr_FR.UTF-8
ENV LANGUAGE fr_FR.UTF-8
ENV PATH="/opt/miniconda3/bin:${PATH}"

CMD ["-jar", "/nifti-conversion.jar"]



################ preclinical ###############################################

FROM base-microservice as preclinical

COPY --link preclinical/shanoir-ng-preclinical.jar shanoir-ng-preclinical.jar
COPY --link preclinical/entrypoint /usr/bin/

# Use the below line for remote debugging
#CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9915", "-jar", "/shanoir-ng-preclinical.jar", "-Xmx6g", "-Xms1g"]
CMD ["-jar", "/shanoir-ng-preclinical.jar", "-Xmx6g", "-Xms1g"]



################ studies ###################################################

FROM base-microservice as studies

COPY --link studies/shanoir-ng-studies.jar shanoir-ng-studies.jar
COPY --link studies/entrypoint /usr/bin/

# Use the below line for remote debugging
#CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9912", "-jar", "/shanoir-ng-studies.jar"]
CMD ["-jar", "/shanoir-ng-studies.jar"]



################ users #####################################################

FROM base-microservice as users

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    apt-get update -qq && apt-get install -qqy openssl

COPY --link users/shanoir-ng-users.jar shanoir-ng-users.jar
COPY --link users/entrypoint /usr/bin/


ENV	\
	kc.admin.client.client.id="admin-cli"	\
	kc.admin.client.realm="master"

# Use the below line for remote debugging
#CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:9911", "-jar", "/shanoir-ng-users.jar", "--syncAllUsersToKeycloak=true"]
CMD ["-jar", "/shanoir-ng-users.jar"]



################ keycloak ##################################################

FROM quay.io/keycloak/keycloak:25.0.4 as keycloak-base

# keycloak options (https://www.keycloak.org/server/all-config)
#
# Here we list only the variables that need to be set at both runtime *and* at
# buildtime ("kc.sh build"). If they do not have the same values then the
# quarkus image has to be rebuilt at runtime, which delays container startup
# with a message like: "(Quarkus augmentation completed in 180315ms)"
#
# The relevant variables can be listed with: /opt/keycloak/bin/kc.sh show-config
#
# All other variables are set in the entrypoint.
ENV KC_DB="mysql" \
    KC_LOG="console,file" \
    KC_HTTP_RELATIVE_PATH="/auth"

#
# Use builder to integrate custom provider
#
FROM keycloak-base as keycloak-builder

COPY keycloak/shanoir-ng-keycloak-auth.jar /opt/keycloak/providers

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build

#
# Create actual image, based on builder before
#
FROM keycloak-base as keycloak

COPY --from=keycloak-builder /opt/keycloak/ /opt/keycloak/
COPY --chown=keycloak keycloak/themes/. /opt/keycloak/themes
COPY keycloak/cfg/. /opt/keycloak/
RUN  mkdir /opt/keycloak/data/log

COPY common/entrypoint_common /usr/bin/
COPY keycloak/entrypoint      /usr/bin/

ENTRYPOINT ["/bin/entrypoint", "/opt/keycloak/bin/kc.sh"]
CMD []



################ nginx #####################################################

FROM ohif/app:v3.9.0-beta.76 AS nginx-viewer

FROM nginx as nginx

COPY --link \
    nginx/http.conf \
    nginx/https.conf \
    nginx/nginx.conf \
    nginx/shanoir.template.conf \
    nginx/shanoir.template.dev.conf \
    nginx/shanoir.template.prod.conf \
    /etc/nginx/

COPY --link \
    nginx/viewer/app-config.js \
    nginx/viewer/ohif-viewer.template.conf \
    /etc/nginx/viewer/
COPY --link --from=nginx-viewer /usr/share/nginx/html/. /etc/nginx/viewer/html/

COPY --link common/entrypoint_common /usr/bin/
COPY --link nginx/entrypoint         /usr/bin/

COPY --link nginx/webapp/ /etc/nginx/html/

ENTRYPOINT ["/bin/entrypoint"]
CMD ["nginx", "-g", "daemon off;"]



################ front-dev #################################################

FROM node:lts-alpine3.21 as front-dev

WORKDIR /app

RUN npm install -g @angular/cli@19.0.1



################ database ##################################################

FROM mysql/mysql-server:5.7 as database

COPY --link --chmod=0755 database/shanoir-entrypoint.sh /
ENTRYPOINT ["/shanoir-entrypoint.sh"]
CMD ["mysqld"]

COPY --link --chmod=0755 \
    database/1_create_databases.sh \
    database/2_add_users.sql \
    database/3_add_statistics_procedure.sql \
    database/4_add_studyStatistics_procedure.sql \
    /docker-entrypoint-initdb.d/
 
COPY --link database/db-changes /opt/db-changes



################ keycloak-database ##################################################

FROM mysql/mysql-server:5.7 as keycloak-database

COPY --link --chmod=0755 keycloak-database/1_add_users.sql /docker-entrypoint-initdb.d/



################ solr ###############################################################

FROM solr:8.1 as solr

USER root
RUN mkdir -p /etc/shanoir-core-template \
    && chown solr:solr /etc/shanoir-core-template
USER solr

COPY --link solr/core /etc/shanoir-core-template/
COPY --link solr/entrypoint /usr/bin/

ENTRYPOINT ["/usr/bin/entrypoint"]
CMD ["solr-precreate", "shanoir", "/etc/shanoir-core-template"]

