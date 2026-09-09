CONF="$JBOSS_HOME/standalone/configuration/dcm4chee-arc.xml"

if [ ! -f "$CONF" ]; then
    echo "tune-wildfly-io-worker.sh: WARNING - $CONF not found, no Undertow tuning applied" >&2
else
    # Single quotes: the ${env.*} parts must reach the XML verbatim, not be expanded by bash.
    WORKER_ATTRS='io-threads="${env.WILDFLY_IO_THREADS:32}" task-max-threads="${env.WILDFLY_IO_TASK_MAX_THREADS:1200}"'
    LISTENER_ATTR='no-request-timeout="${env.WILDFLY_NO_REQUEST_TIMEOUT:120000}"'

    if grep -q '<worker name="default"' "$CONF"; then
        # Matches both the stock self-closing element and one already patched.
        sed -i -E "s#<worker name=\"default\"[^>]*/>#<worker name=\"default\" $WORKER_ATTRS/>#" "$CONF"
    else
        echo "tune-wildfly-io-worker.sh: WARNING - '<worker name=\"default\"' not found in $CONF," \
             "io-threads/task-max-threads tuning was NOT applied" >&2
    fi

    if grep -q '<http-listener name="default"' "$CONF"; then
        # Drop any previous no-request-timeout on that element, then add the current one.
        sed -i -E "s# no-request-timeout=\"[^\"]*\"##" "$CONF"
        sed -i -E "s#<http-listener name=\"default\"#<http-listener name=\"default\" $LISTENER_ATTR#" "$CONF"
    else
        echo "tune-wildfly-io-worker.sh: WARNING - '<http-listener name=\"default\"' not found in $CONF," \
             "no-request-timeout tuning was NOT applied" >&2
    fi

    # To verify the values are correctly transmitted
    echo "tune-wildfly-io-worker.sh: Undertow tuned -" \
         "io-threads=${WILDFLY_IO_THREADS:-32}" \
         "task-max-threads=${WILDFLY_IO_TASK_MAX_THREADS:-1200}" \
         "no-request-timeout=${WILDFLY_NO_REQUEST_TIMEOUT:-120000}ms" \
         "pacsds-max-pool-size=${WILDFLY_PACSDS_MAX_POOL_SIZE:-<image default>}"
fi
