#!/bin/bash
set -e

cleanup() {
	docker compose down --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

cleanup

mvn clean install -f libraries -DskipTests

mvn clean package -f services -DskipTests

docker compose build

docker compose up -d

# Run tests while services are up
mvn -f . test

docker image prune -f 