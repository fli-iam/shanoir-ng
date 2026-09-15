CONF="$JBOSS_HOME/standalone/configuration/dcm4chee-arc.xml"

if [ ! -f "$CONF" ]; then
    echo "tune-wildfly.sh: WARNING - $CONF not found, no WildFly tuning applied" >&2
else
    # Single quotes: the ${env.*} parts must reach the XML verbatim, not be expanded by bash.
    WORKER_ATTRS='io-threads="${env.WILDFLY_IO_THREADS:32}" task-max-threads="${env.WILDFLY_IO_TASK_MAX_THREADS:1200}"'
    LISTENER_ATTR='no-request-timeout="${env.WILDFLY_NO_REQUEST_TIMEOUT:120000}"'

    if grep -q '<worker name="default"' "$CONF"; then
        # Matches both the stock self-closing element and one already patched.
        sed -i -E "s#<worker name=\"default\"[^>]*/>#<worker name=\"default\" $WORKER_ATTRS/>#" "$CONF"
    else
        echo "tune-wildfly.sh: WARNING - '<worker name=\"default\"' not found in $CONF," \
             "io-threads/task-max-threads tuning was NOT applied" >&2
    fi

    if grep -q '<http-listener name="default"' "$CONF"; then
        # Drop any previous no-request-timeout on that element, then add the current one.
        sed -i -E "s# no-request-timeout=\"[^\"]*\"##" "$CONF"
        sed -i -E "s#<http-listener name=\"default\"#<http-listener name=\"default\" $LISTENER_ATTR#" "$CONF"
    else
        echo "tune-wildfly.sh: WARNING - '<http-listener name=\"default\"' not found in $CONF," \
             "no-request-timeout tuning was NOT applied" >&2
    fi

    # Log rotation suffix (SimpleDateFormat, use HH not hh). The server runs on dcm4chee-arc.xml,
    # not standalone.xml. The leading dot separates the date from the log file name.
    sed -i -E "s#(<suffix value=\")[^\"]*(\"/>)#\1.${LOGS_ROTATION:-yyyy-MM-dd}\2#" "$CONF"

    # To verify the values are correctly transmitted
    echo "tune-wildfly.sh: WildFly tuned -" \
         "io-threads=${WILDFLY_IO_THREADS:-32}" \
         "task-max-threads=${WILDFLY_IO_TASK_MAX_THREADS:-1200}" \
         "no-request-timeout=${WILDFLY_NO_REQUEST_TIMEOUT:-120000}ms" \
         "pacsds-max-pool-size=${WILDFLY_PACSDS_MAX_POOL_SIZE:-<image default>}" \
         "log-rotation-suffix=.${LOGS_ROTATION:-yyyy-MM-dd}"
fi
