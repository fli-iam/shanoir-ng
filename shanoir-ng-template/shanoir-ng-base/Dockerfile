# Base debian image for all shanoir-ng containers
# Build image with:  docker build -t shanoir-ng/base .

FROM debian:stretch

# apt config
#  - use miroir.irisa.fr
#  - add backports repository (but do not use it by default)
ADD apt/. /etc/apt/

# install some utilities
RUN	apt-get -qq update	&&\
	DEBIAN_FRONTEND=noninteractive apt-get dist-upgrade -qqy --no-install-recommends	&&\
	DEBIAN_FRONTEND=noninteractive apt-get -qqy --no-install-recommends install	\
		patch python3 zip unzip less vim-tiny procps netcat-openbsd		\
		curl logrotate locales

# static files
COPY files/. /

# generate locales (listed in /etc/locale.gen)
RUN locale-gen

# Environment varialbes common for all containers.
# The default values are meant for development. 
#
# DOCKER_PREFIX: prefix used in the container names,
#	eg:	"prod"	 for prod-shanoir, prod-ng-users, ...
#	eg:	"qualif" for qualif-shanoir, qualif-ng-users, ...
#
# PROFILE: runtime configuration profile
#	(dev, prod or qualif)
#
# SHANOIR_SERVER_URL: base url of the shanoir service
#	(without a trailing slash)
#
# SHANOIR_INSTANCE: name of this shanoir instance (eg: Neurinfo, Ofsep, Qualif
# 	Neurinfo, ...) as displayed to the user
#	
# SHANOIR_ADMIN_NAME <SHANOIR_ADMIN_EMAIL>
#	sender identity for outgoing mail
#
# SMTP_HOST, SMTP_PORT:	local SMTP relay for outgoing mail
#
# LANG: default locale for the container (it is important to set it because
# 	without locale python3 falls back to ascii and will not accept any
# 	UTF-8 IOs or source files)
#
ENV	DOCKER_PREFIX="shanoir"			\
	PROFILE=""				\
	SHANOIR_SERVER_URL="http://localhost"	\
	SHANOIR_INSTANCE="DEV"			\
	SHANOIR_ADMIN_NAME="Shanoir admin"	\
	SHANOIR_ADMIN_EMAIL="shanoir@localhost"	\
	SMTP_HOST="localhost"			\
	SMTP_PORT="25"				\
	LANG="en_US.UTF-8"


