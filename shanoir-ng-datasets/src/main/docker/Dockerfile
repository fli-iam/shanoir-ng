FROM shanoir-ng/base-ms

# static files
COPY files/. /

# this file is generated dynamically. We copy it separately from files/ because
# if not present, we do not want the error to be silently ignored
COPY shanoir-ng-datasets-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-msapp.jar

# microservice name
ENV MS="shanoir-ng-datasets"
