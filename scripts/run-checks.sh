#!/usr/bin/env bash
set -euo pipefail
mvn clean verify
printf '\nBuild and tests passed. Coverage report: target/site/jacoco/index.html\n'
