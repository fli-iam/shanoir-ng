# Shanoir NG - Import, manage and share neuroimaging data
# Copyright (C) 2009-2019 Inria - https://www.inria.fr/
# Contact us on https://project.inria.fr/shanoir/
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

FROM shanoir-ng/base-ms

# static files
COPY files/. /

# this file is generated dynamically. We copy it separately from files/ because
# if not present, we do not want the error to be silently ignored
COPY shanoir-ng-datasets-0.0.1-SNAPSHOT.jar /opt/shanoir-ng/shanoir-ng-msapp.jar

# microservice name
ENV MS="shanoir-ng-datasets"

# microservice version
LABEL dk.data_version="2.0.1"
