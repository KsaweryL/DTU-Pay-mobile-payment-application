#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOOK_DEST="$ROOT_DIR/.git/hooks/pre-push"

if [[ -f "$HOOK_DEST" ]]; then
  rm -f "$HOOK_DEST"
  echo "Removed pre-push hook from $HOOK_DEST"
else
  echo "No pre-push hook installed at $HOOK_DEST"
fi
