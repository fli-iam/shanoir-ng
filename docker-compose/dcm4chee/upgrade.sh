#!/bin/bash
#
# Upgrade of an existing PACS from dcm4chee-arc 5.27.0 (slapd-dcm4chee 2.6.2-27.0, PostgreSQL 14)
# to the versions of docker-compose.yml (dcm4chee-arc 5.36.0, slapd-dcm4chee 2.6.15-36.0,
# PostgreSQL 18), following https://github.com/dcm4che/dcm4chee-arc-light/wiki/Upgrade-on-Docker
#
# Run it once, from the root of the repository, after updating the repository and before starting
# the new version. It uses the compose file selected as usual (COMPOSE_FILE, default
# docker-compose.yml). Not needed for a new install.
#
#   docker-compose/dcm4chee/upgrade.sh [backup directory]
#
# Steps:
#  1. stop every service except the LDAP and database of the PACS
#  2. back up into the backup directory (default: pacs-backup-<date>): a dump of the database and
#     an export of the LDAP configuration; copy the WildFly volume to <volume>-5.27
#  3. LDAP: new image on the same volumes, schema update, configuration updates 5.28.0 to 5.36.0
#  4. database: PostgreSQL 18 on a new volume (dcm4chee-postgres-data), restore of the dump,
#     schema updates 5.28 to 5.36
#  5. start every service
#
# The previous database volume (dcm4chee-database-data) and the WildFly copy (<volume>-5.27) are
# kept: remove them once the new version is checked. If a step fails, nothing is started: fix the
# cause, then contact the team before running the script again (steps 3 and 4 are not repeatable
# as such; the backups allow a restore).

set -euo pipefail

PREV_LDAP_IMAGE=dcm4che/slapd-dcm4chee:2.6.2-27.0
PREV_DB_IMAGE=dcm4che/postgres-dcm4chee:14.4-27
PREV_ARC_IMAGE=dcm4che/dcm4chee-arc-psql:5.27.0

# LDAP configuration updates, applied in order (update-data skips releases without changes)
LDAP_UPDATES="5.28.0 5.29.0 5.29.1 5.29.2 5.30.0 5.31.0 5.31.1 5.31.2 5.32.0 5.33.0 5.33.1
	5.34.0 5.34.1 5.34.2 5.34.3 5.35.0 5.35.1 5.35.2 5.36.0"
# database schema updates, applied in order (5.31.1 adds an index missing from update-5.31)
DB_UPDATES="5.28 5.29 5.30 5.31 5.31.1 5.32 5.33 5.34 5.35 5.36"

die() { echo "error: $*" >&2 ; exit 1 ; }
step() { echo ; echo "### $*" ; }

container() {
	local id
	id=$(docker compose ps -a -q "$1")
	[ -n "$id" ] || die "no '$1' container: nothing to upgrade (for a new install, just start the stack)"
	echo "$id"
}
image_of() { docker inspect -f '{{.Config.Image}}' "$1" ; }
volume_of() { docker inspect -f "{{range .Mounts}}{{if eq .Destination \"$2\"}}{{.Name}}{{end}}{{end}}" "$1" ; }

# wait until a command succeeds in a container (at most about 5 minutes)
wait_for() {
	local svc=$1 ; shift
	for _ in $(seq 150) ; do
		docker compose exec -T "$svc" sh -c "$*" >/dev/null 2>&1 && return 0
		sleep 2
	done
	die "'$svc' not ready: see 'docker compose logs $svc'"
}

[ -f docker-compose/dcm4chee/upgrade.sh ] || die "run this script from the root of the repository"

ldap=$(container ldap)
db=$(container dcm4chee-database)
arc=$(container dcm4chee-arc)
[ "$(image_of "$ldap")" = "$PREV_LDAP_IMAGE" ] || die "ldap runs $(image_of "$ldap"), expected $PREV_LDAP_IMAGE"
[ "$(image_of "$db")" = "$PREV_DB_IMAGE" ] || die "dcm4chee-database runs $(image_of "$db"), expected $PREV_DB_IMAGE"
[ "$(image_of "$arc")" = "$PREV_ARC_IMAGE" ] || die "dcm4chee-arc runs $(image_of "$arc"), expected $PREV_ARC_IMAGE"
wildfly_volume=$(volume_of "$arc" /opt/wildfly/standalone)
[ -n "$wildfly_volume" ] || die "cannot find the WildFly volume of dcm4chee-arc"
prev_db_volume=$(volume_of "$db" /var/lib/postgresql/data)

