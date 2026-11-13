#!/usr/bin/env bash
# Shared HTTPS reverse proxy for the public Neurobagel nodes of nb-node.sh: nginx-proxy + acme-companion
# (Let's Encrypt), from the neurobagel/recipes docker-compose.proxy.yml. One proxy serves every instance.
#
#   bash nb-proxy.sh up          # install if needed, then start (or restart) the proxy
#   bash nb-proxy.sh status      # containers, routed nodes and whether their public URL answers
#   bash nb-proxy.sh logs
#   bash nb-proxy.sh down        # stop the proxy: every public node becomes unreachable (nodes keep running)
#   bash nb-proxy.sh uninstall   # remove the proxy, its certificates and ~/neurobagel-proxy (no node may use it; YES=1: no prompt)
#
# Config (optional): nb-proxy.conf next to this script, see nb-proxy.conf.example.
# Folder: ~/neurobagel-proxy (PROXY_HOME to change it) = recipes clone + compose.local.yml. Compose project: shanoir_nb_proxy.
# It creates the docker network NB_proxynet. A node joins it when its nb-<instance>.conf sets NB_NAPI_DOMAIN:
# nginx-proxy routes https://NB_NAPI_DOMAIN/NB_NAPI_BASE_PATH to it and acme-companion gets the certificate.
# Needs: a DNS entry of each domain to this machine, and ports 80 + 443 open from the internet (Let's Encrypt uses 80).
# The proxy containers read the docker socket (that is how they discover the nodes).
set -euo pipefail
umask 022
export PATH="$HOME/.local/bin:/usr/local/bin:/usr/bin:/bin:$PATH"

HERE="$(cd "$(dirname "$0")" && pwd)"
SELF="$HERE/$(basename "$0")"
CONF="$HERE/nb-proxy.conf"
# shellcheck source=/dev/null
[ -f "$CONF" ] && . "$CONF"

PROXY_HOME="$(realpath -m -- "${PROXY_HOME:-$HOME/neurobagel-proxy}")"
RECIPES="$PROXY_HOME/recipes"
LOCAL_COMPOSE="$PROXY_HOME/compose.local.yml"
PROJECT="shanoir_nb_proxy"
PROXY_NET="NB_proxynet"
LETSENCRYPT_EMAIL="${LETSENCRYPT_EMAIL:-}"
NGINX_PROXY_TAG="${NGINX_PROXY_TAG:-latest}"
ACME_COMPANION_TAG="${ACME_COMPANION_TAG:-latest}"

log() { echo "$(date '+%F %T') [nb:proxy] $*"; }
die() { log "ERROR: $*" >&2; exit 1; }
usage() { awk 'NR > 1 && !/^#/ { exit } NR > 1' "$SELF"; exit 1; }

if docker info >/dev/null 2>&1; then DOCKER=(docker)
elif [ -t 0 ]; then DOCKER=(sudo docker)
else DOCKER=(sudo -n docker)
fi

check_conf() {
    [[ -z $LETSENCRYPT_EMAIL || $LETSENCRYPT_EMAIL =~ ^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$ ]] \
        || die "LETSENCRYPT_EMAIL in $CONF: an email address, or empty"
    [[ $NGINX_PROXY_TAG =~ ^[A-Za-z0-9._-]+$ ]] || die "NGINX_PROXY_TAG in $CONF: an image tag, e.g. 1.6"
    [[ $ACME_COMPANION_TAG =~ ^[A-Za-z0-9._-]+$ ]] || die "ACME_COMPANION_TAG in $CONF: an image tag, e.g. 2.5"
}

local_compose() {  # image versions and Let's Encrypt account email, on top of the recipes' proxy file
    cat <<EOF
# written by nb-proxy.sh from $CONF: edit the config, not this file
services:
  nginx-proxy:
    image: nginxproxy/nginx-proxy:$NGINX_PROXY_TAG
  acme-companion:
    image: nginxproxy/acme-companion:$ACME_COMPANION_TAG
EOF
    if [ -n "$LETSENCRYPT_EMAIL" ]; then
        printf '    environment:\n      DEFAULT_EMAIL: "%s"\n' "$LETSENCRYPT_EMAIL"
    fi
}

compose() {
    (cd "$RECIPES" && "${DOCKER[@]}" compose -p "$PROJECT" -f docker-compose.proxy.yml -f "$LOCAL_COMPOSE" "$@")
}

running() { [ -n "$(compose ps -q --status running nginx-proxy 2>/dev/null)" ]; }

routed_nodes() {  # "<container> https://<host><path>" for every container nginx-proxy routes
    local c
    for c in $("${DOCKER[@]}" network inspect "$PROXY_NET" -f '{{range .Containers}}{{.Name}} {{end}}' 2>/dev/null); do
        "${DOCKER[@]}" inspect "$c" -f '{{range .Config.Env}}{{println .}}{{end}}' \
            | awk -v c="$c" -F= '$1=="VIRTUAL_HOST"{h=$2} $1=="VIRTUAL_PATH"{p=$2} END{if(h!="") print c, "https://" h (p=="/"?"":p)}'
    done
}

latest_tag() {
    git ls-remote --tags --refs --sort=-v:refname "$1" | head -1 | sed 's|.*refs/tags/||'
}

