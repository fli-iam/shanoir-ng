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
| `nb-node.conf.example` | Template of an instance config |
| `nb-proxy.sh` | Runs the HTTPS proxy shared by the public nodes (nginx-proxy + Let's Encrypt) |
| `nb-proxy.conf.example` | Template of the proxy config |
| `query.py` | Queries the nodes, for checks |

## Requirements

On the VM:

- Linux with `bash`, `git`, `curl`, `python3`, `flock`, `ss`
- Docker with the `docker compose` plugin, usable **without a password** (user in the `docker` group, or a
  `NOPASSWD` sudo rule for `docker`), so that cron can run the sync
- Network access to the Shanoir instance, GitHub and Docker Hub
- About 2 GB of RAM per instance (GraphDB)

On the Shanoir instance: a Keycloak client in the `shanoir-ng` realm (e.g. `neurobagel-export`), with
*client authentication* and *service accounts* enabled, allowed to call `/shanoir-ng/studies/neurobagel/*`.

## Install a node

Copy the scripts to the VM (e.g. into your home directory), then:

**1. Write the config**

```bash
cp nb-node.conf.example nb-neurinfo.conf && chmod 600 nb-neurinfo.conf
```

Edit `nb-neurinfo.conf`:

```bash
SHANOIR_URL=https://shanoir.irisa.fr
NB_CLIENT_ID=neurobagel-export
NB_CLIENT_SECRET=<secret of the Keycloak client>
API_PORT=8000                              # unique per instance: 8000, 8001, ...
CRON_AT="20 4"                             # daily sync at 04:20, a different slot per instance
BAGEL_IMAGE=neurobagel/bagelcli:v0.11.7
NB_NAPI_TAG=v0.12.0

NB_RETURN_AGG=true                         # counts only
NB_MIN_CELL_SIZE=5                         # hide datasets with 5 matching subjects or fewer
NB_NAPI_DOMAIN=                            # empty for now (local node): set in "Make a node public", e.g. neurobagel.irisa.fr
NB_NAPI_BASE_PATH=                         # same, e.g. /node --> https://neurobagel.irisa.fr/node
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
its own path of the same domain, e.g. `https://neurobagel.irisa.fr/node`.

> **Why a separate proxy for a single node?** Only the Neurinfo node is public today, so the proxy could simply
> run from that node's own recipes folder, with the recipes' `docker-compose.proxy.yml`
> ([Neurobagel production docs](https://neurobagel.org/user_guide/production_deployment/#proxy-server)).
> It runs instead from its own recipes clone (`~/neurobagel-proxy`, managed by `nb-proxy.sh`), on purpose, for
> future scaling, i.e. publishing more Neurobagel nodes later (e.g. one for OFSEP):
>
> - **One proxy for every node.** Only one program can listen on ports 80 and 443, so all public nodes must
>   share one proxy. Owned by no node, it does not depend on which instance was installed first.
> - **Independent lifecycles.** `down`, `wipe`, `uninstall` or a recipes upgrade of a node never stop the
>   proxy, the other public nodes, or lose the HTTPS certificate (kept in the proxy's own volumes).
> - **Adding a node is only a config change.** To publish another instance (e.g. OFSEP), set `NB_NAPI_DOMAIN`
>   and its own `NB_NAPI_BASE_PATH` in its config and sync it. The proxy finds it on the `NB_proxynet`
>   network and routes it, under the same domain and certificate, without touching the proxy or the
>   Neurinfo node.

**Before you start**

- A DNS entry of the domain pointing to the VM.
- Ports **80 and 443 open from the internet** to the VM (Let's Encrypt needs port 80 for the certificate),
  and nothing else on the VM using them.
- The data owners' approval of what will be public, and of `NB_RETURN_AGG` / `NB_MIN_CELL_SIZE`.

**1. Start the proxy (once for the VM)**

```bash
cp nb-proxy.conf.example nb-proxy.conf     # Let's Encrypt email, image versions
bash nb-proxy.sh up
```

**2. Publish the node**: set in `nb-neurinfo.conf`:

```bash
NB_NAPI_DOMAIN=neurobagel.irisa.fr
NB_NAPI_BASE_PATH=/node                    # unique per instance
```

then reload it:

```bash
bash nb-node.sh sync neurinfo --force
```

**3. Check**

```bash
bash nb-proxy.sh status                    # each public node should answer 200
```

The first certificate can take a few minutes. Then `https://neurobagel.irisa.fr/node/docs` must open
from outside the institute's network.

**4. Register the node** with the Neurobagel team, so that it is added to the public federation
([list of public nodes](https://github.com/neurobagel/menu/blob/main/node_directory/neurobagel_public_nodes.json)).
`https://neurobagel.irisa.fr/node` is already listed there, as "Shanoir": the Neurinfo node needs no new
registration, only a check that it shows up on [query.neurobagel.org](https://query.neurobagel.org) once it
answers. Any other public node needs its own entry: give them a name (e.g. "Shanoir – OFSEP"), its URL and a
contact.

To take a node offline again: remove `NB_NAPI_DOMAIN` from its config, sync it, and ask Neurobagel to remove
it from the federation.

## Configuration

### Instance config (`nb-<instance>.conf`)

| Variable | Default | Description |
|---|---|---|
| `SHANOIR_URL` | *required* | Base URL of the Shanoir instance |
| `NB_CLIENT_ID` | *required* | Keycloak client id, e.g. `neurobagel-export` |
| `NB_CLIENT_SECRET` | *required* | Secret of the Keycloak client |
| `API_PORT` | *required* | Port of the node API on `127.0.0.1`, unique per instance |
| `CRON_AT` | `20 4` | Daily sync time (`minute hour`), a different slot per instance |
| `BAGEL_IMAGE` | *required* | bagel CLI image with its version, e.g. `neurobagel/bagelcli:v0.11.7` |
| `NB_RETURN_AGG` | `true` | `true`: counts only. `false`: subject-level results too, **never on a public node** |
| `NB_MIN_CELL_SIZE` | `0` | Datasets with this many matching subjects or fewer are hidden |
| `NB_NAPI_TAG` | *required* | `neurobagel/api` image tag, e.g. `v0.12.0` |
| `NB_NAPI_DOMAIN` | empty | Public domain of the node, e.g. `neurobagel.irisa.fr`; empty = local only |
| `NB_NAPI_BASE_PATH` | empty | Public path on that domain, e.g. `/node`; unique per instance |

The node settings (`NB_RETURN_AGG` and below) are written by the script into
`~/neurobagel-<instance>/recipes/.env`: edit the config, never that `.env`. A change is applied at the next
sync.

### Proxy config (`nb-proxy.conf`)

| Variable | Default | Description |
|---|---|---|
| `LETSENCRYPT_EMAIL` | empty | Warned by Let's Encrypt if renewals fail (recommended) |
| `NGINX_PROXY_TAG` | *required* | `nginxproxy/nginx-proxy` release, e.g. `1.11.6` (not `latest`: it is a development build) |
| `ACME_COMPANION_TAG` | *required* | `nginxproxy/acme-companion` release, e.g. `2.8.2` |

## Commands

`bash nb-node.sh <command> <instance>`:

| Command | Description |
|---|---|
| `install` | Installs the node (recipes clone, `.env`, random GraphDB passwords, images). Safe to rerun. |
| `sync` | Downloads, builds and reloads the node if the data or settings changed. `--force`: reload anyway. |
| `cron-install` / `cron-remove` | Adds the daily sync at `CRON_AT` |
| `cron-remove` | Removes the daily sync at `CRON_AT` (removing it leaves the node running) |
| `status` | Public or local, containers, listening ports, cron entry, last syncs and their errors |
| `logs` | Last logs of the `init_data`, `graph` and `api` containers |
| `down` | Stops the node and keeps its database; the next `sync` brings it back |
| `wipe` | Stops the node and deletes its database; the next `sync` reloads it |
| `uninstall` | Removes the instance completely: cron, containers, volumes, recipes, data, state, config (`--yes`: no prompt) |

`bash nb-proxy.sh <command>`:

| Command | Description |
|---|---|
| `up` | Installs the proxy if needed, then starts it or applies a changed `nb-proxy.conf` |
| `status` | Containers, routed nodes and the HTTP code of each public URL (`200` = OK) |
| `logs` | Last logs of nginx-proxy and acme-companion (routing, certificates) |
| `down` | Stops the proxy and keeps its certificates: public nodes go offline but keep running |
| `uninstall` | Removes the proxy, its certificates and `~/neurobagel-proxy`; refused while a node uses it (`--yes`: no prompt) |

`bash nb-node.sh --help` and `bash nb-proxy.sh --help` (or `-h`) print the full usage of each script.

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
├── nb-proxy.conf             proxy config (public nodes only)
├── neurobagel-neurinfo/
│   ├── recipes/              neurobagel/recipes clone, generated .env, GraphDB passwords
│   ├── data/                 JSONLD files served by the node
│   └── state/                private: sync log, last downloaded files
└── neurobagel-proxy/         proxy (recipes clone), only for public nodes
```
