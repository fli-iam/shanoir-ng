# Debian "Jessie" with Java 8 installed.
# Build image with:  docker build -t shanoir-ng/debianjava8:latest .

FROM debian:jessie

# supervisor installation
RUN \
	apt-get update && \
	apt-get install -y --no-install-recommends supervisor

# add java8
RUN \
    echo "===> add java8 repository..."  && \
    echo "deb http://ftp.de.debian.org/debian jessie-backports main" >> /etc/apt/sources.list  && \
    apt-get update  && \
    \
    echo "===> install Java"  && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --no-install-recommends openjdk-8-jdk  && \
    \
    echo "===> clean up..."  && \
    apt-get clean  && \
    rm -rf /var/lib/apt/lists/*

# supervisor base configuration
COPY supervisord.conf /etc/supervisor/supervisord.conf

# add user shanoir
RUN groupadd -r shanoir && useradd -r -g shanoir shanoir
