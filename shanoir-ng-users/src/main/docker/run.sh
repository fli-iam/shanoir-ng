#!/bin/bash

if [ ! -d "/vol/log/supervisor" ]; then
  echo "Create supervisor log directory"
  mkdir -p /vol/log/supervisor
fi

supervisord -c /etc/supervisor/supervisord.conf