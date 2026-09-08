#!/bin/bash
# Wraps the stock dcm4chee-arc-psql entrypoint to register our WILDFLY_INIT script
# (see tune-wildfly-io-worker.sh) without pointing WILDFLY_INIT directly at the
# bind-mounted repo file: /docker-entrypoint.sh renames whatever WILDFLY_INIT
# points to into "<file>.done" once it has run, which would rename our tracked
# file on the host if mounted read-write, or fail under set -e if mounted :ro.
# Copying it into a scratch path first sidesteps both problems, and re-copying
# on every start keeps it working across container recreates.
set -e

mkdir -p /tmp/shanoir-wildfly-init
cp /opt/shanoir/tune-wildfly-io-worker.sh /tmp/shanoir-wildfly-init/tune-wildfly-io-worker.sh
export WILDFLY_INIT="/tmp/shanoir-wildfly-init/tune-wildfly-io-worker.sh${WILDFLY_INIT:+ $WILDFLY_INIT}"

exec /docker-entrypoint.sh "$@"
