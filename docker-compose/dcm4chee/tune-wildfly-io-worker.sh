# Sourced by /docker-entrypoint.sh as a WILDFLY_INIT script: runs after the base
# configuration has been copied into $JBOSS_HOME/standalone but before Wildfly
# boots. Not meant to be run standalone -- relies on $JBOSS_HOME being exported
# by the parent script (see entrypoint-wrapper.sh).
#
# dcm4chee-arc.xml ships with an unbounded Undertow IO worker
# (<worker name="default"/>), so io-threads/task-max-threads default to values
# computed from the container's visible CPU count -- under-provisioned on a
# CPU-constrained host, and not controllable via env vars unlike the PacsDS
# pool (WILDFLY_PACSDS_MAX_POOL_SIZE). This pins explicit, tunable values.
CONF="$JBOSS_HOME/standalone/configuration/dcm4chee-arc.xml"
if [ -f "$CONF" ] && ! grep -q 'io-threads=' "$CONF"; then
    sed -i 's#<worker name="default"/>#<worker name="default" io-threads="${env.WILDFLY_IO_THREADS:16}" task-max-threads="${env.WILDFLY_IO_TASK_MAX_THREADS:200}"/>#' "$CONF"
fi
