#!/bin/bash

if [ ! -f "/var/run/mysqld/mysqld.sock" ]; then
  echo "Initialize MariaDB command"
  /docker-entrypoint.sh mysqld
fi

if [ ! -d "/vol/log/supervisor" ]; then
  echo "Create supervisor log directory"
  mkdir -p /vol/log/supervisor
fi

supervisord -c /etc/supervisor/supervisord.conf