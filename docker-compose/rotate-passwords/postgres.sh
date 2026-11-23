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

# Run by rotate-passwords.sh with sh inside the 'dcm4chee-database' container: sets the password
# of the PACS account (POSTGRES_USER) to the value the container was created with (.env).
# Local connections inside the container are trusted (image default), so no old password is
# needed.

set -e

for i in $(seq 60) ; do
	pg_isready -q -U "$POSTGRES_USER" -d "$POSTGRES_DB" && break
	sleep 1
done

if ! psql -q -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c 'SELECT 1' >/dev/null 2>&1 ; then
	echo "error: the account '$POSTGRES_USER' or the database '$POSTGRES_DB' does not exist." \
		"Were they renamed in .env? Put back the existing names." >&2
	exit 1
fi

# escape a value for a double-quoted SQL identifier / a single-quoted SQL string
ident=$(printf '%s' "$POSTGRES_USER" | sed 's/"/""/g')
string=$(printf '%s' "$POSTGRES_PASSWORD" | sed "s/'/''/g")
printf 'ALTER ROLE "%s" PASSWORD '"'"'%s'"'"';\n' "$ident" "$string" \
	| psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"
echo "    $POSTGRES_USER: password set"

# check over the container address (not trusted, like dcm4chee-arc's connections)
ip=$(hostname -i | awk '{print $1}')
if PGPASSWORD="$POSTGRES_PASSWORD" psql -h "$ip" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc 'SELECT 1' >/dev/null 2>&1 ; then
	echo "    checked: new password accepted"
else
	echo "error: $POSTGRES_USER cannot log in with its new password" >&2
	exit 1
fi
