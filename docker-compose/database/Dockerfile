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

FROM mysql/mysql-server:5.7

COPY --chmod=0755 /shanoir-entrypoint.sh /
ENTRYPOINT ["/shanoir-entrypoint.sh"]
CMD ["mysqld"]

COPY --chmod=0755 \
    1_create_databases.sh \
    2_add_users.sql \
    3_add_statistics_procedure.sql \
    4_add_studyStatistics_procedure.sql \
    /docker-entrypoint-initdb.d/
 
COPY db-changes /opt/db-changes
