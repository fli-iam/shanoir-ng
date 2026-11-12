#!/usr/bin/env bash
# Neurobagel node fed by Shanoir, in one script: install, fetch + bagel build + (re)load, made for cron.
# Replaces fetch.sh + run.sh + nb-docker.sh (same compose project, same data/ folder).
#
#   bash nb-node.sh install        # once (sync also does it if needed): recipes, .env, GraphDB passwords, images
#   bash nb-node.sh sync           # fetch every exported study, build the JSONLDs, reload the node if the inputs changed
#   FORCE=1 bash nb-node.sh sync   # reload even if nothing changed
#   bash nb-node.sh cron-install   # daily sync in the user's crontab, at 04:20 (CRON_AT="30 5" to change)
#   bash nb-node.sh cron-remove
#   bash nb-node.sh status | logs | down | wipe
#   bash nb-node.sh uninstall      # remove everything above: cron, containers, volumes, images, recipes, data, state, nb.conf (YES=1 to skip the prompt)
#
# Config: nb.conf next to this script (chmod 600), see nb.conf.example.
# Node: compose project 'shanoir_nb_node' (never the recipes' default 'neurobagel_node'), API on 127.0.0.1 only.
# Files: data/ = JSONLDs the node loads; state/ = log, last downloaded inputs, lock (private, 700).
# Cron cannot type a sudo password: the user must be in the docker group, or 'sudo -n docker' must work (NOPASSWD).
#
# A sync never breaks a running node on a download error: everything is fetched and built in state/staging
# first, the node is only touched once at least one JSONLD is ready. A study whose build fails keeps its
# previous JSONLD if there is one (exit code 2, see the log). A study no longer exposed disappears from the node.
set -euo pipefail
umask 022
export PATH="$HOME/.local/bin:/usr/local/bin:/usr/bin:/bin:$PATH"

HERE="$(cd "$(dirname "$0")" && pwd)"
SELF="$HERE/$(basename "$0")"
CONF="${NB_CONF:-$HERE/nb.conf}"
# shellcheck source=/dev/null
[ -f "$CONF" ] && . "$CONF"

RECIPES="${RECIPES:-$HOME/neurobagel-recipes}"
NB_DATA="${NB_DATA:-$HERE/data}"
STATE="$HERE/state"
STAGE="$STATE/staging"
LOG="$STATE/nb-node.log"
PROJECT="shanoir_nb_node"
API_PORT="${API_PORT:-8000}"
NB_CLIENT_ID="${NB_CLIENT_ID:-neurobagel-export}"
BAGEL_IMAGE="${BAGEL_IMAGE:-neurobagel/bagelcli:latest}"

log() { echo "$(date '+%F %T') [nb] $*"; }
die() { log "ERROR: $*" >&2; exit 1; }

# docker without sudo if allowed, else sudo (non-interactive under cron)
if docker info >/dev/null 2>&1; then DOCKER=(docker)
elif [ -t 0 ]; then DOCKER=(sudo docker)
else DOCKER=(sudo -n docker)
fi

compose() {
    (cd "$RECIPES" && "${DOCKER[@]}" compose -p "$PROJECT" -f docker-compose.noproxy.prod.yml "$@")
}

api_ok() {
    curl -sf -o /dev/null --max-time 10 -X POST -H 'Content-Type: application/json' -d '{}' \
        "http://127.0.0.1:$API_PORT/datasets"
}

wait_api() {
    # the API answers before GraphDB has finished loading: wait for a real query to succeed
    log "waiting for the node to answer queries (GraphDB setup and data load first)..."
    for _ in $(seq 1 150); do
        api_ok && { log "node ready: http://127.0.0.1:$API_PORT/docs"; return; }
        sleep 2
    done
    die "API did not answer, see: bash $0 logs"
}

sync_data_path() {
    sed -i "s|^LOCAL_GRAPH_DATA=.*|LOCAL_GRAPH_DATA=$NB_DATA|" "$RECIPES/.env"
}

