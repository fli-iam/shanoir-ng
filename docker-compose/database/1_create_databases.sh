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

: ${SHANOIR_USERS_DB_NAME:=users}
: ${SHANOIR_STUDIES_DB_NAME:=studies}
: ${SHANOIR_IMPORT_DB_NAME:=import}
: ${SHANOIR_DATASETS_DB_NAME:=datasets}
: ${SHANOIR_PRECLINICAL_DB_NAME:=preclinical}
: ${SHANOIR_MIGRATIONS_DB_NAME:=migrations}

echo "Creating databases, starting..."
mariadb -uroot -ppassword -e "CREATE DATABASE ${SHANOIR_USERS_DB_NAME}"
mariadb -uroot -ppassword -e "CREATE DATABASE ${SHANOIR_STUDIES_DB_NAME}"
mariadb -uroot -ppassword -e "CREATE DATABASE ${SHANOIR_IMPORT_DB_NAME}"
mariadb -uroot -ppassword -e "CREATE DATABASE ${SHANOIR_DATASETS_DB_NAME}"
mariadb -uroot -ppassword -e "CREATE DATABASE ${SHANOIR_PRECLINICAL_DB_NAME}"
mariadb -uroot -ppassword -e "CREATE DATABASE ${SHANOIR_MIGRATIONS_DB_NAME}"
echo "Creating databases, finished."