cmd_up() {
    check_conf
    command -v git >/dev/null || die "git not found"
    "${DOCKER[@]}" compose version >/dev/null || die "docker compose not available (or sudo needs a password)"
    mkdir -p "$PROXY_HOME"
    if [ ! -d "$RECIPES" ]; then
        local tag; tag=$(latest_tag https://github.com/neurobagel/recipes.git)
        [ -n "$tag" ] || die "could not read the neurobagel/recipes tags (network?)"
        log "cloning neurobagel/recipes $tag into $RECIPES..."
        git -c advice.detachedHead=false clone -q --depth 1 --branch "$tag" https://github.com/neurobagel/recipes.git "$RECIPES"
    fi
    [ -f "$RECIPES/docker-compose.proxy.yml" ] || die "$RECIPES has no docker-compose.proxy.yml (recipes release too old?)"
    local svc
    for svc in nginx-proxy acme-companion; do
        (cd "$RECIPES" && "${DOCKER[@]}" compose -f docker-compose.proxy.yml config --services) | grep -qx "$svc" \
            || die "no '$svc' service in $RECIPES/docker-compose.proxy.yml: this script needs updating for that recipes release"
    done
    local_compose > "$LOCAL_COMPOSE"
    [ -n "$LETSENCRYPT_EMAIL" ] || log "WARNING: no LETSENCRYPT_EMAIL in $CONF: no warning from Let's Encrypt if renewals fail"

    if ! running && ss -tln 2>/dev/null | grep -qE ':(80|443)\s'; then
        die "port 80 or 443 is already used on this machine (another web server?): free them for the proxy"
    fi
    log "starting nginx-proxy $NGINX_PROXY_TAG and acme-companion $ACME_COMPANION_TAG..."
    compose up -d --remove-orphans
    log "proxy up, network $PROXY_NET ready. Public nodes: set NB_NAPI_DOMAIN in their config, then: bash nb-node.sh sync <instance>"
    show_nodes
}

show_nodes() {
    local nodes; nodes=$(routed_nodes)
    if [ -z "$nodes" ]; then
        log "no public node attached yet"
        return
    fi
    log "public nodes:"
    local c url code err
    while read -r c url; do
        err=$(mktemp)
        code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 15 -X POST -H 'Content-Type: application/json' \
            -d '{}' "$url/datasets" 2>"$err" || true)
        if [ "$code" = 000 ] || [ -z "$code" ]; then
            code="no answer: $(tail -n 1 "$err" | sed 's/^curl: ([0-9]*) //')"
        fi
        rm -f "$err"
        printf '    %-40s %s  (POST /datasets: %s)\n' "$c" "$url" "$code"
    done <<< "$nodes"
}

cmd_status() {
    [ -d "$RECIPES" ] || die "proxy not installed: bash $0 up"
    compose ps -a
    show_nodes
}

cmd_down() {
    [ -d "$RECIPES" ] || die "proxy not installed"
    # stop only: the network stays, so the nodes attached to it keep running and come back with 'up'
    log "stopping the proxy: the public nodes are unreachable until: bash $0 up"
    compose stop
}

safe_dir() {
    local p; p=$(realpath -m -- "$1")
    [ -n "$1" ] && [ "$p" != / ] && [ "$p" != "$(realpath -m -- "$HOME")" ] \
        && [ "$p" != "$HERE" ] && [[ $HERE != "$p"/* ]]
}

cmd_uninstall() {
    safe_dir "$PROXY_HOME" || die "refusing to delete '$PROXY_HOME'"
    local nodes; nodes=$(routed_nodes)
    [ -z "$nodes" ] || die "nodes still use the proxy, remove NB_NAPI_DOMAIN from their config and sync them first:
$nodes"
    echo "This removes the proxy containers, its network $PROXY_NET, its volumes (certificates included),"
    echo "its images, and $PROXY_HOME."
    if [ "${YES:-0}" != 1 ]; then
        [ -t 0 ] || die "not interactive: run with YES=1 to confirm"
        read -rp "Type 'uninstall' to confirm: " answer
        [ "$answer" = uninstall ] || die "aborted"
    fi
    if [ -f "$RECIPES/docker-compose.proxy.yml" ]; then
        local_compose > "$LOCAL_COMPOSE"
        compose down -v --remove-orphans --rmi all || log "WARNING: compose down failed, cleaning by label"
    fi
    local label="label=com.docker.compose.project=$PROJECT" ids
    ids=$("${DOCKER[@]}" ps -aq --filter "$label"); [ -z "$ids" ] || "${DOCKER[@]}" rm -f $ids >/dev/null
    ids=$("${DOCKER[@]}" volume ls -q --filter "$label"); [ -z "$ids" ] || "${DOCKER[@]}" volume rm $ids >/dev/null
    "${DOCKER[@]}" network rm "$PROXY_NET" >/dev/null 2>&1 || true
    rm -rf -- "$PROXY_HOME"
    log "proxy removed"
}

case "${1:-}" in
    up)        cmd_up ;;
    status)    cmd_status ;;
    logs)      [ -d "$RECIPES" ] || die "proxy not installed"; compose logs --no-color --tail 100 ;;
    down)      cmd_down ;;
    uninstall) cmd_uninstall ;;
    *)         usage ;;
esac