rm_any() {
    # GraphDB may write in the data folder as another user: plain rm may not be enough
    rm -rf -- "$@" 2>/dev/null && return
    local sudo=(sudo -n); [ -t 0 ] && sudo=(sudo)
    "${sudo[@]}" rm -rf -- "$@" 2>/dev/null || log "WARNING: could not delete $*"
}

# ---------------------------------------------------------------- install

latest_tag() {
    git ls-remote --tags --refs --sort=-v:refname "$1" | head -1 | sed 's|.*refs/tags/||'
}

cmd_install() {
    command -v git >/dev/null || die "git not found"
    command -v python3 >/dev/null || die "python3 not found"
    "${DOCKER[@]}" compose version >/dev/null || die "docker compose not available (or sudo needs a password)"
    if [ ! -d "$RECIPES" ]; then
        local tag; tag=$(latest_tag https://github.com/neurobagel/recipes.git)
        [ -n "$tag" ] || die "could not read the neurobagel/recipes tags (network?)"
        log "cloning neurobagel/recipes $tag into $RECIPES..."
        git -c advice.detachedHead=false clone -q --depth 1 --branch "$tag" https://github.com/neurobagel/recipes.git "$RECIPES"
    fi

    log "writing $RECIPES/.env"
    mkdir -p "$NB_DATA"
    cat > "$RECIPES/.env" <<EOF
COMPOSE_PROJECT_NAME=$PROJECT
COMPOSE_PROFILES=node
NB_GRAPH_USERNAME=nbuser
NB_GRAPH_DB=repositories/my_db
NB_GRAPH_MEMORY=2G
LOCAL_GRAPH_DATA=$NB_DATA
NB_RETURN_AGG=true
NB_MIN_CELL_SIZE=0
NB_NAPI_PORT_HOST=127.0.0.1:$API_PORT
# unused by the node profile, set empty only to silence the compose warnings
NB_NAPI_BASE_PATH=
NB_FAPI_DOMAIN=
NB_FAPI_BASE_PATH=
NB_QUERY_CLIENT_ID=
NB_QUERY_HEADER_SCRIPT=
EOF

    # the recipes ship placeholder passwords (ADMINPASSWORD / DBPASSWORD): replace them once
    if grep -qx ADMINPASSWORD "$RECIPES/secrets/NB_GRAPH_ADMIN_PASSWORD.txt" 2>/dev/null; then
        log "generating the GraphDB passwords in $RECIPES/secrets"
        python3 -c 'import secrets; print(secrets.token_urlsafe(24), end="")' > "$RECIPES/secrets/NB_GRAPH_ADMIN_PASSWORD.txt"
        python3 -c 'import secrets; print(secrets.token_urlsafe(24), end="")' > "$RECIPES/secrets/NB_GRAPH_PASSWORD.txt"
        chmod 644 "$RECIPES"/secrets/*.txt   # read by the containers' own users
    fi

    log "checking the compose config..."
    compose config --quiet
    log "pulling the api and graph images..."
    compose pull api graph
    log "building the init_data image (local Dockerfile of the recipes)..."
    compose build init_data
    if ! command -v bagel >/dev/null; then
        log "no bagel CLI in PATH: pulling $BAGEL_IMAGE"
        "${DOCKER[@]}" pull -q "$BAGEL_IMAGE"
    fi
    log "install done. Next: write $CONF, then: bash $0 sync"
}

# ---------------------------------------------------------------- fetch

token() {
    # secret on stdin, not in the process list
    printf %s "$NB_CLIENT_SECRET" | curl -sS --fail-with-body --max-time 60 \
        -d grant_type=client_credentials -d client_id="$NB_CLIENT_ID" --data-urlencode client_secret@- \
        "$SHANOIR_URL/auth/realms/shanoir-ng/protocol/openid-connect/token" \
        | python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])'
}

get() {  # get <api path> <output file>; token in a curl config on stdin, not in the process list
    printf 'header = "Authorization: Bearer %s"\n' "$TOKEN" \
        | curl -sS --fail-with-body --max-time 900 -K - -o "$2" "$API/$1"
}

study_ids() {
    python3 -c 'import json,sys; print(" ".join(str(s["id"]) for s in json.load(open(sys.argv[1]))))' "$1"
}

fetch() {
    rm_any "$STAGE"
    mkdir -p "$STAGE/studies" "$STAGE/data"
    API="$SHANOIR_URL/shanoir-ng/studies/neurobagel"

    log "fetching from $SHANOIR_URL"
    TOKEN=$(token) || die "token request failed (client $NB_CLIENT_ID)"
    get studies "$STAGE/studies.json" || die "could not list the exported studies"
    python3 -c 'import json,sys
for s in json.load(open(sys.argv[1])): print("  exported: %s %s" % (s["id"], s["name"]))' "$STAGE/studies.json"
    get participants.json "$STAGE/participants.json" || die "could not download participants.json"

    local id d f
    for id in $(study_ids "$STAGE/studies.json"); do
        [[ $id =~ ^[0-9]+$ ]] || die "unexpected study id: $id"
        # new token per study: a slow imaging.tsv must not make the next downloads expire
        TOKEN=$(token) || die "token request failed"
        d="$STAGE/studies/study-$id"
        mkdir -p "$d"
        for f in participants.tsv dataset_description.json imaging.tsv; do
            get "study/$id/$f" "$d/$f" || die "study $id: $f download failed"
        done
        log "  study $id: $(($(wc -l < "$d/participants.tsv") - 1)) participants," \
            "$(($(wc -l < "$d/imaging.tsv") - 1)) imaging rows"
    done
}

inputs_hash() {
    (cd "$STAGE" && find participants.json studies -type f | sort | xargs sha256sum | sha256sum | cut -c1-64)
}

# ---------------------------------------------------------------- build

run_bagel() {
    if command -v bagel >/dev/null; then
        bagel "$@"
    else
        "${DOCKER[@]}" run --rm --user "$(id -u):$(id -g)" --volume "$STAGE:$STAGE" -w "$STAGE" "$BAGEL_IMAGE" "$@"
    fi
}

build_study() {  # returns 1 on failure (no set -e inside an 'if')
    local id=$1
    local d="$STAGE/studies/study-$id" out="$STAGE/data/study-$id.jsonld" tmp="$STAGE/study-$id.tmp.jsonld"
    local rows missing
    rows=$(awk -F'\t' 'NR>1 && NF>=4' "$d/imaging.tsv" | wc -l)
    missing=$(comm -23 <(awk -F'\t' 'NR>1{print $1}' "$d/imaging.tsv" | sort -u) \
                       <(awk -F'\t' 'NR>1{print $1}' "$d/participants.tsv" | sort -u))
    if [ -n "$missing" ]; then
        log "study $id: imaging subjects missing from participants.tsv: $(echo "$missing" | tr '\n' ' ')"
        return 1
    fi
    run_bagel pheno --pheno "$d/participants.tsv" --dictionary "$STAGE/participants.json" \
        --dataset-description "$d/dataset_description.json" --output "$tmp" --overwrite || return 1
    if [ "$rows" -gt 0 ]; then
        run_bagel bids --jsonld-path "$tmp" --bids-table "$d/imaging.tsv" \
            --dataset-source-dir "/shanoir/study-$id" --output "$tmp" --overwrite || return 1
    else
        log "study $id: no imaging rows, phenotypic only"
    fi
    mv "$tmp" "$out"
    [ -f "$HERE/inspect_jsonld.py" ] && python3 "$HERE/inspect_jsonld.py" "$out" | sed 's/^/    /'
    return 0
}

# ---------------------------------------------------------------- sync

summary() {
    curl -sf --max-time 30 -X POST -H 'Content-Type: application/json' -d '{}' \
        "http://127.0.0.1:$API_PORT/datasets" | python3 -c 'import json,sys
for d in json.load(sys.stdin):
    m = ", ".join(sorted(x.split("#")[-1] for x in d["image_modals"])) or "none"
    print("    %s: %s subjects | modalities: %s" % (d["dataset_name"], d["dataset_total_subjects"], m))' \
        || log "could not read the node summary"
}

start_log() {
    mkdir -p "$STATE"
    chmod 700 "$STATE"
    if [ -f "$LOG" ] && [ "$(stat -c%s "$LOG")" -gt 5000000 ]; then mv -f "$LOG" "$LOG.1"; fi
    exec > >(tee -a "$LOG") 2>&1
    exec 9>"$STATE/.lock"
    flock -n 9 || die "another sync is running"
}

cmd_sync() {
    start_log
    log "======== sync start ========"
    [ -n "${SHANOIR_URL:-}" ] && [ -n "${NB_CLIENT_SECRET:-}" ] || die "set SHANOIR_URL and NB_CLIENT_SECRET in $CONF"
    SHANOIR_URL="${SHANOIR_URL%/}"
    [ -f "$RECIPES/.env" ] || { log "node not installed yet: installing"; cmd_install; }

    fetch
    local ids; ids=$(study_ids "$STAGE/studies.json")

    if [ -z "$ids" ]; then
        log "no study is exposed to Neurobagel: stopping the node and removing its data"
        compose down -v --remove-orphans || true
        rm_any "$NB_DATA" "$STAGE" "$STATE/inputs.sha256"
        mkdir -p "$NB_DATA"
        log "======== sync done (node stopped, nothing exposed) ========"
        return
    fi

    local hash; hash=$(inputs_hash)
    if [ "${FORCE:-0}" != 1 ] && [ "$hash" = "$(cat "$STATE/inputs.sha256" 2>/dev/null || true)" ] && api_ok; then
        log "inputs unchanged and node up: nothing to do"
        rm_any "$STAGE"
        log "======== sync done (no change) ========"
        return
    fi

    local id failed=()
    for id in $ids; do
        log "---- building study $id"
        if build_study "$id"; then
            :
        elif [ -f "$NB_DATA/study-$id.jsonld" ]; then
            log "study $id: build FAILED, keeping its previous JSONLD"
            cp "$NB_DATA/study-$id.jsonld" "$STAGE/data/"
            failed+=("$id(previous kept)")
        else
            log "study $id: build FAILED, no previous JSONLD: not on the node"
            failed+=("$id(missing)")
        fi
    done
    local n; n=$(find "$STAGE/data" -name '*.jsonld' | wc -l)
    [ "$n" -gt 0 ] || die "no JSONLD could be built: node left as it was"

    log "reloading the node with $n JSONLD file(s)"
    compose down -v --remove-orphans
    rm_any "$NB_DATA.old"
    [ -d "$NB_DATA" ] && mv "$NB_DATA" "$NB_DATA.old"
    mv "$STAGE/data" "$NB_DATA"
    chmod 755 "$NB_DATA"
    chmod 644 "$NB_DATA"/*.jsonld
    rm_any "$STATE/last"
    mv "$STAGE" "$STATE/last"   # keep the last inputs for debugging (state/ is private)
    sync_data_path
    compose up -d
    wait_api
    echo "$hash" > "$STATE/inputs.sha256"
    rm_any "$NB_DATA.old"
    log "node content:"
    summary

    if [ ${#failed[@]} -gt 0 ]; then
        log "======== sync done WITH FAILURES: ${failed[*]} ========"
        exit 2
    fi
    log "======== sync done ========"
}

# ---------------------------------------------------------------- cron, misc

cmd_cron_install() {
    local at="${CRON_AT:-20 4}"
    [ -f "$CONF" ] || die "write $CONF first (see nb.conf.example)"
    if ! docker info >/dev/null 2>&1 && ! sudo -n docker info >/dev/null 2>&1; then
        die "cron could not use docker: add $USER to the docker group, or allow it 'sudo -n docker' (NOPASSWD)"
    fi
    local line="$at * * * /usr/bin/env bash $SELF sync >/dev/null 2>&1"
    { crontab -l 2>/dev/null | grep -vF "$SELF sync" || true; echo "$line"; } | crontab -
    log "crontab entry: $line"
    log "log file: $LOG"
}

cmd_cron_remove() {
    { crontab -l 2>/dev/null | grep -vF "$SELF sync" || true; } | crontab -
    log "crontab entry removed"
}

cmd_status() {
    compose ps -a
    log "listening sockets (API must be 127.0.0.1 only, nothing on 7200):"
    ss -tln 2>/dev/null | grep -E ":($API_PORT|7200)\b" || true
    log "cron: $(crontab -l 2>/dev/null | grep -F "$SELF sync" || echo 'not installed')"
    [ -f "$LOG" ] && { log "last sync lines:"; grep -E '======== sync' "$LOG" | tail -n 4; }
}

cmd_logs() {
    compose logs --no-color init_data graph | tail -n 60
    echo "-----"
    compose logs --no-color api | tail -n 30
}

safe_dir() {  # refuse to delete an empty path, /, $HOME or this script's folder
    local p; p=$(realpath -m -- "$1")
    [ -n "$1" ] && [ "$p" != / ] && [ "$p" != "$(realpath -m -- "$HOME")" ] && [ "$p" != "$HERE" ]
}

cmd_uninstall() {
    local paths=("$NB_DATA" "$NB_DATA.old" "$STATE" "$RECIPES" "$CONF") p
    for p in "${paths[@]}"; do safe_dir "$p" || die "refusing to delete '$p'"; done
    echo "This removes everything this script set up on this machine:"
    echo "  - the cron entry"
    echo "  - the containers, network, volumes and images of compose project '$PROJECT'"
    echo "  - the bagel CLI image $BAGEL_IMAGE"
    printf '  - %s\n' "${paths[@]}"
    if [ "${YES:-0}" != 1 ]; then
        [ -t 0 ] || die "not interactive: run with YES=1 to confirm"
        read -rp "Type 'uninstall' to confirm: " answer
        [ "$answer" = uninstall ] || die "aborted"
    fi

    if [ -d "$STATE" ]; then
        exec 9>>"$STATE/.lock"
        flock -n 9 || die "a sync is running, try again later"
    fi
    cmd_cron_remove
    if [ -f "$RECIPES/docker-compose.noproxy.prod.yml" ]; then
        log "stopping the node and removing its images..."
        compose down -v --remove-orphans --rmi all || log "WARNING: compose down failed, cleaning by label"
    fi
    # leftovers (or recipes already deleted): everything compose labelled with our project
    local label="label=com.docker.compose.project=$PROJECT" ids
    ids=$("${DOCKER[@]}" ps -aq --filter "$label"); [ -z "$ids" ] || "${DOCKER[@]}" rm -f $ids >/dev/null
    ids=$("${DOCKER[@]}" volume ls -q --filter "$label"); [ -z "$ids" ] || "${DOCKER[@]}" volume rm $ids >/dev/null
    ids=$("${DOCKER[@]}" network ls -q --filter "$label"); [ -z "$ids" ] || "${DOCKER[@]}" network rm $ids >/dev/null
    ids=$("${DOCKER[@]}" images -q --filter "$label"); [ -z "$ids" ] || "${DOCKER[@]}" rmi -f $ids >/dev/null
    "${DOCKER[@]}" rmi "$BAGEL_IMAGE" >/dev/null 2>&1 && log "removed image $BAGEL_IMAGE" || true
    exec 9>&-

    log "deleting files..."
    rm_any "${paths[@]}"
    log "uninstall done. Not removed: the scripts in $HERE, and the Keycloak client in Shanoir."
}

case "${1:-}" in
    install)      cmd_install ;;
    uninstall)    cmd_uninstall ;;
    sync)         cmd_sync ;;
    cron-install) cmd_cron_install ;;
    cron-remove)  cmd_cron_remove ;;
    status)       cmd_status ;;
    logs)         cmd_logs ;;
    down)         compose down ;;
    wipe)         compose down -v ;;
    *) sed -n '2,20p' "$0"; exit 1 ;;
esac
