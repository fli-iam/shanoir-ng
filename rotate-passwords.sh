#!/bin/sh
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

# Applies the passwords of .env to the accounts of an existing install, and limits the rights of
# the database accounts to their own schema, as a new install has them. The databases and LDAP
# only read these passwords when their volume is created: without this, an existing install keeps
# its old passwords (and its accounts with ALL ON *.*), and refuses the new ones.
#
#  - database           root, and the account of each microservice (own schema only)
#  - keycloak-database  root, and the account of Keycloak (keycloak schema only)
#  - dcm4chee-database  the PACS account                     } only with the PACS
#  - ldap               the LDAP root and config accounts    } (COMPOSE_PROFILES=full)
#
# Usage, during a maintenance (see README, "CHANGE THE PASSWORDS OF AN EXISTING INSTALL"):
#   docker compose down               # NOT -v
#   (set the new values in .env)
#   ./rotate-passwords.sh
#   ./deploy.sh                       # recreates the other services with the new passwords
#
# The current passwords are found automatically when they are the old defaults ('password' for
# the MariaDB roots, 'secret' for LDAP) or already the new ones (running it again is harmless).
# Otherwise give them in:
#   SHANOIR_OLD_DB_ROOT_PASSWORD, SHANOIR_OLD_KEYCLOAK_DB_ROOT_PASSWORD,
#   SHANOIR_OLD_PACS_LDAP_CONFIG_PASSWORD
# The PACS database needs no old password. The account names must not change.

set -e

cd "$(dirname "$0")"

./check-env.sh

services=$(docker compose config --services)

# rotate <service> <shell> <script> [VAR=NAME ...]: starts the service with the current .env, then
# runs the script inside it. Old passwords are passed by name (-e VAR), never on a command line.
rotate()
{
	service=$1 shell=$2 script=$3
	shift 3
	echo "$service:"
	if ! out=$(docker compose up -d --wait "$service" 2>&1) ; then
		echo "$out" >&2
		echo "error: $service did not start" >&2
		exit 1
	fi
	env_args=""
	for pair in "$@" ; do
		inner=${pair%%=*} outer=${pair#*=}
		eval "value=\${$outer:-}"
		if [ -n "$value" ] ; then
			export "$inner=$value"
			env_args="$env_args -e $inner"
		fi
	done
	# shellcheck disable=SC2086 # env_args is a list of options
	docker compose exec -T $env_args "$service" "$shell" -c "$(cat "$script")"
}

rotate database bash docker-compose/rotate-passwords/mariadb.sh \
	OLD_ROOT_PASSWORD=SHANOIR_OLD_DB_ROOT_PASSWORD
rotate keycloak-database bash docker-compose/rotate-passwords/mariadb.sh \
	OLD_ROOT_PASSWORD=SHANOIR_OLD_KEYCLOAK_DB_ROOT_PASSWORD

if echo "$services" | grep -qx dcm4chee-database ; then
	rotate dcm4chee-database sh docker-compose/rotate-passwords/postgres.sh
fi
if echo "$services" | grep -qx ldap ; then
	rotate ldap sh docker-compose/rotate-passwords/ldap.sh \
		OLD_CONFIG_PASSWORD=SHANOIR_OLD_PACS_LDAP_CONFIG_PASSWORD
fi
if ! echo "$services" | grep -qx ldap ; then
	echo "(PACS not enabled: dcm4chee-database and ldap skipped, see COMPOSE_PROFILES)"
fi

echo "done: now run ./deploy.sh to start the other services with the new passwords"
