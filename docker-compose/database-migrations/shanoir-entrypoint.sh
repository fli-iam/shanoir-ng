#!/bin/bash
#
# entrypoint for applying db migrations
#
# - migrations are provided as sql scripts : "$DB_CHANGES_DIR/<DBNAME>/<SCRIPT>.sql"
#
# - the 'migrations.migrations' mysql table stores the list of migrations
#   already applied ("<DBNAME>/<SCRIPT>.sql")
#
# - migration are applied in alphabetical order of the filename ("<SCRIPT>.sql")
#
# - on container startup the behaviour is selected by the SHANOIR_MIGRATION
#   environment variable:
#
#   init    -> create the 'migrations' table, populate it with all the
#              migrations in the DB_CHANGES_DIR and exit
#              (to be used for setting up a new shanoir instance)
#
#   auto    -> run the mysqld as pid 1, then initialise or apply migrations in a
#              background process. The mysql daemon is killed in case of
#              migration failure (fail-fast)
#
#   manual  -> apply the migrations manually and exit
#              (to be used when migrating to a newer shanoir version)
#
#   never   -> do not apply anything, just run the myslqd server
#              (to be used for normal operation)
#
#   dev     -> do not apply anything, just run the myslqd server
#              (to be used in development mode: migrations are applied directly
#              by the microservices with spring.jpa.hibernate.ddl-auto=update)

DB_CHANGES_DIR="/opt/db-changes"
DB_INIT_PROCEDURES_DIR="/opt/db-init-procedures"

MIGRATION_DB=migrations
MIGRATION_USER=migrations
MIGRATION_PASSWORD=password
MYSQL_HOST="${MYSQL_HOST:-database}"

HEADER="[Shanoir Entrypoint]"

MYSQL="mysql           -h$MYSQL_HOST -u$MIGRATION_USER -p$MIGRATION_PASSWORD"
MYSQLADMIN="mysqladmin -h$MYSQL_HOST -u$MIGRATION_USER -p$MIGRATION_PASSWORD"

# wait until the mysqld server is ready
wait_mysqld()
{
	echo "$HEADER wait mysqld"
	for i in {30..0} ; do
		if $MYSQLADMIN ping --silent; then
			echo "$HEADER wait mysqld done"
			break
		fi
		echo "$HEADER Waiting for server"
		sleep 1
	done
	if [ "$i" = 0 ] ; then
		echo >&2 "$HEADER Timeout during MySQL init  at $MYSQL_HOST."
		exit 1
	fi
}

# return true if the migrations table exists
check_migrations_db()
{
	local tbs="`$MYSQL "$MIGRATION_DB" -e "SHOW TABLES LIKE 'migrations';"`" || exit 1
	if [ -n "$tbs" ] ; then
		echo "$HEADER migration table exists"
		return 0
	else
		echo "$HEADER migration table does not exist"
		return 1
	fi
}

# list all existing migrations in the DB_CHANGES_DIR
# (in the order they shall be applied)
list_all_migrations()
{
	(cd "$DB_CHANGES_DIR" && find * -name "*.sql" | sort) || exit 1
}

# list the migrations that are already applied (i.e. migrations that are listed
# in the 'migrations' table)
list_applied_migrations()
{
	$MYSQL "$MIGRATION_DB" --skip-column-names -e 'select script from migrations;' || exit 1
}

# initialise the migrations table
# - create the table
# - populate it with the list of migrations in the DB_CHANGES_DIR
init_migrations()
{
	local all_migrations="`list_all_migrations`"

	echo "$HEADER initialising the migrations db..."
	(
		echo '	CREATE TABLE migrations (`script` VARCHAR(127) PRIMARY KEY);
			INSERT INTO migrations VALUES'
		comma=
		for migration in $all_migrations
		do
			echo "	$comma('$migration')"
			comma=,
		done
		echo ';'
	) | $MYSQL "$MIGRATION_DB" || return 1
	echo "$HEADER done migrations init"
	return 0
}

# walk all migrations and apply those which are not yet listed in the migrations table
apply_migrations()
{
	echo "$HEADER applying migrations..."

	# store the already applied migrations into an associative array
	declare -A applied_migrations
	for migration in `list_applied_migrations`
	do
		applied_migrations[$migration]=1
	done

	# process migrations in filename order (alphabetical)
	local db
	local status
	for migration in `list_all_migrations`
	do
		if [ -n "${applied_migrations[$migration]}" ] ; then
			status="already applied"
		else
			echo "    $migration..."

			[[ "$migration" =~ ^([^/]+)/ ]] || return 1
			db="${BASH_REMATCH[1]}"

			if $MYSQL "$db" <"$DB_CHANGES_DIR/$migration" &&
			   $MYSQL "$MIGRATION_DB" -e "INSERT INTO migrations VALUES ('$migration');"
			then
				status=done
			else
				status=fail
			fi
		fi
		printf "    %-64s (%s)\n" "$migration" "$status"

		[ "$status" != fail ] || return 1
	done
	echo "$HEADER done applying migrations"
	return 0
}

# apply all procedures to load on fresh install
apply_init_procedures()
{
  echo "$HEADER applying procedures..."

  for sql_file in $(find "$DB_INIT_PROCEDURES_DIR" -name "*.sql" | sort); do
    echo "    $sql_file..."
    # extract the target DB from the USE statement in the file
    db=$(grep -i '^\s*USE\s' "$sql_file" | head -1 | sed 's/[Uu][Ss][Ee]\s*//;s/;//;s/\s//g')
    if [ -z "$db" ]; then
      echo "$HEADER error: could not determine target DB for $sql_file" >&2
      return 1
    fi
    if ! $MYSQL "$db" < "$sql_file"; then
      echo "$HEADER error: failed to apply $sql_file" >&2
      return 1
    fi
    done
    echo "$HEADER done applying init procedures"
    return 0
}

echo "$HEADER Shanoir NG migrations entrypoint"
echo "$HEADER migration mode '$SHANOIR_MIGRATION'"

case "$SHANOIR_MIGRATION" in
init)
  # init mode
  # - check if the db migrations exists
  #   - if no, initialise the migrations db
  #   - if yes, apply de migrations
  # - exit

  wait_mysqld
  init_migrations
  apply_init_procedures
  exit $?
  ;;
never)
  # TODO: (warn/fail/alter healthcheck) if there are pending migrations
  echo "$HEADER mode '$SHANOIR_MIGRATION': migrations are skipped"
  ;;
auto)
  # automatic mode
  # - init db or apply migrations in a background process
  wait_mysqld
  if check_migrations_db ; then
    apply_migrations
  else
    init_migrations
  fi
  exit $?
  ;;
manual)
  # manual mode
  # - apply migrations
  # - exit
  wait_mysqld
  apply_migrations
  exit $?
  ;;
dev)
  echo "$HEADER mode '$SHANOIR_MIGRATION': nothing to do, exiting"
  exit 0
  ;;
'')
  echo "$HEADER error: SHANOIR_MIGRATION is unset" >&2
  exit 1
  ;;
*)
  echo "$HEADER error: invalid SHANOIR_MIGRATION value: '$SHANOIR_MIGRATION'" >&2
  exit 1
esac

exec /entrypoint.sh "$@"
