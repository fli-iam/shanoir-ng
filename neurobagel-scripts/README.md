# Neurobagel nodes for Shanoir

These scripts run one [Neurobagel](https://neurobagel.org) node per Shanoir instance on the Neurobagel VM:
today **Neurinfo** and **OFSEP**, and more can be added later.

For each instance, `nb-node.sh` does the following every day from cron:

1. downloads the studies that the Shanoir instance exports to Neurobagel,
2. builds the Neurobagel JSONLD files with the `bagel` CLI,
3. reloads that instance's own node (GraphDB + API), but only when the data changed.

| File | Role |
|---|---|
| `nb-node.sh` | Installs, syncs, manages and removes the nodes |
| `nb.conf.example` | Template of an instance config |
| `query.py` | Queries a node's API, for checks |
| `.gitignore` | Keeps the configs (secrets), data and state folders out of git |

## How the instances are separated

Every command takes the **instance name** as its second argument:

```bash
bash nb-node.sh <command> <instance>
```

Everything that belongs to an instance is derived from its name, so instances never share state:

| | `neurinfo` | `ofsep` | `<name>` |
|---|---|---|---|
| Config | `nb-neurinfo.conf` | `nb-ofsep.conf` | `nb-<name>.conf` |
| Compose project | `shanoir_nb_neurinfo` | `shanoir_nb_ofsep` | `shanoir_nb_<name>` |
| JSONLD folder | `data-neurinfo/` | `data-ofsep/` | `data-<name>/` |
| State folder (log, lock, last inputs) | `state-neurinfo/` | `state-ofsep/` | `state-<name>/` |
| Recipes clone (`.env`, GraphDB passwords) | `~/neurobagel-recipes-neurinfo` | `~/neurobagel-recipes-ofsep` | `~/neurobagel-recipes-<name>` |
| API port (set in the config) | `8000` | `8001` | next free port |
| Daily sync (set in the config) | `04:20` | `04:50` | next free slot |

Each instance has its own Keycloak client secret, GraphDB, API, cron entry and log. If one instance is down,
misconfigured or fails to sync, the others are not affected. Instance names may only contain lowercase
letters, digits, `-` and `_`.

The instances share only the scripts and the Docker images (api, graph, bagel CLI).

## Requirements

On the VM:

- Linux with `bash`, `git`, `curl`, `python3`, `flock`, `ss`
- Docker with the `docker compose` plugin
- Network access to every Shanoir instance, and to GitHub / Docker Hub for the install
- About **2 GB of RAM per instance** for GraphDB (`NB_GRAPH_MEMORY=2G`), plus the API containers
- For cron, the user must be able to run docker **without a password**: either membership in the `docker`
  group, or a `NOPASSWD` sudo rule for `docker` (the script then uses `sudo -n docker`)

On each Shanoir instance:

- A Keycloak client in the `shanoir-ng` realm (default name `neurobagel-export`), with *client
  authentication* and *service accounts* enabled and allowed to call `/shanoir-ng/studies/neurobagel/*`

## Setup

Run this once per instance (here Neurinfo and OFSEP):

```bash
cd neurobagel-scripts

cp nb.conf.example nb-neurinfo.conf && chmod 600 nb-neurinfo.conf
cp nb.conf.example nb-ofsep.conf    && chmod 600 nb-ofsep.conf
# edit both files: SHANOIR_URL, NB_CLIENT_SECRET, API_PORT, CRON_AT (see below)

bash nb-node.sh sync neurinfo           # first run installs the node, then fetches, builds and loads
bash nb-node.sh sync ofsep
bash nb-node.sh cron-install neurinfo   # daily sync
bash nb-node.sh cron-install ofsep
```

Check the result:

```bash
bash nb-node.sh status neurinfo
python3 query.py --port 8000            # datasets on the Neurinfo node
python3 query.py --port 8001            # datasets on the OFSEP node
```

### Instance config (`nb-<name>.conf`)

| Variable | Required | Default | Description |
|---|---|---|---|
| `SHANOIR_URL` | yes | | Base URL of the Shanoir instance, e.g. `https://shanoir.example.org` |
| `NB_CLIENT_SECRET` | yes | | Secret of the instance's Keycloak client |
| `API_PORT` | yes | | Port of the node API, bound to `127.0.0.1` only. **Must be unique per instance.** |
| `CRON_AT` | recommended | `20 4` | Daily sync time (`minute hour`). Give each instance its own slot. |
| `NB_CLIENT_ID` | no | `neurobagel-export` | Keycloak client id |
| `RECIPES` | no | `$HOME/neurobagel-recipes-<name>` | Recipes clone. **Never share it between instances.** |
| `NB_DATA` | no | `./data-<name>` | Folder of the JSONLD files loaded by the node |
| `BAGEL_IMAGE` | no | `neurobagel/bagelcli:latest` | Image used when no `bagel` command is in `PATH` |

Example `nb-ofsep.conf`:

```bash
SHANOIR_URL=https://ofsep-shanoir.example.org
NB_CLIENT_SECRET=...
API_PORT=8001
CRON_AT="50 4"
```

Syncs of different instances can run at the same time, but each one builds JSONLDs and restarts a GraphDB.
Spreading them over separate slots keeps the VM responsive; 30 minutes apart is plenty.

## Adding an instance later

1. Create the Keycloak client on the new Shanoir instance (see [Requirements](#requirements)).
2. Pick a name, a free port and a free cron slot, then write the instance's config:
   ```bash
   cp nb.conf.example nb-<name>.conf && chmod 600 nb-<name>.conf
   # SHANOIR_URL, NB_CLIENT_SECRET, API_PORT=8002, CRON_AT="20 5"
   ```
3. Start it and schedule it:
   ```bash
   bash nb-node.sh sync <name>
   bash nb-node.sh cron-install <name>
   ```
4. Add a column for it to the table in [How the instances are separated](#how-the-instances-are-separated).
   If you run a federation, add the new node's API to it (see [Searching all nodes at once](#searching-all-nodes-at-once)).

Check that the VM has enough RAM first: each instance adds a GraphDB of about 2 GB.

The install stops if the port is already in use or if `RECIPES` points to another instance's clone. A config
copied from another instance and not edited cannot break a running node.

## Commands

All commands take the instance name: `bash nb-node.sh <command> <instance>`.

| Command | Description |
|---|---|
| `install` | Clones the latest `neurobagel/recipes` tag, writes its `.env`, replaces the placeholder GraphDB passwords with random ones, and pulls or builds the images. `sync` runs it automatically the first time. Safe to run again. |
| `sync` | Fetches, builds and reloads the node if the inputs changed (see [How a sync works](#how-a-sync-works)). |
| `FORCE=1 … sync` | Reloads the node even if the inputs did not change. |
| `cron-install` | Adds the instance's daily sync to the user's crontab at `CRON_AT`. Running it again replaces only that instance's entry. |
| `cron-remove` | Removes the instance's crontab entry. |
| `status` | Shows the containers, the listening sockets, the cron entry and the last sync results. |
| `logs` | Shows the last container logs (`init_data`, `graph`, `api`). |
| `down` | Stops the node and keeps its volumes. |
| `wipe` | Stops the node and removes its volumes. Images, recipes, data, state, config and cron are kept, and the next `sync` reloads it. |
| `uninstall` | Removes the instance completely (see [Removing an instance](#removing-an-instance)). |

To run a command on every instance:

```bash
for i in neurinfo ofsep; do bash nb-node.sh status "$i"; done
```

### Exit codes of `sync`

| Code | Meaning |
|---|---|
| `0` | Success, or nothing to do |
| `1` | Fatal error (config, token, download, no JSONLD built, API not answering). The node is left as it was, unless the error happened after the reload started. |
| `2` | Node reloaded, but at least one study failed to build (see the log) |

## How a sync works

A sync handles one instance:

1. It takes the instance lock (`state-<name>/.lock`), so two syncs of the same instance never overlap.
2. It gets a `client_credentials` token from the instance's Keycloak and downloads into `state-<name>/staging/`:
   - `studies` (the exported studies) and `participants.json` (the data dictionary)
   - for each study: `participants.tsv`, `dataset_description.json`, `imaging.tsv`
3. If no study is exported, it stops the node and removes its data.
4. If the inputs have the same hash as the last loaded ones and the API answers, it stops here.
5. It builds one `study-<id>.jsonld` per study with `bagel pheno`, then `bagel bids` when the study has
   imaging rows. A study that fails to build keeps its previous JSONLD if there is one.
6. It restarts the node on the new data folder, waits until the node answers queries, and logs a summary of
   the datasets it serves.

Nothing on the running node changes until at least one JSONLD has been built, so a Shanoir outage or a failed
download leaves the node as it was. A study that is no longer exported disappears from the node at the next
reload.

## Files

For each instance, in `neurobagel-scripts/` unless noted otherwise:

| Path | Content |
|---|---|
| `nb-<name>.conf` | Config and secret (`chmod 600`, ignored by git) |
| `data-<name>/` | JSONLD files loaded by the node |
| `state-<name>/nb-node.log` | Log of every sync, rotated at 5 MB (`nb-node.log.1`) |
| `state-<name>/last/` | Inputs of the last reload, for debugging |
| `state-<name>/inputs.sha256` | Hash of the last loaded inputs |
| `~/neurobagel-recipes-<name>/` | Recipes clone, its `.env` and the GraphDB passwords |

`state-<name>/` is private (`700`). Each API listens on `127.0.0.1:<API_PORT>` only, and GraphDB (port 7200)
is never exposed. The API docs are at `http://127.0.0.1:<API_PORT>/docs`.

## Querying a node

```bash
python3 query.py --port 8001                             # every dataset on the OFSEP node
python3 query.py --port 8001 --modality T1w --min-age 35 # filtered
```

`API_PORT=8001 python3 query.py` works too. Modalities: `T1w T2w dwi bold asl eeg meg pet`.

## Searching all nodes at once

Each node answers only for its own instance. To search Neurinfo, OFSEP and any future instance in a single
query, run a Neurobagel **federation API** that lists every node's API (`http://127.0.0.1:8000`,
`http://127.0.0.1:8001`, …), and point the query tool at it. Each node can also be registered separately in
the public Neurobagel federation. These scripts do not set up the federation.

## Removing an instance

```bash
bash nb-node.sh uninstall ofsep          # asks you to type 'uninstall'
YES=1 bash nb-node.sh uninstall ofsep    # no prompt (required when not run from a terminal)
```

For that instance only, this removes:

- its cron entry
- the containers, network and volumes of its compose project, and its locally built init_data image
- its data folder, state folder, recipes clone (with the GraphDB passwords) and config file

The shared api, graph and bagel images are removed only when no other node is left on the VM.

The uninstall stops if a sync of that instance is running. It refuses to delete `/`, `$HOME` or the scripts
folder if a path in the config points there. It does not remove the scripts or the Keycloak client. If the
instance is gone for good, disable or delete that client in Keycloak and remove the node from the federation.

## Troubleshooting

Replace `<name>` with the instance name.

- **Check the last sync:** `bash nb-node.sh status <name>`, then `tail -n 100 state-<name>/nb-node.log`.
- **`token request failed`:** `SHANOIR_URL`, `NB_CLIENT_ID` or `NB_CLIENT_SECRET` is wrong in
  `nb-<name>.conf`, or the client has no service account.
- **`port ... is already used`:** another instance (or another service) uses that `API_PORT`. Give the
  instance a free port.
- **`... belongs to another node`:** two configs point `RECIPES` to the same folder. Remove `RECIPES` from
  the configs to use the per-instance default.
- **`another sync is running`:** a sync of that instance is still running
  (`pgrep -af "nb-node.sh sync <name>"`). The lock is released when that sync exits, so there is never a
  stale lock to delete.
- **`API did not answer`:** run `bash nb-node.sh logs <name>`. GraphDB can take a while to load large
  datasets, and it starts more slowly when the VM is short on RAM.
- **Cron does nothing:** check that `docker info` (or `sudo -n docker info`) works for the user without a
  password, and that `crontab -l` has one line per instance.
- **`imaging subjects missing from participants.tsv`:** that study's export is inconsistent. Fix it in
  Shanoir; until then the node keeps the study's previous JSONLD.
