echo generating self-signed key and certificate
/usr/bin/openssl genrsa 2048 > /etc/ssl/certs/java/shanoir-ng-nginx.key
/usr/bin/openssl req -new -key /etc/ssl/certs/java/shanoir-ng-nginx.key -batch -subj "/CN=shanoir-ng-nginx" > /etc/ssl/certs/java/shanoir-ng-nginx.csr
/usr/bin/openssl x509 -req -days 730 -in /etc/ssl/certs/java/shanoir-ng-nginx.csr -signkey /etc/ssl/certs/java/shanoir-ng-nginx.key -out /etc/ssl/certs/java/shanoir-ng-nginx.crt
rm /etc/ssl/certs/java/shanoir-ng-nginx.csr
chmod 0600 /etc/ssl/certs/java/

