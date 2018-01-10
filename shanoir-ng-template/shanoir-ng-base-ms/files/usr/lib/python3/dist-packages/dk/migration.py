#!/usr/bin/python3

import atexit, collections, os, subprocess, sys, traceback

def die(fmt, *k):
    sys.stderr.write("error:%s\n" % (fmt % k))
    sys.exit(1)

class Failure(Exception):
    pass

class migration:
    __instances = collections.defaultdict(lambda: {})

    def __init__(self, previous, next):
        self.previous = previous
        self.next     = next
        self.func     = None

        assert next not in self.__instances[previous], ("duplicated migration: %s -> %s" % key)

        self.__instances[previous][next] = self

    def __call__(self, func):
        assert self.func is None, "migration function already set"
        self.func = func

    def __str__(self):
        return "migration %r -> %r" % (self.previous, self.next)

    @classmethod
    def run(cls, from_version, to_version):
        # find the shortest migration path for each version

        # version -> (migration, ...)
        routes = {from_version: ()}

        queue = collections.deque([from_version])
        while queue:
            version = queue.popleft()
            for migration in cls.__instances[version].values():
                assert migration.previous == version
                if migration.next not in routes:
                    routes[migration.next] = routes[migration.previous] + (migration,)
                    queue.append(migration.next)
                else:
                    assert len(routes[migration.next]) <= len(routes[migration.previous]) + 1

        fmt_route = lambda r: from_version + "".join(" -> %s" % m.next for m in r)

        if routes:
            print ("possible migrations from version %s" % from_version)
            for version in sorted(routes):
                print("    %-8s    %s" % (version, fmt_route(routes[version])))
            print()

        route = routes.get(to_version)
        if not route:
            die("no migration path from version %s to %s", from_version, to_version)

        print("migration path: %s" % fmt_route(route))

        for migration in route:
            print("\n"
                  "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓\n"
                  "┃ applying %-33s ┃\n"
                  "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛" % migration) 
            try:
                # flush stdout/stderr because we may have some interleaving
                sys.stdout.flush()
                sys.stderr.flush()
                migration.func()
            except subprocess.CalledProcessError as e:
                sys.stderr.write("error: %s\n" % e)
                die("migration %s failed", migration)
            except SystemExit as e:
                if e.code:
                    die("migration %s failed", migration)
            except Exception as e:
                traceback.print_exc()
                die("unexpected error in migration %s", migration)

def run():
    atexit.unregister(_guard)
    if len(sys.argv) != 3:
        die("usage: %s SRC_VERSION  DST_VERSION", sys.argv[0])
    migration.run(*sys.argv[1:3])

@atexit.register
def _guard():
    die("dk.migration.run() was not called")
