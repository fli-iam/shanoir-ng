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

echo "Creating databases, starting..."
mysql -uroot -ppassword -e "CREATE DATABASE users"
mysql -uroot -ppassword -e "CREATE DATABASE studies"
mysql -uroot -ppassword -e "CREATE DATABASE import"
mysql -uroot -ppassword -e "CREATE DATABASE datasets"
mysql -uroot -ppassword -e "CREATE DATABASE preclinical"
mysql -uroot -ppassword -e "CREATE DATABASE migrations"
echo "Creating databases, finished."
