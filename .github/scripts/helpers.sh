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
		if docker logs "$container" 2>&1 | grep -qE "$pattern"; then
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

	echo "Waiting for URL $url (timeout: ${timeout}s)..."
	while [ "$elapsed" -lt "$timeout" ]; do
		local code
		code="$(curl -sk -o /dev/null -w '%{http_code}' --max-time 5 "$url" 2>/dev/null || echo "000")"
		if [ "$code" -ge 200 ] 2>/dev/null && [ "$code" -lt 400 ] 2>/dev/null; then
			echo "URL $url is ready (status $code, after ${elapsed}s)"
			return 0
		fi
		sleep 5
		elapsed=$((elapsed + 5))
	done

	echo "Timed out waiting for URL $url"
	return $TIMEOUT_EXIT
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
	code="$(curl -sk -o /dev/null -w '%{http_code}' --max-time 10 "$url" 2>/dev/null || echo "000")"
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
	echo "Functions: wait_for_log, wait_for_url, smoke_tests"
	exit 1
fi

func="$1"
shift
"$func" "$@"
