#!/bin/bash
#
# Create a Shanoir admin user in Keycloak via the Admin REST API.
#
# This automates the manual "FIRST RUN" steps described in the README:
#   1. obtain an admin token
#   2. create a user in the shanoir-ng realm
#   3. set a permanent password
#   4. assign ROLE_ADMIN
#   5. set the required "userId" attribute
#
# The script reads Keycloak credentials from the environment variables
# set in .env (SHANOIR_KEYCLOAK_USER / SHANOIR_KEYCLOAK_PASSWORD).

set -euo pipefail

KC_BASE="${KC_BASE:-http://localhost:8080/auth}"
REALM="shanoir-ng"
NEW_USER="${SHANOIR_TEST_USER:-shanoir-admin}"
NEW_PASS="${SHANOIR_TEST_PASSWORD:-Ch4ng3M3!@2025}"
USER_ID_ATTR="${SHANOIR_TEST_USER_ID:-1}"

# Read keycloak admin credentials from .env for any that are not already set.
# We cannot simply 'source .env' because the password may contain special
# shell characters (e.g. '&') that would be misinterpreted by bash.
_read_env() { grep -E "^$1=" .env | head -1 | sed "s/^$1=//"; }
: "${SHANOIR_KEYCLOAK_USER:=$(_read_env SHANOIR_KEYCLOAK_USER)}"
: "${SHANOIR_KEYCLOAK_PASSWORD:=$(_read_env SHANOIR_KEYCLOAK_PASSWORD)}"

KC_ADMIN_USER="${SHANOIR_KEYCLOAK_USER:?SHANOIR_KEYCLOAK_USER is required}"
KC_ADMIN_PASS="${SHANOIR_KEYCLOAK_PASSWORD:?SHANOIR_KEYCLOAK_PASSWORD is required}"

die() { echo "FATAL: $*" >&2; exit 1; }

# curl_api URL [extra curl args...]
# Makes a curl call, captures body in $RESP_BODY and HTTP code in $RESP_CODE.
RESP_BODY="" RESP_CODE=""
curl_api() {
    local url="$1"; shift
    local tmp; tmp=$(mktemp)
    RESP_CODE=$(curl -sS -o "$tmp" -w "%{http_code}" "$url" "$@") || {
        rm -f "$tmp"; die "curl failed for $url"
    }
    RESP_BODY=$(cat "$tmp"); rm -f "$tmp"
}

wait_keycloak_ready() {
    local timeout="${1:-120}"
    local url="${KC_BASE}/realms/master"
    local elapsed=0
    echo "Waiting for Keycloak API at ${url} (timeout: ${timeout}s)..."
    while [ "$elapsed" -lt "$timeout" ]; do
        local code
        code=$(curl -sf -o /dev/null -w "%{http_code}" --max-time 5 "$url" 2>/dev/null || echo "000")
        if [ "$code" = "200" ]; then
            echo "Keycloak API is ready (after ${elapsed}s)"
            return 0
        fi
        sleep 3
        elapsed=$((elapsed + 3))
    done
    die "Keycloak API not ready after ${timeout}s (last HTTP ${code})"
}

get_token() {
    local resp
    resp=$(curl -sf -X POST "${KC_BASE}/realms/master/protocol/openid-connect/token" \
        --data-urlencode "client_id=admin-cli" \
        --data-urlencode "username=${KC_ADMIN_USER}" \
        --data-urlencode "password=${KC_ADMIN_PASS}" \
        --data-urlencode "grant_type=password") || die "failed to obtain admin token"
    echo "$resp" | jq -r '.access_token'
}

wait_keycloak_ready

echo "Obtaining admin token..."
TOKEN=$(get_token)

if [ "${CI:-}" = "true" ]; then
    echo "Disabling SSL requirement for realm '${REALM}' (CI only)..."
    curl -sf -X PUT \
        "${KC_BASE}/admin/realms/${REALM}" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{"sslRequired":"NONE"}' || die "failed to disable SSL requirement"
fi

echo "Creating user '${NEW_USER}' in realm '${REALM}'..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    "${KC_BASE}/admin/realms/${REALM}/users" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"${NEW_USER}\",
        \"email\": \"${NEW_USER}@shanoir-test.local\",
        \"enabled\": true,
        \"firstName\": \"Shanoir\",
        \"lastName\": \"Admin\",
        \"requiredActions\": [],
        \"attributes\": {
            \"userId\": [\"${USER_ID_ATTR}\"]
        }
    }")

case "$HTTP_CODE" in
    201) echo "User created." ;;
    409) echo "User already exists, continuing." ;;
    *)   die "user creation returned HTTP ${HTTP_CODE}" ;;
esac

echo "Looking up user ID..."
USER_UUID=$(curl -sf "${KC_BASE}/admin/realms/${REALM}/users?username=${NEW_USER}&exact=true" \
    -H "Authorization: Bearer ${TOKEN}" | jq -r '.[0].id') \
    || die "failed to look up user"
[ "$USER_UUID" != "null" ] || die "user not found after creation"
echo "  -> ${USER_UUID}"

echo "Setting permanent password..."
PASS_BODY=$(jq -nc --arg pass "$NEW_PASS" '{"type":"password","value":$pass,"temporary":false}')
curl_api "${KC_BASE}/admin/realms/${REALM}/users/${USER_UUID}/reset-password" \
    -X PUT \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$PASS_BODY"
case "$RESP_CODE" in
    204) echo "Password set." ;;
    *)   die "failed to set password (HTTP ${RESP_CODE}): ${RESP_BODY}" ;;
esac

echo "Assigning ROLE_ADMIN..."
ROLE_JSON=$(curl -sf "${KC_BASE}/admin/realms/${REALM}/roles/ROLE_ADMIN" \
    -H "Authorization: Bearer ${TOKEN}") \
    || die "ROLE_ADMIN not found in realm"

curl -sf -X POST \
    "${KC_BASE}/admin/realms/${REALM}/users/${USER_UUID}/role-mappings/realm" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "[${ROLE_JSON}]" || die "failed to assign role"

echo "Keycloak user '${NEW_USER}' ready with ROLE_ADMIN."

echo "Verifying login for '${NEW_USER}'..."
curl_api "${KC_BASE}/realms/${REALM}/protocol/openid-connect/token" \
    -X POST \
    --data-urlencode "client_id=shanoir-swagger" \
    --data-urlencode "username=${NEW_USER}" \
    --data-urlencode "password=${NEW_PASS}" \
    --data-urlencode "grant_type=password"
case "$RESP_CODE" in
    200) ;;
    *)   die "login verification returned HTTP ${RESP_CODE}: ${RESP_BODY}" ;;
esac
USER_TOKEN=$(echo "$RESP_BODY" | jq -r '.access_token')
[ "$USER_TOKEN" != "null" ] && [ -n "$USER_TOKEN" ] \
    || die "login returned null/empty token for '${NEW_USER}'"
echo "Login verified."
