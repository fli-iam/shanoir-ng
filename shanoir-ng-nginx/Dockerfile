FROM shanoir-ng/base

RUN apt-get --no-install-recommends -qqy install nginx-light

COPY nginx.conf /etc/nginx/nginx.conf
COPY run-nginx /usr/local/sbin/

COPY webapp/ /etc/nginx/html/

CMD ["run-nginx"]
EXPOSE 80
