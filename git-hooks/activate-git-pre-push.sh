#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOOK_SRC="$ROOT_DIR/git-hooks/git-pre-push.sh"
HOOK_DEST="$ROOT_DIR/.git/hooks/pre-push"

if [[ ! -f "$HOOK_SRC" ]]; then
  echo "Hook source not found: $HOOK_SRC"
  exit 1
fi

mkdir -p "$(dirname "$HOOK_DEST")"
cp "$HOOK_SRC" "$HOOK_DEST"
chmod +x "$HOOK_DEST"

echo "Installed pre-push hook to $HOOK_DEST"
