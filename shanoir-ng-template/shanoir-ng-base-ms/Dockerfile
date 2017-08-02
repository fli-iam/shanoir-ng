# Debian "Stretch" with Java 8 and MariaDB installed.
# Build image with:  docker build -t shanoir-ng/base-ms .

FROM shanoir-ng/base

# add users shanoir & mysql (with fixed ids)
RUN	groupadd -g 500 -r shanoir && useradd -r -u 500 -g shanoir shanoir &&\
	groupadd -g 501 -r mysql   && useradd -r -u 501 -g mysql   mysql

# install packages (supervisor + java8 + mariadb)
RUN	apt-get update  -qqy	&&\
	DEBIAN_FRONTEND=noninteractive apt-get install -qqy --no-install-recommends supervisor openjdk-8-jdk-headless openjdk-8-jre-headless ca-certificates-java mariadb-server	&&\
	rm -rf /var/lib/apt/lists/*

# supervisor base configuration
COPY supervisord.conf /etc/supervisor/supervisord.conf

COPY patches /tmp/patches
RUN set -e ; for p in /tmp/patches/*.diff ; do patch -F0 -p0 <"$p" ; done

COPY supervisor /etc/supervisor/template.d/
# install commands
COPY commands/* /usr/local/sbin/

# MS: microservice name (eg: shanoir-ng-users)
# 	Shall be set in the child image. The name of the mysql database is
# 	derived from this variable (with '-' changed into'_')
ENV MS=""

# PROFILE: configuration profile (eg: prod, qualif, dev)
#	Shall be set by the sysadmin
ENV PROFILE=""

CMD ["run-supervisor"]

