FROM shanoir-ng/base-ms

# static files
COPY files/. /

# this file is generated dynamically. We copy it separately from files/ because
# if not present, we do not want the error to be silently ignored
COPY shanoir-ng-preclinical-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-msapp.jar

# data folder (for uploading file)
RUN	mkdir /data
RUN chown -R shanoir:shanoir /data
RUN chmod -R 744 /data


# microservice name
ENV MS="shanoir-ng-preclinical"

