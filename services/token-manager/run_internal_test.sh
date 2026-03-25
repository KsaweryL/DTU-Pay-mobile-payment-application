#!/bin/bash
echo "Running tests within token-manager..."
mvn -Dtest=behaviourtests.TokenRepositoryTest test