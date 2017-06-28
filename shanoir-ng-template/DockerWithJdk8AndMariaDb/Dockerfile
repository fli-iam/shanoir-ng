# Debian "Jessie" with Java 8 and MariaDB installed.
# Build image with:  docker build -t shanoir-ng/debianjava8mariadb:latest .

FROM debian:jessie

# apt config
#  - use miroir.irisa.fr
#  - add backports repository and use it only for jdk8
ADD apt/. /etc/apt/

# add users shanoir & mysql (with fixed ids)
RUN	groupadd -g 500 -r shanoir && useradd -r -u 500 -g shanoir shanoir &&\
	groupadd -g 501 -r mysql   && useradd -r -u 501 -g mysql   mysql

# install packages (supervisor + java8 + mariadb)
RUN	apt-get update  -qqy	&&\
	DEBIAN_FRONTEND=noninteractive apt-get dist-upgrade -qqy --no-install-recommends	&&\
	DEBIAN_FRONTEND=noninteractive apt-get install -qqy --no-install-recommends supervisor openjdk-8-jdk-headless openjdk-8-jre-headless ca-certificates-java mariadb-server	&&\
	rm -rf /var/lib/apt/lists/*

# supervisor base configuration
COPY supervisord.conf /etc/supervisor/supervisord.conf

# automatic upgrade script
ADD dk /dk

RUN apt-get update && apt-get install patch
COPY patches /tmp/patches
RUN set -e ; for p in /tmp/patches/*.diff ; do patch -F0 -p0 <"$p" ; done

COPY supervisor /etc/supervisor/template.d/
COPY init-* run-* wait-* /usr/local/sbin/

# MS: microservice name (eg: shanoir-ng-users)
# 	Shall be set in the child image. The name of the mysql database is
# 	derived from this variable (with '-' changed into'_')
ENV MS=""

# PROFILE: configuration profile (eg: prod, qualif, dev)
#	Shall be set by the sysadmin
ENV PROFILE=""

CMD ["run-supervisor"]
