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
		patch python3 zip less vim-tiny procps
COPY utils/* /usr/local/bin/

# automatic upgrade script
ADD dk /dk

# add shanoir-aba.irisa.fr self-signed CA for debugging purpose (TODO: remove)
COPY shanoir-aba /usr/local/share/ca-certificates/

