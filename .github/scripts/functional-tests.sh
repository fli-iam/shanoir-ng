#!/bin/bash
#
# Functional tests for the Shanoir NG CI deployment.
#
# Validates that the deployment is actually working beyond basic health checks:
#   1. Database schema: expected tables exist in each database
#   2. Seed data: initial records were loaded (roles, profiles, etc.)
#   3. Authentication: OAuth2 password grant returns a valid token
#   4. API endpoints: each microservice responds to authenticated requests
#   5. Log scan: no ERROR/Exception in docker compose logs
#
# Usage: functional-tests.sh
#
# Requires:
#   - mysql client (or docker exec to the database container)
#   - curl, jq
#   - SHANOIR_TEST_USER / SHANOIR_TEST_PASSWORD (or defaults from create-keycloak-user.sh)

set -euo pipefail

FAILURES=0
TESTS=0

REALM="shanoir-ng"
NGINX_BASE="https://shanoir-ng-nginx"
TEST_USER="${SHANOIR_TEST_USER:-shanoir-admin}"
TEST_PASS="${SHANOIR_TEST_PASSWORD:-Ch4ng3M3!@2025}"

# Token acquisition goes through the docker network so that the JWT issuer
# matches what the microservices expect (SPRING_SECURITY_..._ISSUER_URI).
KC_INTERNAL_BASE="http://keycloak:8080/auth"

docker_curl() {
    docker exec shanoir-ng-nginx curl -s "$@"
}

