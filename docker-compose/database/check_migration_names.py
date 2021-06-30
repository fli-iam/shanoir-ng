#!/usr/bin/python3
#
# this script ensures that all new migrations in the current branch have a
# filename that is greater than all existing migrations in the release branch
#


RELEASE_BRANCH="origin/master"

import pathlib
import subprocess
import sys

errors = 0
for path in pathlib.Path("db-changes").glob("*"):
    if path.is_dir():
        db = path.name
        print(f"{30*'#'} {db} {45*'#'}"[:79])

        # get the existing migrations from the RELEASE_BRANCH
        proc = subprocess.run(["git", "ls-tree", f"{RELEASE_BRANCH}/develop:db-changes/{db}"],
                encoding="utf-8", capture_output=True, errors="replace")
        if proc.returncode == 0:
            released_migrations = set(line.split("\t", 1)[1] for line in proc.stdout.splitlines())

        elif "Not a valid object name" in proc.stderr:
            released_migrations = []
            latest_migration = ""
        else:
            sys.stderr.write(proc.stderr)
            proc.check_returncode()
            raise # not reachable

        latest_migration = max(released_migrations, default="")
        print(f"\nlatest migration in {RELEASE_BRANCH}: {latest_migration or '(none)'}\n")

        for migration in sorted(path.glob("*.sql")):
            if migration.name not in released_migrations:
                if migration.name > latest_migration:
                    status = "ok"
                else:
                    status = "FAIL"
                    errors += 1
                print("%-74s %4s" % (migration, status))

        print()

print("#"*79)
if errors:
    print(f"{errors} errors (migrations must be ordered after all migrations in {RELEASE_BRANCH})")
    sys.exit(1)
else:
    print ("success")
