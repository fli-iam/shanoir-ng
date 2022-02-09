#!/bin/bash

if [ -n "${PORT}" ]
  then
    echo "Changing port to ${PORT}..."
    sed -i -e "s/listen 80/listen ${PORT}/g" /etc/nginx/conf.d/default.conf
fi

echo "Changes for Shanoir-NG on OHIF Viewer..."

set -e

. /bin/entrypoint_common

require SHANOIR_URL_SCHEME
require SHANOIR_URL_HOST
require VIEWER_URL_SCHEME
require VIEWER_URL_HOST

sed -i "s/SHANOIR_URL_SCHEME/$SHANOIR_URL_SCHEME/g" /usr/share/nginx/html/app-config.js

sed -i "s/SHANOIR_URL_HOST/$SHANOIR_URL_HOST/g" /usr/share/nginx/html/app-config.js

sed -i "s/VIEWER_URL_SCHEME/$VIEWER_URL_SCHEME/g" /usr/share/nginx/html/app-config.js

sed -i "s/VIEWER_URL_HOST/$VIEWER_URL_HOST/g" /usr/share/nginx/html/app-config.js


echo "Starting Nginx to serve the OHIF Viewer..."

exec "$@"
