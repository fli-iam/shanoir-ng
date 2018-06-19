FROM debian:jessie

RUN set -ex && \
    echo 'deb http://deb.debian.org/debian jessie-backports main' \
      > /etc/apt/sources.list.d/jessie-backports.list && \
    apt update -y && \
    apt install -t \
      jessie-backports \
      openjdk-8-jre-headless \
      ca-certificates-java -y

RUN apt-get update -qq \
    && apt-get install -y \
    git \
    curl \
    build-essential \
    cmake \
    pkg-config \
    libgdcm-tools=2.4.4-3+deb8u1 \
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

RUN mkdir -pv /var/log/shanoir-ng-logs

ADD shanoir-ng-import.jar shanoir-ng-import.jar

# Use the below line for remote debugging
#ENTRYPOINT ["java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=9913,suspend=y", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-import.jar"]
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/shanoir-ng-import.jar"]
