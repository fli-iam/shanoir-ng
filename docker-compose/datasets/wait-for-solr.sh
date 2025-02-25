#!/bin/bash

WAIT_TIME=300

echo "Waiting for the solr container to be reachable..."

# Try pinging the solr container
while ! ping -c 1 -W 1 "solr" > /dev/null 2>&1; do
  echo "Container solr is not reachable, retrying..."
  sleep 5

  # Decrement wait time and exit if it reaches zero
  WAIT_TIME=$((WAIT_TIME - 1))
  if [ "$WAIT_TIME" -le 0 ]; then
    echo "Error : Timeout waiting for container solr to become reachable."
    exit 1
  fi
done

echo "Container solr reached, launching container datasets."
