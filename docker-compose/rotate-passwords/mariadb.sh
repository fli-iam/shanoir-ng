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

# Run by rotate-passwords.sh with bash inside the 'database' or the 'keycloak-database'
# container: sets the passwords and the rights of its accounts to the values the container was
# created with (.env), as a new install would have them:
#  - root: password MARIADB_ROOT_PASSWORD
#  - each account of a service: its password, no global privilege, only its own schema(s),
#    only as '<user>'@'%' (other host variants, eg 'keycloak'@'localhost', are dropped)
# The accounts are those of 2_add_users.sh ('database') or MARIADB_USER on MARIADB_DATABASE
# ('keycloak-database'). They must exist: an account renamed in .env is an error.
# OLD_ROOT_PASSWORD: current root password, when it is neither the new one nor 'password'.

set -eo pipefail

# escape a value for a single-quoted SQL string / a backquoted SQL identifier
sql_string() { printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e "s/'/''/g"; }
sql_identifier() { printf '%s' "$1" | sed -e 's/`/``/g'; }

# log in as root: with the new password (already rotated), the given old one, or the 2.14 default
root_pw=
for pw in "$MARIADB_ROOT_PASSWORD" "${OLD_ROOT_PASSWORD:-}" password ; do
	if [ -n "$pw" ] && MYSQL_PWD="$pw" mariadb -uroot -e 'SELECT 1' >/dev/null 2>&1 ; then
		root_pw="$pw"
		break
	fi
done
if [ -z "$root_pw" ] ; then
	echo "error: cannot log in as root with the password of .env nor with 'password':" \
		"set the current one in the SHANOIR_OLD_..._ROOT_PASSWORD variable (see README)" >&2
	exit 1
fi
root_sql() { MYSQL_PWD="$root_pw" mariadb -uroot "$@"; }

# accounts: "user<TAB>password<TAB>schema schema..."
accounts()
{
	if [ -n "${MARIADB_USER:-}" ] ; then
		# keycloak-database
		printf '%s\t%s\t%s\n' "$MARIADB_USER" "$MARIADB_PASSWORD" "$MARIADB_DATABASE"
		return
	fi
	# database: same accounts and schemas as 2_add_users.sh
	local services="studies users import datasets preclinical migrations" s S var user pw schemas
	for s in $services ; do
		S=$(echo "$s" | tr '[:lower:]' '[:upper:]')
		var="SHANOIR_${S}_DB_USER";     user="${!var}"
		var="SHANOIR_${S}_DB_PASSWORD"; pw="${!var}"
		if [ -z "$user" ] || [ -z "$pw" ] ; then
			echo "error: SHANOIR_${S}_DB_USER or SHANOIR_${S}_DB_PASSWORD is not set" >&2
			return 1
		fi
		if [ "$s" = migrations ] ; then
			schemas=
			for t in $services ; do
				var="SHANOIR_$(echo "$t" | tr '[:lower:]' '[:upper:]')_DB_NAME"
				schemas="$schemas ${!var:-$t}"
			done
		else
			var="SHANOIR_${S}_DB_NAME"; schemas="${!var:-$s}"
		fi
		printf '%s\t%s\t%s\n' "$user" "$pw" "$schemas"
	done
}

list="$(accounts)"

# all accounts must exist before changing anything
while IFS="$(printf '\t')" read -r user pw schemas ; do
	n=$(root_sql -N -e "SELECT COUNT(*) FROM mysql.global_priv WHERE User='$(sql_string "$user")'")
	if [ "$n" = 0 ] ; then
		echo "error: the account '$user' does not exist in this database. Was it renamed in .env?" \
			"Put back the existing name (accounts cannot be renamed)." >&2
		exit 1
	fi
done <<< "$list"

while IFS="$(printf '\t')" read -r user pw schemas ; do
	u="$(sql_string "$user")"
	{
		for host in $(root_sql -N -e "SELECT Host FROM mysql.global_priv WHERE User='$u' AND Host<>'%'") ; do
			echo "DROP USER '$u'@'$(sql_string "$host")';"
		done
		echo "CREATE USER IF NOT EXISTS '$u'@'%' IDENTIFIED BY '$(sql_string "$pw")';"
		echo "ALTER USER '$u'@'%' IDENTIFIED BY '$(sql_string "$pw")';"
		echo "REVOKE ALL PRIVILEGES, GRANT OPTION FROM '$u'@'%';"
		for schema in $schemas ; do
			echo "GRANT ALL ON \`$(sql_identifier "$schema")\`.* TO '$u'@'%';"
		done
	} | root_sql
	echo "    $user: password set, rights limited to:$(printf ' %s' $schemas)"
done <<< "$list"

# root last: the connection above uses its current password
for host in $(root_sql -N -e "SELECT Host FROM mysql.global_priv WHERE User='root'") ; do
	echo "ALTER USER 'root'@'$(sql_string "$host")' IDENTIFIED BY '$(sql_string "$MARIADB_ROOT_PASSWORD")';"
done | root_sql
echo "    root: password set"

# check: every account logs in with its new password, over the network like the services,
# and has no global privilege
ip=$(hostname -i | awk '{print $1}')
failed=0
while IFS="$(printf '\t')" read -r user pw schemas ; do
	if ! global=$(MYSQL_PWD="$pw" mariadb -h"$ip" -u"$user" -N -e 'SHOW GRANTS' 2>/dev/null) ; then
		echo "error: $user cannot log in with its new password" >&2
		failed=1
	elif echo "$global" | grep 'ON \*\.\*' | grep -vq '^GRANT USAGE ON' ; then
		echo "error: $user still has global privileges" >&2
		failed=1
	fi
done <<< "$list"
if ! MYSQL_PWD="$MARIADB_ROOT_PASSWORD" mariadb -uroot -e 'SELECT 1' >/dev/null 2>&1 ; then
	echo "error: root cannot log in with its new password" >&2
	failed=1
fi
[ "$failed" = 0 ] && echo "    checked: new passwords accepted, no global privileges"
exit "$failed"