backup=${1:-pacs-backup-$(date +%Y%m%d-%H%M%S)}
[ ! -e "$backup" ] || die "$backup already exists"

step "1. stopping every service except the LDAP and database of the PACS"
mapfile -t others < <(docker compose config --services | grep -vx -e ldap -e dcm4chee-database)
[ ${#others[@]} -eq 0 ] || docker compose stop "${others[@]}"
docker start "$ldap" "$db" >/dev/null
for _ in $(seq 60) ; do
	docker exec "$db" sh -c 'pg_isready -q -U "$POSTGRES_USER" -d "$POSTGRES_DB"' \
		&& docker exec "$ldap" export-data >/dev/null 2>&1 && break
	sleep 2
done

step "2. backup into $backup"
mkdir -p "$backup"
docker exec "$db" sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > "$backup/pacsdb-5.27.sql"
grep -q "PostgreSQL database dump complete" "$backup/pacsdb-5.27.sql" || die "incomplete database dump"
docker exec "$ldap" export-data > "$backup/ldap-data-5.27.ldif"
[ -s "$backup/ldap-data-5.27.ldif" ] || die "empty LDAP export"
docker volume create "$wildfly_volume-5.27" >/dev/null
docker run --rm -v "$wildfly_volume:/from:ro" -v "$wildfly_volume-5.27:/to" --entrypoint sh \
	"$PREV_DB_IMAGE" -c 'cp -a /from/. /to/'
echo "database dump, LDAP export: $backup; WildFly copy: volume $wildfly_volume-5.27"

step "3. LDAP"
# the new WildFly configuration is installed on an empty volume by the new archive image
docker rm -f "$arc" >/dev/null
docker volume rm "$wildfly_volume" >/dev/null
docker rm -f "$ldap" >/dev/null
docker compose up -d --no-deps ldap
wait_for ldap '. setenv.sh && ldapsearch -LLL -xw "$LDAP_ROOTPASS" -D "cn=admin,$LDAP_BASE_DN" -H "$LDAP_URLS" -b "$LDAP_BASE_DN" -s base dn'
docker compose exec -T ldap update-schema
for version in $LDAP_UPDATES ; do
	echo "- configuration update $version"
	docker compose exec -T ldap update-data "$version"
done

step "4. database"
docker rm -f "$db" >/dev/null
docker compose up -d --no-deps dcm4chee-database
# the image creates the tables of the new version on the new volume: replace them by the dump
for _ in $(seq 150) ; do
	docker compose logs dcm4chee-database 2>&1 | grep -q "init process complete" && break
	sleep 2
done
wait_for dcm4chee-database 'pg_isready -q -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
psql() { docker compose exec -T dcm4chee-database sh -c 'psql -v ON_ERROR_STOP=1 -q -U "$POSTGRES_USER" "$POSTGRES_DB" "$@"' psql "$@" ; }
psql -c 'DROP SCHEMA public CASCADE' -c 'CREATE SCHEMA public'
psql < "$backup/pacsdb-5.27.sql"
# databases created before 5.3x name this index UK_..., update-5.35 drops it as IDX...
psql -c 'ALTER INDEX IF EXISTS uk_24av2ewa70e7cykl340n63aqd RENAME TO IDX24av2ewa70e7cykl340n63aqd'
for version in $DB_UPDATES ; do
	echo "- schema update $version"
	psql -f "/sql/update-$version-psql.sql" >/dev/null
done
psql -c 'ANALYZE'

step "5. starting every service"
docker compose up -d

echo
echo "Done. Once the PACS is checked, remove the previous volumes:"
echo "  docker volume rm $prev_db_volume $wildfly_volume-5.27"
