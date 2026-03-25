#!/bin/bash
set -e

docker compose build

docker compose up -d

mvn clean test -f end-to-end-test