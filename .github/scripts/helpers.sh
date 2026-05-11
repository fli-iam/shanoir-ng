#!/bin/bash
#
# Helper functions for the deployment smoke test CI workflow.
#
# Usage: helpers.sh <function_name> [args...]

set -euo pipefail

TIMEOUT_EXIT=124

wait_for_log() {
	local container="$1"
	local pattern="$2"
	local timeout="${3:-90}"
	local elapsed=0

	echo "Waiting for pattern '$pattern' in logs of '$container' (timeout: ${timeout}s)..."
	while [ "$elapsed" -lt "$timeout" ]; do
		# -a: treat logs as text (docker occasionally emits NUL/control bytes; grep
		# may otherwise skip "binary" stdin and miss matches).
		#
		# IMPORTANT: With `set -o pipefail`, when grep -q matches it exits immediately
		# and docker logs receives SIGPIPE (often exit 141). The pipeline status is
		# then non-zero even though grep succeeded — the loop never sees a match.
		# Run this pipeline without pipefail so the exit status reflects grep only.
		if ( set +o pipefail
		     docker logs "$container" 2>&1 | grep -aqE "$pattern" ); then
			echo "Pattern matched in '$container' logs (after ${elapsed}s)"
			return 0
		fi
		if [ $((elapsed % 30)) -eq 0 ] && [ "$elapsed" -gt 0 ]; then
			local status
			status=$(docker inspect --format='{{.State.Status}}' "$container" 2>/dev/null || echo "unknown")
			echo "  ... still waiting ($container: status=$status, ${elapsed}s elapsed)"
			echo "  ... last log: $(docker logs --tail=1 "$container" 2>&1)"
		fi
		sleep 5
		elapsed=$((elapsed + 5))
	done

	echo "Timed out waiting for pattern in '$container' logs"
	echo "--- last 30 lines of '$container' ---"
	docker logs --tail=30 "$container" 2>&1
	echo "--- container status ---"
	docker inspect --format='{{.State.Status}} (exit={{.State.ExitCode}})' "$container" 2>/dev/null || echo "container not found"
	return $TIMEOUT_EXIT
}

wait_for_url() {
	local url="$1"
	local timeout="${2:-120}"
	local elapsed=0
	local code="000"

	echo "Waiting for URL $url (timeout: ${timeout}s)..."
	while [ "$elapsed" -lt "$timeout" ]; do
		# Prefer IPv4: on some hosts curl tries IPv6 first and gets connection refused
		# while Docker publishes 443 on IPv4 only.
		code="$(curl -4sk -o /dev/null -w '%{http_code}' --max-time 5 "$url" 2>/dev/null || echo "000")"
		code="$(echo "$code" | tr -d '[:space:]')"
		if [ "$code" -ge 200 ] 2>/dev/null && [ "$code" -lt 400 ] 2>/dev/null; then
			echo "URL $url is ready (status $code, after ${elapsed}s)"
			return 0
		fi
		if [ $((elapsed % 30)) -eq 0 ] && [ "$elapsed" -gt 0 ]; then
			local nx_status="n/a"
			case "$url" in *shanoir-ng-nginx*)
				nx_status="$(docker inspect --format '{{.State.Status}} exit={{.State.ExitCode}}' shanoir-ng-nginx 2>/dev/null || echo 'unknown')"
				;;
			esac
			echo "  ... still waiting for $url (HTTP ${code}, ${elapsed}s elapsed; nginx: ${nx_status})"
		fi
		sleep 5
		elapsed=$((elapsed + 5))
	done

	echo "Timed out waiting for URL $url (last HTTP code: ${code})"
	return $TIMEOUT_EXIT
}

