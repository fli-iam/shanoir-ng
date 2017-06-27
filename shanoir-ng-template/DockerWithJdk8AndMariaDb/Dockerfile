# Debian "Jessie" with Java 8 and MariaDB installed.
# Build image with:  docker build -t shanoir-ng/debianjava8mariadb:latest .

FROM debian:jessie

# apt config
#  - use miroir.irisa.fr
#  - add backports repository and use it only for jdk8
ADD apt/. /etc/apt/


# install packages (supervisor + java8 + mariadb)
RUN	apt-get update  -qqy	&&\
	DEBIAN_FRONTEND=noninteractive apt-get install -qqy --no-install-recommends supervisor openjdk-8-jdk-headless openjdk-8-jre-headless ca-certificates-java mariadb-server	&&\
	rm -rf /var/lib/apt/lists/*

# supervisor base configuration
COPY supervisord.conf /etc/supervisor/supervisord.conf

# add user shanoir
RUN groupadd -r shanoir && useradd -r -g shanoir shanoir