# Decode a base64url-encoded string (as used in JWT tokens).
# Adds padding and translates the URL-safe alphabet to standard base64.
b64url_decode() {
    local data="$1"
    local pad=$(( (4 - ${#data} % 4) % 4 ))
    data="${data}$(printf '%*s' "$pad" '' | tr ' ' '=')"
    echo "$data" | tr '_-' '/+' | base64 -d
}

pass() { TESTS=$((TESTS + 1)); echo "[PASS] $1"; }
fail() { TESTS=$((TESTS + 1)); FAILURES=$((FAILURES + 1)); echo "[FAIL] $1"; }

# Run a SQL query via docker exec on the database container.
# Usage: db_query <db> <sql>
db_query() {
    docker exec database mysql -uroot -ppassword --no-beep -N -B "$1" -e "$2" 2>/dev/null || true
}

# ─── 1. Database checks ──────────────────────────────────────────────

# Check tables exist in sql db.
# Usage: db_check_tables <db> <expected_tables...>
db_check_tables() {
    local db="$1"
    shift
    local tables=("$@")

    echo ""
    echo "---- Database '$db': table checks ----"
    local actual
    actual=$(db_query "$db" "SHOW TABLES;" | sort)

    for table in "${tables[@]}"; do
        if echo "$actual" | grep -qx "$table"; then
            pass "Table '$db.$table' exists"
        else
            fail "Table '$db.$table' is MISSING"
        fi
    done
}

# Check sql table contains at least n rows.
# Usage: db_check_tables <db> <table> <n>
db_check_row_count() {
    local db="$1"
    local table="$2"
    local min_count="$3"
    local label="${4:-$db.$table has >= $min_count rows}"

    local count
    count=$(db_query "$db" "SELECT COUNT(*) FROM \`$table\`;" | tr -d '[:space:]') || true
    if [ -n "$count" ] && [ "$count" -ge "$min_count" ] 2>/dev/null; then
        pass "$label (count=$count)"
    else
        fail "$label (count=${count:-null}, expected >= $min_count)"
    fi
}

test_database() {
    echo ""
    echo "======== Database Tests ========"

    # Check that all expected databases exist
    echo ""
    echo "---- Database existence ----"
    local dbs
    dbs=$(db_query "information_schema" "SELECT SCHEMA_NAME FROM SCHEMATA;" | sort)
    for db in users studies datasets import preclinical migrations; do
        if echo "$dbs" | grep -qx "$db"; then
            pass "Database '$db' exists"
        else
            fail "Database '$db' is MISSING"
        fi
    done

    # Check key tables per database (not exhaustive — pick representative tables
    # that prove Hibernate schema creation + migrations ran successfully)
    db_check_tables users \
        role users study_user study_user_center access_request

    db_check_tables studies \
        study center subject profile study_center \
        manufacturer manufacturer_model acquisition_equipment coil \
        study_tag tag

    db_check_tables datasets \
        dataset dataset_metadata dataset_acquisition \
        mr_dataset mr_dataset_acquisition study_cards \
        related_datasets dataset_property processing_resource \
        execution_monitoring xa_dataset

    db_check_tables import \
        study_user_center

    # Check seed data was loaded (from import.sql during SHANOIR_MIGRATION=init)
    echo ""
    echo "---- Seed data checks ----"
    db_check_row_count users role 3 "users.role has at least 3 roles"
    db_check_row_count users users 1 "users.users has at least 1 seed user"
    db_check_row_count studies profile 2 "studies.profile has at least 2 profiles"
    db_check_row_count studies study 1 "studies.study has at least 1 seed study"
}

# ─── 2. Authentication test ──────────────────────────────────────────

TOKEN=""

test_auth() {
    echo ""
    echo "======== Authentication Tests ========"

    echo ""
    echo "---- Token acquisition (password grant) ----"
    local body
    body=$(docker_curl -X POST \
        "${KC_INTERNAL_BASE}/realms/${REALM}/protocol/openid-connect/token" \
        --data-urlencode "client_id=shanoir-swagger" \
        --data-urlencode "username=${TEST_USER}" \
        --data-urlencode "password=${TEST_PASS}" \
        --data-urlencode "grant_type=password") || body=""

    TOKEN=$(echo "$body" | jq -r '.access_token // empty')
    if [ -n "$TOKEN" ]; then
        pass "Token acquired for '${TEST_USER}'"
    else
        local err; err=$(echo "$body" | jq -r '.error_description // .error // empty')
        fail "Token acquisition failed${err:+: $err}"
        TOKEN=""
    fi

    if [ -n "$TOKEN" ]; then
        local exp
        exp=$(b64url_decode "$(echo "$TOKEN" | cut -d. -f2)" | jq -r '.exp // empty' 2>/dev/null) || true
        if [ -n "$exp" ]; then
            pass "Token has expiration claim"
        else
            fail "Token missing expiration claim"
        fi
    fi
}

# ─── 3. API endpoint tests ──────────────────────────────────────────

# api_check <label> <url> <allowed_codes> [jq_assertion]
#   jq_assertion: optional jq filter that must return true (e.g. 'length > 0')
api_check() {
    local label="$1"
    local url="$2"
    local allowed="$3"
    local jq_assert="${4:-}"

    local tmp; tmp=$(mktemp)
    local code
    code=$(curl -sk -o "$tmp" -w "%{http_code}" --max-time 15 \
        -H "Authorization: Bearer ${TOKEN}" "$url") || code="000"
    local body; body=$(cat "$tmp"); rm -f "$tmp"

    if echo "$code" | grep -qxE "$allowed"; then
        if [ -n "$jq_assert" ] && [ "$code" = "200" ]; then
            if echo "$body" | jq -e "$jq_assert" > /dev/null 2>&1; then
                pass "$label (HTTP $code, assertion OK)"
            else
                fail "$label (HTTP $code, assertion FAILED: $jq_assert)"
            fi
        else
            pass "$label (HTTP $code)"
        fi
    else
        fail "$label — got HTTP $code, expected $allowed"
    fi
}

test_api() {
    echo ""
    echo "======== API Endpoint Tests ========"

    if [ -z "$TOKEN" ]; then
        echo "SKIPPED: no valid token available"
        fail "API tests skipped (no token)"
        return
    fi

    echo ""
    echo "---- Users microservice ----"
    api_check "GET /users (list)" \
        "${NGINX_BASE}/shanoir-ng/users/users" "200|204"
    api_check "GET /users/count (active count)" \
        "${NGINX_BASE}/shanoir-ng/users/users/count" "200" '. >= 0'

    echo ""
    echo "---- Studies microservice ----"
    api_check "GET /studies/names" \
        "${NGINX_BASE}/shanoir-ng/studies/studies/names" "200|204"
    api_check "GET /centers (list)" \
        "${NGINX_BASE}/shanoir-ng/studies/centers" "200|204"

    echo ""
    echo "---- Datasets microservice ----"
    api_check "GET /datasets" \
        "${NGINX_BASE}/shanoir-ng/datasets/datasets" "200|204"

    echo ""
    echo "---- Import microservice ----"
    api_check "GET /importer (create temp dir)" \
        "${NGINX_BASE}/shanoir-ng/import/importer" "200"

    echo ""
    echo "---- Preclinical microservice ----"
    api_check "GET /pathology (list)" \
        "${NGINX_BASE}/shanoir-ng/preclinical/pathology" "200|204"
}

# ─── 4. Log error scan ───────────────────────────────────────────────

scan_logs() {
    echo ""
    echo "======== Docker Log Error Scan ========"

    # Collect logs and look for ERROR-level entries or Java exceptions.
    # We ignore known benign patterns:
    #   - "ERROR 1007" from MySQL (database already exists)
    #   - "ERROR 1396" from MySQL (user already exists)
    #   - preDestroy / shutdown messages
    #   - Hibernate SQL error logging that is just a constraint check
    local errors
    errors=$(docker compose logs --no-color 2>&1 \
        | grep -iE '\bERROR\b|Exception|FATAL' \
        | grep -viE \
            -e 'ERROR 10(07|62|396)' \
            -e 'preDestroy|ShutdownHook|HikariPool.*shutdown|Closing JPA|Unregistering JMX' \
            -e 'MailConnectException|jakarta\.mail|smtp-sink|mail\.smtp' \
            -e 'Failed to reserve shared memory' \
            -e 'Setting level of logger .* to ERROR' \
            -e 'Propagating ERROR level' \
            -e 'error reading communication packets' \
            -e 'LOG_EXCEPTION_CONVERSION_WORD' \
            -e 'rabbitmq.*(error|disk space monitor)' \
            -e 'dcm4chee-arc.*Name or service not known' \
            -e 'UnknownHostException.*dcm4chee' \
            -e 'WFLYELY00024: Certificate \[rootca\].*CertificateExpiredException' \
            -e 'Unexpected error occurred in scheduled task' \
            -e 'LastLoginDateApiController' \
            -e 'ExceptionTranslationFilter' \
            -e 'EntityNotFoundException' \
        | head -30) || true

    if [ -z "$errors" ]; then
        pass "No unexpected errors in docker compose logs"
    else
        local count
        count=$(echo "$errors" | wc -l)
        fail "Found $count error(s) in docker compose logs:"
        echo "$errors" | sed 's/^/  | /'
    fi
}

# ─── Main ────────────────────────────────────────────────────────────

echo ""
echo "╔══════════════════════════════════════╗"
echo "║     Shanoir NG Functional Tests      ║"
echo "╚══════════════════════════════════════╝"

test_database
test_auth
test_api
scan_logs

echo ""
echo "======== Results: $FAILURES failure(s) / $TESTS test(s) ========"

if [ "$FAILURES" -gt 0 ]; then
    exit 1
fi
exit 0
