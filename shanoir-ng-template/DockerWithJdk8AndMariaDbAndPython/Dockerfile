# Debian "Jessie" with Java 8, MariaDB and python installed.
# Build image with:  docker build -t shanoir-ng/debianjava8mariadbpython:latest .

FROM mariadb:10.1

# package containing security keys
RUN apt-get install debian-archive-keyring

# install java8
RUN \
    echo "===> add java8 repository..." && \
    echo "deb http://ftp.de.debian.org/debian jessie-backports main contrib non-free" >> /etc/apt/sources.list && \
    apt-get update && \
    \
    echo "===> install Java" && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --no-install-recommends openjdk-8-jdk-headless openjdk-8-jre-headless ca-certificates-java/jessie-backports

# install python
RUN \
    echo "===> install python" && \
	apt-get install -y --no-install-recommends python python-pip

# supervisor installation
RUN \
    echo "===> install supervisor" && \
	apt-get install -y --no-install-recommends supervisor

RUN \
    echo "===> clean up..." && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# install "pymysql"
RUN pip install pymysql

# supervisor base configuration
COPY supervisord.conf /etc/supervisor/supervisord.conf

# add user shanoir
RUN groupadd -r shanoir && useradd -r -g shanoir shanoir
