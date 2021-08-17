#!/bin/bash
#
# entrypoint for applying db migrations
#
# - migrations are provided as sql scripts : "$DB_CHANGES_DIR/<DBNAME>/<SCRIPT>.sql"
#
# - the 'migrations.migrations' mysql table stores the list of migrations
#   already applied ("<DBNAME>/<SCRIPT>.sql")
#
# - migration are applied in alphebetical order of the filename ("<SCRIPT>.sql")
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

MIGRATION_DB=migrations
MIGRATION_USER=migrations
MIGRATION_PASSWORD=password

HEADER="[Shanoir Entrypoint]"

MYSQL="mysql           -u$MIGRATION_USER -p$MIGRATION_PASSWORD"
MYSQLADMIN="mysqladmin -u$MIGRATION_USER -p$MIGRATION_PASSWORD"

# wait until the mysqld server is ready
wait_mysqld()
{
	echo "$HEADER wait mysqld"
	for i in {30..0} ; do
		if [ -f /mysql-init-complete ] && $MYSQLADMIN ping ; then
			echo "$HEADER wait mysqld done"
			break
		fi
		echo "$HEADER Waiting for server"
		sleep 1
	done
	if [ "$i" = 0 ] ; then
		echo >&2 "$HEADER Timeout during MySQL init."
		exit 1
	fi
}

# send SIGTERM to mysqld and wait for its termination
stop_mysqld()
{
	echo "$HEADER shutting down mysqld"
	kill "$mysqld_pid"
	wait
	echo "$HEADER done"
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
list_all_migrations()
{
	(cd "$DB_CHANGES_DIR" && find * -name "*.sql") || exit 1
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

echo "$HEADER Shanoir NG entrypoint"


# If command starts with an option, then the mysql entrypoint.sh prepend mysqld
if [ "${1:0:1}" = '-' ] ; then
	set -- mysqld "$@"
fi

# Bypass the migration code if not running mysqld
if [ "$1" = mysqld ] ; then
	echo "$HEADER migration mode '$SHANOIR_MIGRATION'"

	case "$SHANOIR_MIGRATION" in
	init)
		# init mode
		# - run mysqld in a background process
		# - check if the db migrations exists
		#   - if no, initialise the migrations db
		#   - if yes, apply de migrations
		# - stop mysqld
		# - exit

		# TODO: clear the db dir?
		/entrypoint.sh "$@" &
		mysqld_pid=$!
		wait_mysqld
		trap stop_mysqld EXIT
		init_migrations
		exit $?
		;;
	never)
		# TODO: (warn/fail/alter healthcheck) if there are pending migrations
		;;
	auto)
		# automatic mode
		# - run mysqld as PID 1
		# - init db or apply migrations in a background process
		(
			wait_mysqld
			if check_migrations_db ; then
				apply_migrations
			else
				init_migrations
			fi
			if [ $? -ne 0 ] ; then
				# fail fast
				echo "migration failed, killing the mysql daemon..."
				kill 1
			fi
		)&
		;;
	manual)
		# manual mode
		# - run mysqld in a background process
		# - apply migrations
		# - stop mysqld
		# - exit
		/entrypoint.sh "$@" &
		mysqld_pid=$!
		wait_mysqld
		trap stop_mysqld EXIT
		apply_migrations
		exit $?
		;;
	
	dev)
		;;

	'')
		echo "$HEADER error: SHANOIR_MIGRATION is unset" >&2
		exit 1
		;;
	*)
		echo "$HEADER error: invalid SHANOIR_MIGRATION value: '$SHANOIR_MIGRATION'" >&2
		exit
	esac
fi

exec /entrypoint.sh "$@"
