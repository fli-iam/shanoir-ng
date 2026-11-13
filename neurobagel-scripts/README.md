# Neurobagel nodes for Shanoir

These scripts run one [Neurobagel](https://neurobagel.org) node per Shanoir instance on the Neurobagel VM:
today **Neurinfo** and **OFSEP**, and more can be added later. A shared HTTPS proxy can publish them so the
public Neurobagel query tool can search them (see [Making the nodes public](#making-the-nodes-public)).

For each instance, `nb-node.sh` does the following every day from cron:

1. downloads the studies that the Shanoir instance exports to Neurobagel,
2. builds the Neurobagel JSONLD files with the `bagel` CLI,
3. reloads that instance's own node (GraphDB + API), but only when the data changed.

| File | Role |
|---|---|
| `nb-node.sh` | Installs, syncs, manages and removes the nodes |
| `nb.conf.example` | Template of an instance config |
| `nb-proxy.sh` | Runs the shared HTTPS proxy of the public nodes (nginx-proxy + Let's Encrypt) |
| `nb-proxy.conf.example` | Template of the optional proxy config |
| `query.py` | Queries the nodes by instance name, for checks |

On the VM, the scripts and the instance configs live together in your home directory, and each instance
gets its own folder next to them:

```
~/
├── nb-node.sh
├── nb-proxy.sh
├── query.py
├── nb.conf.example
├── nb-proxy.conf.example
├── nb-neurinfo.conf
├── nb-ofsep.conf
├── nb-proxy.conf     optional
├── neurobagel-proxy/
│   ├── recipes/              neurobagel/recipes clone (docker-compose.proxy.yml)
│   └── compose.local.yml     image versions, Let's Encrypt email
├── neurobagel-neurinfo/
│   ├── recipes/   neurobagel/recipes clone, .env, GraphDB passwords
│   ├── data/      JSONLD files loaded by the node
│   └── state/     log, lock, last downloaded inputs
└── neurobagel-ofsep/
    ├── recipes/
    ├── data/
    └── state/
```

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
| Instance folder (`recipes/`, `data/`, `state/`) | `~/neurobagel-neurinfo/` | `~/neurobagel-ofsep/` | `~/neurobagel-<name>/` |
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
- About **2 GB of RAM per instance** for GraphDB (`NB_GRAPH_MEMORY`, 2G by default), plus the API containers
- For cron, the user must be able to run docker **without a password**: either membership in the `docker`
  group, or a `NOPASSWD` sudo rule for `docker` (the script then uses `sudo -n docker`)

On each Shanoir instance:

- A Keycloak client in the `shanoir-ng` realm (default name `neurobagel-export`), with *client
  authentication* and *service accounts* enabled and allowed to call `/shanoir-ng/studies/neurobagel/*`

## Setup

Run this once per instance (here Neurinfo and OFSEP):

```bash
cd ~        # where nb-node.sh, query.py and nb.conf.example are

cp nb.conf.example nb-neurinfo.conf && chmod 600 nb-neurinfo.conf
cp nb.conf.example nb-ofsep.conf    && chmod 600 nb-ofsep.conf
# edit both files: SHANOIR_URL, NB_CLIENT_SECRET, API_PORT, CRON_AT (see below)

bash nb-node.sh install neurinfo           # Installs the node
bash nb-node.sh install ofsep
bash nb-node.sh sync neurinfo           # Fetches exported studies, builds and loads data
bash nb-node.sh sync ofsep
bash nb-node.sh cron-install neurinfo   # daily sync
bash nb-node.sh cron-install ofsep
```

Check the result:

```bash
bash nb-node.sh status neurinfo
bash nb-node.sh status ofsep
python3 query.py                        # datasets on every node
```

### Instance config (`nb-<name>.conf`)

| Variable | Required | Default | Description |
|---|---|---|---|
| `SHANOIR_URL` | yes | | Base URL of the Shanoir instance, e.g. `https://shanoir.example.org` |
| `NB_CLIENT_SECRET` | yes | | Secret of the instance's Keycloak client |
| `API_PORT` | yes | | Port of the node API, bound to `127.0.0.1` only. **Must be unique per instance.** |
| `CRON_AT` | recommended | `20 4` | Daily sync time (`minute hour`). Give each instance its own slot. |
| `NB_CLIENT_ID` | no | `neurobagel-export` | Keycloak client id |
| `NB_HOME` | no | `$HOME/neurobagel-<name>` | Instance folder, e.g. on a bigger disk. **Never share it between instances.** |
| `BAGEL_IMAGE` | no | `neurobagel/bagelcli:latest` | Image used when no `bagel` command is in `PATH`. Pin it together with `NB_NAPI_TAG`. |

#### Node settings

These configure the node itself. The script writes them into `~/neurobagel-<name>/recipes/.env`, which it
owns: **edit the config, never the `.env`**, which is rewritten at every reload. A changed setting reloads
the node at the next `sync`, even if the data did not change.

| Variable | Default | Description |
|---|---|---|
| `NB_RETURN_AGG` | `true` | `true`: the node returns only dataset-level counts. `false`: it also returns subject-level results (age, sex, diagnosis, sessions… of each matching subject). **Keep `true` on any node reachable from outside.** Every sync logs a warning when it is `false`. |
| `NB_MIN_CELL_SIZE` | `0` | Datasets with this many matching subjects **or fewer** are left out of query results. Hides small groups that could point to individuals. `0` shows every dataset. |
| `NB_NAPI_TAG` | `latest` | Image tag of `neurobagel/api`. Pin a version in production, compatible with the JSONLDs the bagel CLI (`BAGEL_IMAGE`) produces. |
| `NB_NAPI_DOMAIN` | empty | Public domain of the node, e.g. `neurobagel.example.org` (no `https://`, no path). Set: the node is published through the shared proxy (see [Making the nodes public](#making-the-nodes-public)). Empty: local only. |
| `NB_NAPI_BASE_PATH` | empty | Public sub-path of the node on that domain, e.g. `/ofsep` for `https://<domain>/ofsep`. Leading `/`, no trailing `/`. **Unique per instance** when instances share a domain; empty when the node has its own domain. |
| `NB_GRAPH_MEMORY` | `2G` | Maximum memory of the instance's GraphDB (e.g. `2G`, `1500M`). |

Example `nb-ofsep.conf`:

```bash
SHANOIR_URL=https://shanoir-ofsep.irisa.fr
NB_CLIENT_SECRET=...
API_PORT=8001
CRON_AT="50 4"

NB_RETURN_AGG=true
NB_MIN_CELL_SIZE=10
NB_NAPI_TAG=<pinned version>
NB_NAPI_DOMAIN=neurobagel.example.org
NB_NAPI_BASE_PATH=/ofsep
NB_GRAPH_MEMORY=2G
```

Syncs of different instances can run at the same time, but each one builds JSONLDs and restarts a GraphDB.
Spreading them over separate slots keeps the VM responsive; 30 minutes apart is plenty.

## Adding an instance later

1. Create the Keycloak client on the new Shanoir instance (see [Requirements](#requirements)).
2. Pick a name, a free port and a free cron slot, then write the instance's config:
   ```bash
   cp nb.conf.example nb-<name>.conf && chmod 600 nb-<name>.conf
   # SHANOIR_URL, NB_CLIENT_SECRET, API_PORT=8002, CRON_AT="20 5"
   # public: NB_NAPI_DOMAIN (same as the others) and its own NB_NAPI_BASE_PATH, e.g. /<name>
   ```
3. Start it and schedule it:
   ```bash
   bash nb-node.sh install <name>
   bash nb-node.sh sync <name>
   bash nb-node.sh cron-install <name>
   ```
4. Add a column for it to the table in [How the instances are separated](#how-the-instances-are-separated).
5. If it is public, check it with `bash nb-proxy.sh status` and register it with Neurobagel (see
   [Registering the nodes with Neurobagel](#registering-the-nodes-with-neurobagel)).

Check that the VM has enough RAM first: each instance adds a GraphDB of about 2 GB.

The install stops if the port is already in use or if `NB_HOME` points to another instance's folder. A config
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

A sync handles one instance. Its files go through three places inside `~/neurobagel-<name>/`:
`state/staging/` (work in progress), `data/` (what the node serves) and `state/last/` (raw inputs kept
for debugging).

### 1. Download

The sync takes the instance lock (`state/.lock`), so two syncs of the same instance never overlap. It
empties `state/staging/`, gets a `client_credentials` token from the instance's Keycloak, and downloads
from `<SHANOIR_URL>/shanoir-ng/studies/neurobagel/`:

```
state/staging/
├── studies.json                     list of the exported studies (id, name)
├── participants.json                data dictionary, shared by all studies
├── studies/
│   └── study-<id>/
│       ├── participants.tsv         one row per subject (age, sex, diagnosis…)
│       ├── dataset_description.json study name, authors, homepage…
│       └── imaging.tsv              one row per imaging session and modality
└── data/                            empty: the JSONLDs are built here
```

A new token is requested for each study, so a slow download cannot make the next ones expire. If any
download fails, the sync stops (exit code 1) and the node and `data/` are not touched.

### 2. Decide

| Case | What happens |
|---|---|
| No study is exported | The node is stopped, and `data/`, `state/staging/` and the hash are deleted. |
| Same inputs and node settings as the last reload, and the API answers | `state/staging/` is deleted and nothing else changes. |
| Inputs or node settings changed (or `FORCE=1`) | Build and reload, below. |

The comparison uses a hash (`state/inputs.sha256`) of `participants.json`, everything under `studies/`, and
the `.env` generated from the [node settings](#node-settings). `studies.json` is not part of the hash, so
renaming a study changes nothing on the node until its own files change.

### 3. Build

For each study, in `state/staging/`:

1. The study is skipped if `imaging.tsv` lists subjects that are missing from `participants.tsv`.
2. `bagel pheno` turns `participants.tsv`, `participants.json` and `dataset_description.json` into a JSONLD.
3. If `imaging.tsv` has rows, `bagel bids` adds the imaging to it. Otherwise the study is phenotypic only.
4. The result goes to `state/staging/data/study-<id>.jsonld`.

If a study fails to build, its previous JSONLD is copied from `data/` and the sync will end with exit
code 2. If it has no previous JSONLD, it is left off the node. If no JSONLD at all could be built, the sync
stops and the node is left as it was.

### 4. Reload

1. The node is stopped and its GraphDB volume removed.
2. `data/` is renamed to `data.old/`, and `state/staging/data/` becomes the new `data/`.
3. The rest of `state/staging/` (the raw files from Shanoir) replaces `state/last/`.
4. `recipes/.env` is rewritten from the config, so the node starts with the current node settings.
5. The node starts on the new `data/`, and GraphDB loads every JSONLD in it.
6. Once the API answers queries, the new hash is written, `data.old/` is deleted, and a summary of the
   served datasets is logged.

After a successful reload, the instance folder looks like this:

```
~/neurobagel-<name>/
├── recipes/                 neurobagel/recipes clone, .env, GraphDB passwords
├── data/
│   └── study-<id>.jsonld    what the node serves, one file per exported study
└── state/                   private (700)
    ├── last/                raw files of the last reload, same layout as staging/
    ├── inputs.sha256        hash of the inputs and settings loaded in the node
    ├── nb-node.log          log of every sync (nb-node.log.1 after 5 MB)
    └── .lock
```

### What stays on disk

- **Subject-level data:** `state/last/` keeps the raw `participants.tsv` files (ages, sex, diagnoses)
  until the next reload replaces them. `state/` is readable only by the user running the script (and root).
  The node itself only returns counts.
- **After a failed sync:** a download or build error leaves the partial `state/staging/` behind. The next
  sync empties it first, so files never pile up.
- **After a failed restart:** `data.old/` and the new `state/last/` stay, and the hash is not written. The
  next sync therefore rebuilds and reloads everything.

A study that is no longer exported disappears from the node at the next reload.

## Files

For each instance:

| Path | Content |
|---|---|
| `~/nb-<name>.conf` | Config and secret (`chmod 600`) |
| `~/neurobagel-<name>/recipes/` | Recipes clone, its `.env` and the GraphDB passwords |
| `~/neurobagel-<name>/data/` | JSONLD files loaded by the node |
| `~/neurobagel-<name>/state/nb-node.log` | Log of every sync, rotated at 5 MB (`nb-node.log.1`) |
| `~/neurobagel-<name>/state/last/` | Inputs of the last reload, for debugging |
| `~/neurobagel-<name>/state/inputs.sha256` | Hash of the last loaded inputs |

| `~/neurobagel-<name>/compose.local.yml` | Public nodes only: keeps the API on `127.0.0.1:<API_PORT>` (generated) |

`state/` is private (`700`). Each API listens on `127.0.0.1:<API_PORT>`, and GraphDB (port 7200) is never
exposed. A public node is also reachable through the proxy, and only through it. The API docs are at
`http://127.0.0.1:<API_PORT>/docs` (and `https://<domain><base path>/docs` for a public node).

## Querying a node

`query.py` queries the nodes by instance name. It finds every `nb-<name>.conf` next to it and reads the
node's port (`API_PORT`) from it.

```bash
python3 query.py                                    # every instance, every dataset
python3 query.py ofsep                              # one instance
python3 query.py neurinfo ofsep --modality T1w      # several instances, filtered
python3 query.py ofsep --min-age 35 --max-age 60
python3 query.py --help                             # options and configured instances
```

Modalities: `T1w T2w dwi bold asl eeg meg pet`. Example output:

```
Query: subjects with T1-weighted imaging, aged 35 or more

== ofsep  (https://shanoir-ofsep.irisa.fr)

   Study A
     Matching subjects: 12 of 40
     Imaging:           T1-weighted, diffusion-weighted
     Homepage:          https://example.org/study-a
     Subject details:   hidden (counts only)

   1 dataset(s), 12 matching subject(s)

== neurinfo  (https://shanoir.irisa.fr)
   The node does not answer on port 8000 ([Errno 111] Connection refused).
   Check it with: bash nb-node.sh status neurinfo
```

It exits with code 1 if a node could not be queried, so it can be used in checks. It queries each node
separately; it does not replace a federation.

## Making the nodes public

The public Neurobagel query tool ([query.neurobagel.org](https://query.neurobagel.org)) searches the nodes
registered in the Neurobagel public federation. To be searchable, a node must answer over HTTPS from the
internet. This follows the [Neurobagel production deployment](https://neurobagel.org/user_guide/production_deployment/#proxy-server)
with one proxy for all instances:

```
                     internet :80 / :443
                            │
   ┌────────────────────────▼─────────────────────────┐
   │  nb-proxy.sh (recipes' docker-compose.proxy.yml)  │
   │   nginx-proxy     routes by domain + path         │
   │   acme-companion  Let's Encrypt certificates      │
   └───────────┬──────────────────────────┬────────────┘
               │   docker network NB_proxynet           │
   ┌───────────▼───────────┐  ┌───────────▼───────────┐
   │ node neurinfo  (api)  │  │ node ofsep  (api)     │
   │ <domain>/neurinfo     │  │ <domain>/ofsep        │
   │ graph: private        │  │ graph: private        │
   └───────────────────────┘  └───────────────────────┘
```

- The proxy runs once for the whole VM, from its own recipes clone in `~/neurobagel-proxy/`. It creates the
  `NB_proxynet` network and is the only thing listening on ports 80 and 443.
- A node with `NB_NAPI_DOMAIN` in its config runs on the recipes' `docker-compose.prod.yml`: its API joins
  `NB_proxynet` with its domain and base path, and nginx-proxy routes `https://<domain><base path>` to it.
  acme-companion gets and renews the certificate of each domain, one for all nodes that share it.
- The API also stays on `127.0.0.1:<API_PORT>` (through the generated `compose.local.yml`), for the
  script's checks and `query.py`. GraphDB is never reachable from outside.

### Prerequisites

- A DNS entry of the domain (e.g. `neurobagel.example.org`) pointing to the VM's public IP.
- Ports **80 and 443** open from the internet. Let's Encrypt validates on port 80, so no port 80 = no certificate.
- Nothing else on the VM listening on 80 or 443 (e.g. a system nginx or apache).
- The data owners' approval, and the [node settings](#node-settings) chosen for public use: `NB_RETURN_AGG=true`,
  a `NB_MIN_CELL_SIZE` agreed with them, and pinned `NB_NAPI_TAG` / `BAGEL_IMAGE`.

### Steps

```bash
# 1. the proxy, once
cp nb-proxy.conf.example nb-proxy.conf          # optional: LETSENCRYPT_EMAIL, pinned image versions
bash nb-proxy.sh up

# 2. each node: in nb-<name>.conf
#      NB_NAPI_DOMAIN=neurobagel.example.org
#      NB_NAPI_BASE_PATH=/neurinfo              (/ofsep for the other one)
FORCE=1 bash nb-node.sh sync neurinfo           # restarts the node behind the proxy
FORCE=1 bash nb-node.sh sync ofsep

# 3. check
bash nb-proxy.sh status                         # routed nodes and their HTTP answer
curl -s -X POST https://neurobagel.example.org/ofsep/datasets -H 'Content-Type: application/json' -d '{}'
```

The first certificate can take a few minutes after the first start. From a machine outside the VM's
network, also check that `https://<domain>/<name>/docs` opens and that ports 8000, 8001 and 7200 are
closed.

Without `FORCE=1` the change is applied at the next daily sync anyway, since the domain is a node setting.
A public node refuses to reload while the proxy is not running ("start the proxy first"), and leaves the
running node as it was.

### Registering the nodes with Neurobagel

Ask the Neurobagel team to add each node to the public federation (see their documentation for the
current process). They typically need, per node: a name (e.g. "Shanoir – OFSEP"), its public API URL
(e.g. `https://neurobagel.example.org/ofsep`) and a contact. Then check on
[query.neurobagel.org](https://query.neurobagel.org) that both nodes are listed and answer.

### Proxy commands

| Command | Description |
|---|---|
| `bash nb-proxy.sh up` | Clones the recipes into `~/neurobagel-proxy` if needed, then starts (or updates) nginx-proxy and acme-companion. Safe to run again, e.g. after changing `nb-proxy.conf`. |
| `bash nb-proxy.sh status` | Containers, routed nodes, and the HTTP code each public URL answers (`200` = OK). |
| `bash nb-proxy.sh logs` | Last proxy logs, e.g. certificate errors. |
| `bash nb-proxy.sh down` | Stops the proxy. Every public node becomes unreachable, but keeps running; `up` brings them back. |
| `bash nb-proxy.sh uninstall` | Removes the proxy, its certificates and `~/neurobagel-proxy`. Refused while a node still uses it: remove `NB_NAPI_DOMAIN` from those configs and sync them first. |

Proxy config (`nb-proxy.conf`, optional):

| Variable | Default | Description |
|---|---|---|
| `LETSENCRYPT_EMAIL` | empty | Email of the Let's Encrypt account, warned if renewals fail. Recommended. |
| `NGINX_PROXY_TAG` | `latest` | Image tag of `nginxproxy/nginx-proxy`. Pin it in production. |
| `ACME_COMPANION_TAG` | `latest` | Image tag of `nginxproxy/acme-companion`. Pin it in production. |
| `PROXY_HOME` | `$HOME/neurobagel-proxy` | Proxy folder. |

Certificates are kept in the proxy's Docker volumes: `down` and `up` keep them, `uninstall` deletes them.
Let's Encrypt limits how many certificates a domain gets per week, so avoid reinstalling the proxy in a loop.

To take a node offline again, remove `NB_NAPI_DOMAIN` from its config and sync it, and ask Neurobagel to
remove it from the federation.

## Searching all nodes at once

Each node answers only for its own instance. A **federation API** sends one query to many nodes and merges
the answers; the query tool talks to a federation, never to the nodes directly.

The nodes are made searchable together through the **public** Neurobagel federation and query tool, run by
the Neurobagel team: once the nodes are public and registered (see [Making the nodes public](#making-the-nodes-public)),
[query.neurobagel.org](https://query.neurobagel.org) searches them alongside the other public nodes. These
scripts do not run a federation or query tool of their own (the recipes' `portal` profile would).

## Removing an instance

```bash
bash nb-node.sh uninstall ofsep          # asks you to type 'uninstall'
YES=1 bash nb-node.sh uninstall ofsep    # no prompt (required when not run from a terminal)
```

For that instance only, this removes:

- its cron entry
- the containers, network and volumes of its compose project, and its locally built init_data image
- its whole instance folder `~/neurobagel-<name>/` (recipes clone with the GraphDB passwords, data,
  state) and its config file `~/nb-<name>.conf`

The shared api, graph and bagel images are removed only when no other node is left on the VM.

The uninstall stops if a sync of that instance is running. It refuses to delete `/`, `$HOME`, the scripts
folder or one of its parents if `NB_HOME` points there. It does not remove the scripts or the Keycloak client. If the
instance is gone for good, disable or delete that client in Keycloak and remove the node from the federation.

## Troubleshooting

Replace `<name>` with the instance name.

- **Check the last sync:** `bash nb-node.sh status <name>`, then `tail -n 100 ~/neurobagel-<name>/state/nb-node.log`.
- **`token request failed`:** `SHANOIR_URL`, `NB_CLIENT_ID` or `NB_CLIENT_SECRET` is wrong in
  `nb-<name>.conf`, or the client has no service account.
- **`port ... is already used`:** another instance (or another service) uses that `API_PORT`. Give the
  instance a free port.
- **`the proxy is not running`:** a public node (`NB_NAPI_DOMAIN` set) cannot reload without the proxy.
  Run `bash nb-proxy.sh up`, then sync again.
- **Public URL gives 502/503, or `nb-proxy.sh status` shows no node:** the node is not attached to the proxy.
  Check `bash nb-node.sh status <name>` (it says whether the node is public), then `FORCE=1 bash nb-node.sh sync <name>`.
- **No certificate / browser warning:** the DNS entry does not point to the VM yet, or port 80 is not open
  from the internet. See `bash nb-proxy.sh logs`.
- **`port 80 or 443 is already used`:** another web server runs on the VM. Stop it, or put the nodes behind
  that server instead of `nb-proxy.sh`.
- **`... belongs to another node`:** two configs point `NB_HOME` to the same folder. Remove `NB_HOME` from
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
