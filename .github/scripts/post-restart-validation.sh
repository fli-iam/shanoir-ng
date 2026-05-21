#!/bin/bash
#
# Minimal checks after "docker compose restart" to verify persistence.
# Requires a prior successful functional-tests.sh user lifecycle that wrote
# CI_LIFECYCLE_STATE_FILE (default /tmp/shanoir-ci-lifecycle.env).
#
# Usage (from repo root, after "docker compose restart"):
#   bash .github/scripts/helpers.sh wait_for_shanoir_password_token 240
#   bash .github/scripts/helpers.sh wait_for_url https://shanoir-ng-nginx/ 180
#   bash .github/scripts/post-restart-validation.sh
#
# Or rely on the built-in readiness waits below (do not use wait_for_log after restart:
# Docker keeps pre-restart log lines and matches while services are still booting).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

CI_LIFECYCLE_STATE_FILE="${CI_LIFECYCLE_STATE_FILE:-/tmp/shanoir-ci-lifecycle.env}"
REALM="shanoir-ng"
NGINX_BASE="https://shanoir-ng-nginx"
KC_INTERNAL_BASE="http://keycloak:8080/auth"
TEST_USER="${SHANOIR_TEST_USER:-shanoir-admin}"
TEST_PASS="${SHANOIR_TEST_PASSWORD:-Ch4ng3M3!@2025}"

FAILURES=0
TESTS=0
pass() { TESTS=$((TESTS + 1)); echo "[PASS] $1"; }
fail() { TESTS=$((TESTS + 1)); FAILURES=$((FAILURES + 1)); echo "[FAIL] $1"; }

docker_curl() {
    if [ "$(docker inspect shanoir-ng-nginx --format '{{.State.Running}}' 2>/dev/null)" != "true" ]; then
        echo "ERROR: container shanoir-ng-nginx is not running; cannot reach Keycloak from inside the stack." >&2
        docker ps -a --filter name=shanoir-ng-nginx --format 'table {{.Names}}\t{{.Status}}\t{{.ID}}' >&2 || true
        echo "Hint: docker logs shanoir-ng-nginx && docker compose up -d nginx" >&2
        return 1
    fi
    docker exec shanoir-ng-nginx curl -s "$@"
}

echo "======== Post-restart validation ========"

echo ""
echo "---- Waiting for stack readiness after restart ----"
bash "$SCRIPT_DIR/helpers.sh" wait_for_shanoir_password_token "${POST_RESTART_TOKEN_TIMEOUT:-240}"
bash "$SCRIPT_DIR/helpers.sh" wait_for_url https://shanoir-ng-nginx/ "${POST_RESTART_URL_TIMEOUT:-180}"

if [ ! -f "$CI_LIFECYCLE_STATE_FILE" ]; then
    echo "No lifecycle state file at $CI_LIFECYCLE_STATE_FILE — skipping CI user checks."
fi

echo ""
echo "---- Admin token after restart ----"
body=$(docker_curl -X POST \
    "${KC_INTERNAL_BASE}/realms/${REALM}/protocol/openid-connect/token" \
    --data-urlencode "client_id=shanoir-swagger" \
    --data-urlencode "username=${TEST_USER}" \
    --data-urlencode "password=${TEST_PASS}" \
    --data-urlencode "grant_type=password") || body=""

TOKEN=$(echo "$body" | jq -r '.access_token // empty')
users_tmp=""

if [ -n "$TOKEN" ]; then
    pass "Admin token acquired after restart"
else
    fail "Admin token failed after restart"
    echo "$body" | jq . 2>/dev/null || echo "$body"
fi

if [ -n "$TOKEN" ]; then
    echo ""
    echo "---- GET /users after restart ----"
    users_tmp=$(mktemp)
    users_timeout="${POST_RESTART_USERS_TIMEOUT:-180}"
    users_elapsed=0
    code="000"
    while [ "$users_elapsed" -lt "$users_timeout" ]; do
        code=$(curl -4sk -o "$users_tmp" -w "%{http_code}" --max-time 20 \
            -H "Authorization: Bearer ${TOKEN}" \
            "${NGINX_BASE}/shanoir-ng/users/users") || code="000"
        if echo "$code" | grep -qxE "200|204"; then
            break
        fi
        if [ $((users_elapsed % 30)) -eq 0 ] && [ "$users_elapsed" -gt 0 ]; then
            echo "  ... /users still HTTP ${code} (${users_elapsed}s; microservices may still be starting)"
        fi
        sleep 5
        users_elapsed=$((users_elapsed + 5))
    done
    if echo "$code" | grep -qxE "200|204"; then
        pass "GET /users after restart (HTTP $code, after ${users_elapsed}s)"
    else
        fail "GET /users after restart — HTTP $code (waited ${users_elapsed}s)"
    fi
fi

if [ -f "$CI_LIFECYCLE_STATE_FILE" ] && [ -n "$users_tmp" ] && [ -f "$users_tmp" ]; then
    # shellcheck source=/dev/null
    source "$CI_LIFECYCLE_STATE_FILE"
    if [ -n "${CI_LIFECYCLE_USER:-}" ] && [ -n "${CI_LIFECYCLE_PASS:-}" ]; then
        echo ""
        echo "---- CI lifecycle user still listed ----"
        if jq -e --arg u "$CI_LIFECYCLE_USER" '.[] | select(.username == $u)' "$users_tmp" >/dev/null 2>&1; then
            pass "Lifecycle user '$CI_LIFECYCLE_USER' still in /users list"
        else
            fail "Lifecycle user '$CI_LIFECYCLE_USER' missing from /users list"
        fi

        echo ""
        echo "---- Lifecycle user token after restart ----"
        ub=$(docker_curl -X POST \
            "${KC_INTERNAL_BASE}/realms/${REALM}/protocol/openid-connect/token" \
            --data-urlencode "client_id=shanoir-swagger" \
            --data-urlencode "username=${CI_LIFECYCLE_USER}" \
            --data-urlencode "password=${CI_LIFECYCLE_PASS}" \
            --data-urlencode "grant_type=password") || ub=""
        ut=$(echo "$ub" | jq -r '.access_token // empty')
        if [ -n "$ut" ]; then
            pass "Token for lifecycle user after restart"
        else
            fail "Token for lifecycle user failed after restart"
            echo "$ub" | jq . 2>/dev/null || echo "$ub"
        fi
    fi
fi

rm -f "$users_tmp"

echo ""
echo "======== Post-restart results: $FAILURES failure(s) / $TESTS test(s) ========"
if [ "$FAILURES" -gt 0 ]; then
    exit 1
fi
exit 0
