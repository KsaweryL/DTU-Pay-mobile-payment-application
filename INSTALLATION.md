# Installation guide (Linux CLI)

This guide explains how to build, deploy, and test the DTU Pay microservices from the command line on any Linux machine (no Jenkins required).

## Table of Contents
- [Repository](#repository)
- [Prerequisites](#prerequisites)
- [Quick start (build, deploy, test)](#quick-start-build-deploy-test)
- [Manual build/deploy/test (if needed)](#manual-builddeploytest-if-needed)
- [Ports](#ports)
- [Test output locations](#test-output-locations)
- [Access](#access)


## Repository

Repository URL:
- https://github.com/KsaweryL/02267-Dev-of-web-services-repo.git

Access requirements:
  - The following account have access:
    - GitHub: `hubertbau`

## Prerequisites

Required tools (tested versions):
- Git: https://git-scm.com/downloads
- Java 21 (OpenJDK/Temurin 21.x): https://adoptium.net/temurin/releases/?version=21
- Maven 3.9.12: https://maven.apache.org/download.cgi
- Docker Engine (with Compose v2 plugin): https://docs.docker.com/engine/install/

Example (Ubuntu/Debian):
```bash
sudo apt-get update
sudo apt-get install -y git
```

Install Java 21 and Maven:
- Recommended: install Temurin 21 and Maven 3.9.12 from the links above and add them to PATH.

Verify versions:
```bash
java -version
mvn -v
docker --version
docker compose version
```

## Quick start (build, deploy, test)

From the repository root:
```bash
./build_and_run.sh
```

What the script does:
- Builds shared libraries and services with Maven
- Builds Docker images and starts all services via Docker Compose
- Runs the system tests (`mvn test` at repo root)
- Shuts down containers at exit and prunes unused Docker images

The script is already in the root, uses Unix (LF) line endings, and is executable.

## Manual build/deploy/test (if needed)

1) Build libraries and services:
```bash
mvn clean install -f libraries -DskipTests
mvn clean package -f services -DskipTests
```

2) Build and run the microservices:
```bash
docker compose build
docker compose up -d
```

3) Run all tests:
```bash
mvn test
```

4) Stop services:
```bash
docker compose down --remove-orphans
```

## Ports

Default ports used:
- Merchant facade: http://localhost:8080
- Customer facade: http://localhost:8081
- Manager facade: http://localhost:8082
- RabbitMQ: 5672 (AMQP), 15672 (management UI)

## Test output locations

- Surefire reports:
  - `end-to-end-test/target/surefire-reports/`

## Access

- Virtual Machine:
  - Username: `not_real_url_username`
  - login via SSH using the private key provided to us the group.
  - ` ssh -i not_real_path`

- Jenkins:
  - URL: not_real_url
  - Username: `not_real_username`
  - Password: `not_real_password` 

