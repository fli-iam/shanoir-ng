#!/bin/bash
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

# Creates the database users of the microservices, with the names and passwords set in .env
# (passed by docker compose). Runs only when the database volume is first initialised.
#
# Least privilege: each microservice user only gets its own schema. The migrations user gets the
# six Shanoir schemas (migrations, and the statistics procedures it creates: they run with its
# rights, so the services only need EXECUTE, included in their schema rights). No global
# privileges (FILE, SUPER, CREATE USER, ...).

set -eo pipefail

SERVICES="studies users import datasets preclinical migrations"

# value of the setting of a service, eg: setting_of studies USER -> $SHANOIR_STUDIES_DB_USER
setting_of()
{
	var="SHANOIR_$(echo "$1" | tr '[:lower:]' '[:upper:]')_DB_$2"
	printf '%s' "${!var}"
}

# schema of a service, eg: SHANOIR_STUDIES_DB_NAME (default: the service name)
schema_of()
{
	name="$(setting_of "$1" NAME)"
	printf '%s' "${name:-$1}"
}

# escape a value for a backquoted SQL identifier
sql_identifier()
{
	# shellcheck disable=SC2016 # literal backquotes
	printf '%s' "$1" | sed -e 's/`/``/g'
}

# escape a value for a single-quoted SQL string
sql_string()
{
	printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e "s/'/''/g"
}

for service in $SERVICES ; do
	for setting in USER PASSWORD ; do
		if [ -z "$(setting_of "$service" "$setting")" ] ; then
			echo "error: SHANOIR_$(echo "$service" | tr '[:lower:]' '[:upper:]')_DB_$setting is not set" >&2
			exit 1
		fi
	done
done

echo "Creating database users, starting..."
for service in $SERVICES ; do
	user="$(sql_string "$(setting_of "$service" USER)")"
	echo "CREATE USER '$user'@'%' IDENTIFIED BY '$(sql_string "$(setting_of "$service" PASSWORD)")';"
	if [ "$service" = migrations ] ; then
		schemas=""
		for s in $SERVICES ; do
			schemas="$schemas $(schema_of "$s")"
		done
	else
		schemas="$(schema_of "$service")"
	fi
	for schema in $schemas ; do
		echo "GRANT ALL ON \`$(sql_identifier "$schema")\`.* TO '$user'@'%';"
	done
done | MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -uroot
echo "Creating database users, finished."
