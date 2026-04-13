#!/bin/bash
#
# Helper functions for the deployment smoke test CI workflow.
#
# Usage: deployment-test-helpers.sh <function_name> [args...]

set -euo pipefail

TIMEOUT_EXIT=124

wait_for_healthy() {
	local container="$1"
	local timeout="${2:-120}"
	local elapsed=0

	echo "Waiting for container '$container' to be healthy (timeout: ${timeout}s)..."
	while [ "$elapsed" -lt "$timeout" ]; do
		local status
		status="$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")"
		case "$status" in
			healthy)
				echo "Container '$container' is healthy (after ${elapsed}s)"
				return 0
				;;
			unhealthy)
				echo "Container '$container' is unhealthy"
				docker logs --tail=20 "$container"
				return 1
				;;
		esac
		sleep 3
		elapsed=$((elapsed + 3))
	done

	echo "Timed out waiting for '$container' to become healthy"
	docker logs --tail=30 "$container"
	return $TIMEOUT_EXIT
}

wait_for_log() {
	local container="$1"
	local pattern="$2"
	local timeout="${3:-90}"

	echo "Waiting for pattern '$pattern' in logs of '$container' (timeout: ${timeout}s)..."
	local deadline=$((SECONDS + timeout))
	while [ "$SECONDS" -lt "$deadline" ]; do
		if docker logs "$container" 2>&1 | grep -q "$pattern"; then
			echo "Pattern matched in '$container' logs"
			return 0
		fi
		sleep 3
	done

	echo "Timed out waiting for pattern in '$container' logs"
	docker logs --tail=30 "$container"
	return $TIMEOUT_EXIT
}

wait_for_http() {
	local container="$1"
	local port="$2"
	local path="$3"
	local timeout="${4:-120}"

	echo "Waiting for HTTP on $container:$port$path (timeout: ${timeout}s)..."
	local elapsed=0
	while [ "$elapsed" -lt "$timeout" ]; do
		local code
		code="$(docker exec "$container" sh -c \
			"wget -q -O /dev/null -S http://localhost:${port}${path} 2>&1 | head -1 | grep -oE '[0-9]{3}'" \
			2>/dev/null || echo "000")"
		if [ "$code" -ge 200 ] 2>/dev/null && [ "$code" -lt 500 ] 2>/dev/null; then
			echo "HTTP ready on $container:$port$path (status $code, after ${elapsed}s)"
			return 0
		fi
		sleep 5
		elapsed=$((elapsed + 5))
	done

	echo "Timed out waiting for HTTP on $container:$port$path"
	docker logs --tail=30 "$container"
	return $TIMEOUT_EXIT
}

wait_for_url() {
	local url="$1"
	local timeout="${2:-120}"

	echo "Waiting for URL $url (timeout: ${timeout}s)..."
	local elapsed=0
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

wait_for_port() {
	local container="$1"
	local port="$2"
	local timeout="${3:-120}"

	echo "Waiting for port $port on container '$container' (timeout: ${timeout}s)..."
	local elapsed=0
	while [ "$elapsed" -lt "$timeout" ]; do
		# Use bash /dev/tcp on the host (GitHub runner has bash) to probe
		# the container's published port via localhost.
		if (echo > "/dev/tcp/localhost/$port") 2>/dev/null; then
			echo "Port $port is open on '$container' (after ${elapsed}s)"
			return 0
		fi
		sleep 3
		elapsed=$((elapsed + 3))
	done

	echo "Timed out waiting for port $port on '$container'"
	docker logs --tail=30 "$container"
	return $TIMEOUT_EXIT
}

smoke_tests() {
	local failures=0

	echo ""
	echo "======== Smoke Tests ========"
	echo ""

	check_container_running() {
		local name="$1"
		if docker ps --format '{{.Names}}' | grep -q "^${name}$"; then
			echo "[PASS] Container '$name' is running"
		else
			echo "[FAIL] Container '$name' is NOT running"
			failures=$((failures + 1))
		fi
	}

	check_container_running database
	check_container_running keycloak-database
	check_container_running keycloak
	check_container_running rabbitmq
	check_container_running solr
	check_container_running users
	check_container_running studies
	check_container_running datasets
	check_container_running import
	check_container_running preclinical
	check_container_running nifti-conversion
	check_container_running shanoir-ng-nginx

	echo ""
	echo "---- Keycloak realm check ----"
	local kc_status
	kc_status="$(curl -sk -o /dev/null -w '%{http_code}' \
		http://localhost:8080/auth/realms/shanoir-ng 2>/dev/null || echo "000")"
	if [ "$kc_status" = "200" ]; then
		echo "[PASS] Keycloak shanoir-ng realm is accessible (HTTP $kc_status)"
	else
		echo "[FAIL] Keycloak shanoir-ng realm returned HTTP $kc_status"
		failures=$((failures + 1))
	fi

	echo ""
	echo "---- Nginx TLS endpoint check ----"
	local nginx_status
	nginx_status="$(curl -sk -o /dev/null -w '%{http_code}' \
		https://shanoir-ng-nginx/ 2>/dev/null || echo "000")"
	if [ "$nginx_status" = "200" ] || [ "$nginx_status" = "302" ]; then
		echo "[PASS] Nginx is responding on HTTPS (HTTP $nginx_status)"
	else
		echo "[FAIL] Nginx HTTPS check returned HTTP $nginx_status (expected 200 or 302)"
		failures=$((failures + 1))
	fi

	echo ""
	echo "---- Keycloak via nginx proxy check ----"
	local kc_proxy_status
	kc_proxy_status="$(curl -sk -o /dev/null -w '%{http_code}' \
		https://shanoir-ng-nginx/auth/realms/shanoir-ng/ 2>/dev/null || echo "000")"
	if [ "$kc_proxy_status" = "200" ]; then
		echo "[PASS] Keycloak realm accessible through nginx (HTTP $kc_proxy_status)"
	else
		echo "[FAIL] Keycloak realm via nginx returned HTTP $kc_proxy_status"
		failures=$((failures + 1))
	fi

	echo ""
	echo "---- Microservice proxy check (users) ----"
	local users_proxy_status
	users_proxy_status="$(curl -sk -o /dev/null -w '%{http_code}' \
		https://shanoir-ng-nginx/shanoir-ng/users/swagger-ui/index.html 2>/dev/null || echo "000")"
	if [ "$users_proxy_status" = "200" ]; then
		echo "[PASS] Users microservice accessible through nginx (HTTP $users_proxy_status)"
	else
		echo "[FAIL] Users microservice via nginx returned HTTP $users_proxy_status (expected 200)"
		failures=$((failures + 1))
	fi

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
	echo "Functions: wait_for_healthy, wait_for_log, wait_for_http, wait_for_url, wait_for_port, smoke_tests"
	exit 1
fi

func="$1"
shift
"$func" "$@"
