# Debian "Jessie" with Java 8 and MariaDB installed.
# Build image with:  docker build -t shanoir-ng/debianjava8mariadb:latest .

FROM mariadb:10.1

# package containing security keys
RUN apt-get install debian-archive-keyring

# supervisor installation
RUN \
	apt-get update && \
	apt-get install -y --no-install-recommends supervisor

# install java8
RUN \
    echo "===> add java8 repository..." && \
    echo "deb http://ftp.de.debian.org/debian jessie-backports main contrib non-free" >> /etc/apt/sources.list && \
    apt-get update && \
    \
    echo "===> install Java" && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --no-install-recommends openjdk-8-jdk-headless openjdk-8-jre-headless ca-certificates-java/jessie-backports && \
    \
    echo "===> clean up..." && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# supervisor base configuration
COPY supervisord.conf /etc/supervisor/supervisord.conf
