# Neurobagel nodes for Shanoir

These scripts run one [Neurobagel](https://neurobagel.org) node per Shanoir instance (e.g. **Neurinfo**,
**OFSEP**) on one VM, and can publish them over HTTPS so that the public query tool
([query.neurobagel.org](https://query.neurobagel.org)) searches them.

Every day, for each instance, `nb-node.sh` downloads the studies the Shanoir instance exports to Neurobagel,
turns them into Neurobagel JSONLD files with the `bagel` CLI, and reloads the instance's node (GraphDB +
API) if the data changed.

| File | Role |
|---|---|
| `nb-node.sh` | Installs, syncs and manages the node of an instance |
| `nb.conf.example` | Template of an instance config |
| `nb-proxy.sh` | Runs the HTTPS proxy shared by the public nodes (nginx-proxy + Let's Encrypt) |
| `nb-proxy.conf.example` | Template of the optional proxy config |
| `query.py` | Queries the nodes, for checks |

## Requirements

On the VM:

- Linux with `bash`, `git`, `curl`, `python3`, `flock`, `ss`
- Docker with the `docker compose` plugin, usable **without a password** (user in the `docker` group, or a
  `NOPASSWD` sudo rule for `docker`), so that cron can run the sync
- Network access to the Shanoir instance, GitHub and Docker Hub
- About 2 GB of RAM per instance (GraphDB)

On the Shanoir instance: a Keycloak client in the `shanoir-ng` realm (default `neurobagel-export`), with
*client authentication* and *service accounts* enabled, allowed to call `/shanoir-ng/studies/neurobagel/*`.

## Install a node

Copy the scripts to the VM (e.g. into your home directory), then:

**1. Write the config**

```bash
cp nb.conf.example nb-neurinfo.conf && chmod 600 nb-neurinfo.conf
```

Edit `nb-neurinfo.conf`:

```bash
SHANOIR_URL=https://shanoir.irisa.fr
NB_CLIENT_SECRET=<secret of the Keycloak client>
API_PORT=8000          # unique per instance: 8000, 8001, ...
CRON_AT="20 4"         # daily sync at 04:20, a different slot per instance

NB_RETURN_AGG=true     # counts only
NB_MIN_CELL_SIZE=5     # hide datasets with 5 matching subjects or fewer
```

**2. Install, load the data, schedule the daily sync**

```bash
bash nb-node.sh install neurinfo
bash nb-node.sh sync neurinfo
bash nb-node.sh cron-install neurinfo
```

**3. Check**

```bash
bash nb-node.sh status neurinfo
python3 query.py neurinfo
```

The node now answers on `http://127.0.0.1:8000` (API docs at `/docs`). It is only reachable from the VM.

## Make a node public

The node must answer over HTTPS from the internet. One proxy serves every public node of the VM, each on
its own path of the same domain, e.g. `https://neurobagel.irisa.fr/neurinfo`.

**Before you start**

- A DNS entry of the domain pointing to the VM.
- Ports **80 and 443 open from the internet** to the VM (Let's Encrypt needs port 80 for the certificate),
  and nothing else on the VM using them.
- The data owners' approval of what will be public, and of `NB_RETURN_AGG` / `NB_MIN_CELL_SIZE`.

**1. Start the proxy (once for the VM)**

```bash
cp nb-proxy.conf.example nb-proxy.conf     # optional: Let's Encrypt email, image versions
bash nb-proxy.sh up
```

**2. Publish the node**: add to `nb-neurinfo.conf`:

```bash
NB_NAPI_DOMAIN=neurobagel.irisa.fr
NB_NAPI_BASE_PATH=/neurinfo                # unique per instance
```

then reload it:

```bash
FORCE=1 bash nb-node.sh sync neurinfo
```

**3. Check**

```bash
bash nb-proxy.sh status                    # each public node should answer 200
```

The first certificate can take a few minutes. Then `https://neurobagel.irisa.fr/neurinfo/docs` must open
from outside the institute's network.

**4. Register the node** with the Neurobagel team, so that it is added to the public federation
([list of public nodes](https://github.com/neurobagel/menu/blob/main/node_directory/neurobagel_public_nodes.json)).
Give them a name (e.g. "Shanoir – Neurinfo"), the URL `https://neurobagel.irisa.fr/neurinfo` and a contact.
Then check that the node shows up on [query.neurobagel.org](https://query.neurobagel.org).

To take a node offline again: remove `NB_NAPI_DOMAIN` from its config, sync it, and ask Neurobagel to remove
it from the federation.

## Configuration

### Instance config (`nb-<instance>.conf`)

| Variable | Default | Description |
|---|---|---|
| `SHANOIR_URL` | *required* | Base URL of the Shanoir instance |
| `NB_CLIENT_SECRET` | *required* | Secret of the Keycloak client |
| `API_PORT` | *required* | Port of the node API on `127.0.0.1`, unique per instance |
| `CRON_AT` | `20 4` | Daily sync time (`minute hour`) |
| `NB_CLIENT_ID` | `neurobagel-export` | Keycloak client id |
| `NB_HOME` | `~/neurobagel-<instance>` | Instance folder, never shared between instances |
| `BAGEL_IMAGE` | `neurobagel/bagelcli:latest` | bagel CLI image; pin a version in production |
| `NB_RETURN_AGG` | `true` | `true`: counts only. `false`: subject-level results too, **never on a public node** |
| `NB_MIN_CELL_SIZE` | `0` | Datasets with this many matching subjects or fewer are hidden |
| `NB_NAPI_TAG` | `latest` | `neurobagel/api` image tag; pin a version in production |
| `NB_NAPI_DOMAIN` | empty | Public domain of the node; empty = local only |
| `NB_NAPI_BASE_PATH` | empty | Public path on that domain, e.g. `/neurinfo` |
| `NB_GRAPH_MEMORY` | `2G` | Maximum memory of GraphDB |

The node settings (`NB_RETURN_AGG` and below) are written by the script into
`~/neurobagel-<instance>/recipes/.env`: edit the config, never that `.env`. A change is applied at the next
sync.

### Proxy config (`nb-proxy.conf`, optional)

| Variable | Default | Description |
|---|---|---|
| `LETSENCRYPT_EMAIL` | empty | Warned by Let's Encrypt if renewals fail (recommended) |
| `NGINX_PROXY_TAG` | `latest` | `nginxproxy/nginx-proxy` image tag; pin in production |
| `ACME_COMPANION_TAG` | `latest` | `nginxproxy/acme-companion` image tag; pin in production |
| `PROXY_HOME` | `~/neurobagel-proxy` | Proxy folder |

## Commands

`bash nb-node.sh <command> <instance>`:

| Command | Description |
|---|---|
| `install` | Installs the node (recipes clone, `.env`, random GraphDB passwords, images). Safe to rerun. |
| `sync` | Downloads, builds and reloads the node if the data or settings changed. `FORCE=1`: reload anyway. |
| `cron-install` / `cron-remove` | Adds / removes the daily sync |
| `status` | Containers, ports, cron entry, last sync |
| `logs` | Container logs |
| `down` | Stops the node |
| `wipe` | Stops the node and deletes its database; the next `sync` reloads it |
| `uninstall` | Removes the instance completely (`YES=1`: no prompt) |

`bash nb-proxy.sh <command>`: `up`, `status`, `logs`, `down` (all public nodes go offline), `uninstall`.

`sync` exits with `0` (success or nothing to do), `1` (error, the node is left as it was) or `2` (reloaded,
but some studies failed to build). Its log is `~/neurobagel-<instance>/state/nb-node.log`.

## Querying the nodes

```bash
python3 query.py                                  # every node
python3 query.py neurinfo                         # one node
python3 query.py neurinfo --modality T1w --min-age 35
python3 query.py --help
```

## Files on the VM

```
~/
├── nb-node.sh, nb-proxy.sh, query.py, *.example
├── nb-neurinfo.conf          config + secret (chmod 600)
├── nb-proxy.conf             optional
├── neurobagel-neurinfo/
│   ├── recipes/              neurobagel/recipes clone, generated .env, GraphDB passwords
│   ├── data/                 JSONLD files served by the node
│   └── state/                private: sync log, last downloaded files
└── neurobagel-proxy/         proxy (recipes clone), only for public nodes
```
