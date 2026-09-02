#!/usr/bin/env python3

import os
import re
import subprocess
from pathlib import Path

ROOT = Path("docker-compose/database-migrations/db-changes")

DATABASES = {
    "datasets",
    "import",
    "preclinical",
    "studies",
    "users",
}

MIGRATION_PATTERN = re.compile(r"^(\d{4})_(.+\.sql)$")


def git(*args: str) -> str:
    return subprocess.check_output(
        ["git", *args],
        text=True,
    ).strip()


base_sha = os.environ["BASE_SHA"]
merge_sha = os.environ["MERGE_SHA"]

changed_files = git(
    "diff",
    "--diff-filter=A",
    "--name-only",
    base_sha,
    merge_sha,
    "--",
    str(ROOT),
).splitlines()

new_migrations: dict[str, list[Path]] = {
    database: [] for database in DATABASES
}

for filename in changed_files:
    path = Path(filename)

    if len(path.parts) < len(ROOT.parts) + 2:
        continue

    database = path.parts[len(ROOT.parts)]

    if database not in DATABASES:
        continue

    if MIGRATION_PATTERN.match(path.name):
        new_migrations[database].append(path)


for database, migrations in new_migrations.items():
    if not migrations:
        continue

    migrations.sort(
        key=lambda path: int(MIGRATION_PATTERN.match(path.name).group(1))
    )

    directory = ROOT / database
    new_paths = set(migrations)

    existing_numbers = []

    for path in directory.glob("*.sql"):
        if path in new_paths:
            continue

        match = MIGRATION_PATTERN.match(path.name)

        if match:
            existing_numbers.append(int(match.group(1)))

    next_number = max(existing_numbers, default=0) + 1

    for path in migrations:
        if not path.exists():
            continue

        match = MIGRATION_PATTERN.match(path.name)
        suffix = match.group(2)

        new_path = path.with_name(f"{next_number:04d}_{suffix}")

        if new_path != path:
            if new_path.exists():
                raise RuntimeError(f"Target already exists: {new_path}")

            print(f"{path} -> {new_path}")
            subprocess.run(
                ["git", "mv", str(path), str(new_path)],
                check=True,
            )

        next_number += 1
