FROM shanoir-ng/base

RUN apt-get --no-install-recommends -qqy install nginx-full

# static files
COPY files/. /

# webapp/ is generated dynamically. We copy it separately from files/ because
# if not present, we do not want the error to be silently ignored
COPY webapp/ /etc/nginx/html/

CMD ["run-nginx"]
EXPOSE 80