# Poll Keycloak until the shanoir-admin password grant returns an access token.
# After `docker compose restart`, `wait_for_log` often matches *old* log lines and
# returns immediately while Keycloak is still starting — use this instead for readiness.
# Usage: wait_for_shanoir_password_token [timeout_seconds]
wait_for_shanoir_password_token() {
	local timeout="${1:-180}"
	local elapsed=0
	local REALM="${REALM:-shanoir-ng}"
	local KC_INTERNAL_BASE="${KC_INTERNAL_BASE:-http://keycloak:8080/auth}"
	local U="${SHANOIR_TEST_USER:-shanoir-admin}"
	local P="${SHANOIR_TEST_PASSWORD:-Ch4ng3M3!@2025}"

	if [ "$(docker inspect shanoir-ng-nginx --format '{{.State.Running}}' 2>/dev/null)" != "true" ]; then
		echo "ERROR: shanoir-ng-nginx is not running; cannot reach Keycloak from the stack." >&2
		return 1
	fi

	echo "Waiting for Shanoir admin password grant (Keycloak realm ${REALM}, timeout: ${timeout}s)..."
	while [ "$elapsed" -lt "$timeout" ]; do
		local body token
		body=$(
			docker exec shanoir-ng-nginx curl -sS \
				-X POST "${KC_INTERNAL_BASE}/realms/${REALM}/protocol/openid-connect/token" \
				--data-urlencode "client_id=shanoir-swagger" \
				--data-urlencode "username=${U}" \
				--data-urlencode "password=${P}" \
				--data-urlencode "grant_type=password" 2>/dev/null || true
		)
		token=$(echo "$body" | jq -r '.access_token // empty' 2>/dev/null || true)
		if [ -n "$token" ]; then
			echo "Password grant succeeded (after ${elapsed}s)"
			return 0
		fi
		if [ $((elapsed % 20)) -eq 0 ] && [ "$elapsed" -gt 0 ]; then
			echo "  ... still waiting (${elapsed}s; Keycloak may still be starting after restart)"
		fi
		sleep 5
		elapsed=$((elapsed + 5))
	done

	echo "Timed out waiting for password grant from Keycloak"
	return 124
}

# Check an HTTP endpoint and record pass/fail.
# Usage: smoke_check_http <label> <url> <allowed_codes>
#   allowed_codes: pipe-separated HTTP codes, e.g. "200" or "200|301|401"
# Sets $failures (must be declared in caller's scope).
smoke_check_http() {
	local label="$1"
	local url="$2"
	local allowed="$3"
	local code
	code="$(curl -4sk -o /dev/null -w '%{http_code}' --max-time 10 "$url" 2>/dev/null || echo "000")"
	if echo "$code" | grep -qxE "$allowed"; then
		echo "[PASS] $label (HTTP $code)"
	else
		echo "[FAIL] $label — got HTTP $code, expected $allowed"
		failures=$((failures + 1))
	fi
}

smoke_tests() {
	local failures=0

	echo ""
	echo "======== Smoke Tests ========"
	echo ""

	for name in database keycloak-database keycloak rabbitmq solr \
	            users studies datasets import preclinical \
	            nifti-conversion shanoir-ng-nginx; do
		if docker ps --format '{{.Names}}' | grep -q "^${name}$"; then
			echo "[PASS] Container '$name' is running"
		else
			echo "[FAIL] Container '$name' is NOT running"
			failures=$((failures + 1))
		fi
	done

	echo ""
	echo "---- Keycloak realm check ----"
	smoke_check_http "Keycloak shanoir-ng realm is accessible" \
		"http://localhost:8080/auth/realms/shanoir-ng" "200"

	echo ""
	echo "---- Nginx TLS endpoint check ----"
	smoke_check_http "Nginx is responding on HTTPS" \
		"https://shanoir-ng-nginx/" "200|301|302"

	echo ""
	echo "---- Keycloak via nginx proxy check ----"
	smoke_check_http "Keycloak realm accessible through nginx" \
		"https://shanoir-ng-nginx/auth/realms/shanoir-ng/" "200"

	echo ""
	echo "---- Microservice proxy check (users) ----"
	smoke_check_http "Users microservice reachable through nginx" \
		"https://shanoir-ng-nginx/shanoir-ng/users/swagger-ui/index.html" "200|301|302|401|403"

	echo ""
	echo "======== Results: $failures failure(s) ========"

	if [ "$failures" -gt 0 ]; then
		echo ""
		echo "---- Container statuses ----"
		docker compose ps --all
		return 1
	fi
	return 0
}

if [ $# -eq 0 ]; then
	echo "Usage: $0 <function> [args...]"
	echo "Functions: wait_for_log, wait_for_url, wait_for_shanoir_password_token, smoke_tests"
	exit 1
fi

func="$1"
shift
"$func" "$@"
