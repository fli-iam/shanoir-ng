#!/bin/bash
#Modifying the dcm4chee container's entrypoint with modifying the image, avoiding folder readOnly issues

set -e

export WILDFLY_IO_THREADS="${WILDFLY_IO_THREADS:-32}"
export WILDFLY_IO_TASK_MAX_THREADS="${WILDFLY_IO_TASK_MAX_THREADS:-1200}"
export WILDFLY_NO_REQUEST_TIMEOUT="${WILDFLY_NO_REQUEST_TIMEOUT:-120000}"

mkdir -p /tmp/shanoir-wildfly-init
cp /opt/shanoir/tune-wildfly-io-worker.sh /tmp/shanoir-wildfly-init/tune-wildfly-io-worker.sh
export WILDFLY_INIT="/tmp/shanoir-wildfly-init/tune-wildfly-io-worker.sh${WILDFLY_INIT:+ $WILDFLY_INIT}"

exec /docker-entrypoint.sh "$@"
