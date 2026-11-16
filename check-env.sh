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

# Checks the local configuration before a build or a deployment:
#  - .env exists (it is not tracked by git: it is created from .env.example);
#  - every variable of .env.example is also in .env (new releases add variables);
#  - docker compose accepts the configuration (required secrets are set).
# Run from anywhere; uses COMPOSE_FILE and COMPOSE_PROFILES when they are set.

set -e

cd "$(dirname "$0")"

die()
{
	echo "error: $*" >&2
	exit 1
}

env_keys()
{
	sed -n 's/^[[:space:]]*\(export[[:space:]]\{1,\}\)\{0,1\}\([A-Za-z_][A-Za-z0-9_]*\)=.*/\2/p' "$1" | sort -u
}

[ -f .env.example ] || die ".env.example not found in $(pwd)"

if [ ! -f .env ] ; then
	die "no .env in $(pwd).
For a new install: cp .env.example .env, then fill in the secrets (see the comments in .env.example).
For an existing install: restore your .env from its backup. Git deletes it when you pull or switch
to a branch from an older version, where .env was still tracked."
fi

example_keys=$(mktemp)
local_keys=$(mktemp)
trap 'rm -f "$example_keys" "$local_keys"' EXIT
env_keys .env.example > "$example_keys"
env_keys .env > "$local_keys"
missing=$(comm -23 "$example_keys" "$local_keys")
if [ -n "$missing" ] ; then
	die "these variables of .env.example are missing in .env:
$(echo "$missing" | sed 's/^/	/')
Copy them from .env.example into .env and set their values."
fi

docker compose config --quiet \
	|| die "docker compose refused the configuration (see above): set the missing values in .env"
