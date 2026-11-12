# Neurobagel node scripts

These scripts run a [Neurobagel](https://neurobagel.org) node fed by Shanoir on the Neurobagel VM.
`nb-node.sh` does all the work: it installs the node, downloads the studies Shanoir exports, builds the
Neurobagel JSONLD files with the `bagel` CLI, and reloads the node when the data changes. It is designed to
run unattended from cron.

| File | Role |
|---|---|
| `nb-node.sh` | Installs, syncs and manages the node |
| `nb.conf.example` | Sample configuration, copy it to `nb.conf` |
| `query.py` | Queries the local node API, for checks |

## Requirements

- Linux with `bash`, `git`, `curl`, `python3`, `flock`
- Docker with the `docker compose` plugin
- Network access to the Shanoir instance and to GitHub / Docker Hub (for the install)
- A Keycloak client in the `shanoir-ng` realm (default name `neurobagel-export`) with
  *client authentication* and *service accounts* enabled. Its token must be allowed to call the
  `/shanoir-ng/studies/neurobagel/*` endpoints.

For cron, the user must be able to run docker **without a password**: either membership in the `docker`
group, or a `NOPASSWD` sudo rule for `docker` (the script then uses `sudo -n docker`).

## Setup

```bash
cd neurobagel-scripts
cp nb.conf.example nb.conf && chmod 600 nb.conf
# edit nb.conf: set SHANOIR_URL and NB_CLIENT_SECRET

bash nb-node.sh install        # optional: sync installs the node if it is missing
bash nb-node.sh sync           # first fetch, build and load
bash nb-node.sh cron-install   # daily sync
```

### Configuration (`nb.conf`)

| Variable | Required | Default | Description |
|---|---|---|---|
| `SHANOIR_URL` | yes | | Base URL of Shanoir, e.g. `https://shanoir.example.org` |
| `NB_CLIENT_SECRET` | yes | | Secret of the Keycloak client |
| `NB_CLIENT_ID` | no | `neurobagel-export` | Keycloak client id |
| `API_PORT` | no | `8000` | Port of the node API, bound to `127.0.0.1` only |
| `RECIPES` | no | `$HOME/neurobagel-recipes` | Where the `neurobagel/recipes` repository is cloned |
| `NB_DATA` | no | `./data` | Folder of the JSONLD files loaded by the node |
| `BAGEL_IMAGE` | no | `neurobagel/bagelcli:latest` | Docker image used when no `bagel` command is in `PATH` |

Set `NB_CONF=/path/to/file` to use a configuration file somewhere else.

## Commands

| Command | Description |
|---|---|
| `bash nb-node.sh install` | Clones the latest `neurobagel/recipes` tag, writes its `.env`, replaces the placeholder GraphDB passwords with random ones, then pulls or builds the images. Safe to run again. |
| `bash nb-node.sh sync` | Fetches, builds and reloads the node if needed (see below). |
| `FORCE=1 bash nb-node.sh sync` | Reloads the node even if the inputs did not change. |
| `bash nb-node.sh cron-install` | Adds a daily `sync` to the user's crontab, at 04:20 by default. Use `CRON_AT="30 5"` (minute hour) to change the time. Running it again replaces the entry. |
| `bash nb-node.sh cron-remove` | Removes the crontab entry. |
| `bash nb-node.sh status` | Shows the containers, the listening sockets, the cron entry and the last sync results. |
| `bash nb-node.sh logs` | Shows the last container logs (`init_data`, `graph`, `api`). |
| `bash nb-node.sh down` | Stops the node and keeps its volumes. |
| `bash nb-node.sh wipe` | Stops the node and removes its volumes. The next `sync` reloads it. |

### Exit codes of `sync`

| Code | Meaning |
|---|---|
| `0` | Success, or nothing to do |
| `1` | Fatal error (configuration, token, download, no JSONLD built, API not answering). The running node is left as it was, unless the error happened after the reload started. |
| `2` | Node reloaded, but at least one study failed to build (see the log) |

## How a sync works

1. Takes a lock (`state/.lock`), so two syncs never run at the same time.
2. Gets a `client_credentials` token from Keycloak, then downloads into `state/staging/`:
   - `studies` (the list of exported studies) and `participants.json` (the data dictionary)
   - for each study: `participants.tsv`, `dataset_description.json`, `imaging.tsv`
3. If no study is exported, the node is stopped and its data is removed.
4. If the inputs have the same hash as the last successful sync and the API answers, it stops here.
5. Builds one `study-<id>.jsonld` per study with `bagel pheno`, then `bagel bids` if the study has imaging rows.
   If a study fails to build, its previous JSONLD is kept when there is one.
6. Restarts the node on the new `data/` folder and waits until it answers queries, then logs a summary
   of the datasets it serves.

Nothing on the running node changes before at least one JSONLD has been built, so a Shanoir outage or a
failed download leaves the node as it was.

## Files

| Path | Content |
|---|---|
| `nb.conf` | Configuration and secret (`chmod 600`, never commit it) |
| `data/` | JSONLD files loaded by the node |
| `state/nb-node.log` | Log of every sync, rotated at 5 MB (`nb-node.log.1`) |
| `state/last/` | Inputs of the last reload, for debugging |
| `state/inputs.sha256` | Hash of the last loaded inputs |
| `$RECIPES/` | `neurobagel/recipes` clone, its `.env` and the GraphDB secrets |

The node runs as compose project `shanoir_nb_node`. The API listens on `127.0.0.1:$API_PORT` only and
GraphDB (7200) is not exposed. Its docs are at `http://127.0.0.1:8000/docs`.

## Querying the node

```bash
python3 query.py                              # every dataset
python3 query.py --modality T1w --min-age 35  # filtered
python3 query.py --port 8001                  # other API port (or API_PORT=8001)
```

Modalities: `T1w T2w dwi bold asl eeg meg pet`.

## Troubleshooting

- **Check the last sync:** `bash nb-node.sh status`, then `tail -n 100 state/nb-node.log`.
- **`token request failed`:** wrong `SHANOIR_URL`, `NB_CLIENT_ID` or `NB_CLIENT_SECRET`, or the client
  has no service account.
- **`another sync is running`:** a sync is still running (`pgrep -af "nb-node.sh sync"`). The lock is released when
  that process exits, so there is never a stale lock to delete.
- **`API did not answer`:** run `bash nb-node.sh logs`. GraphDB can take a while to load large datasets.
- **Cron does nothing:** check that `docker info` (or `sudo -n docker info`) works for the user without a
  password.
- **`imaging subjects missing from participants.tsv`:** the Shanoir export of that study is inconsistent.
  Fix it in Shanoir; until then the node keeps that study's previous JSONLD.
