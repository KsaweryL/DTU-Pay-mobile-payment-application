#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -n "${SKIP_PRE_PUSH:-}" || -n "${SKIP_PRE_PUSH_HOOK:-}" ]]; then
  echo "pre-push hook skipped (SKIP_PRE_PUSH[_HOOK] set)."
  exit 0
fi

BUILD_SCRIPT="$ROOT_DIR/build_and_run.sh"
if [[ -x "$BUILD_SCRIPT" ]]; then
  echo "Running pre-push checks via build_and_run.sh..."
  "$BUILD_SCRIPT"
  exit 0
fi

if [[ -f "$BUILD_SCRIPT" ]]; then
  echo "build_and_run.sh exists but is not executable. Run: chmod +x build_and_run.sh"
  exit 1
fi

echo "No build_and_run.sh found. Skipping pre-push checks."
exit 0
