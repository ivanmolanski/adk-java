#!/usr/bin/env bash
# Helper script: pull latest from origin/main, commit local changes if any, and push (force-with-lease)
# Usage:
#   ./scripts/pull_push_publish.sh        # runs in dry-run first (shows actions)
#   ./scripts/pull_push_publish.sh --run  # actually performs pull/commit/push

set -euo pipefail

DRY_RUN=true
if [[ "${1:-}" == "--run" ]]; then
  DRY_RUN=false
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Working directory: $ROOT_DIR"

echo "Checking git status..."
git fetch origin main

if $DRY_RUN; then
  echo "DRY RUN: The script will perform these actions if you re-run with --run:" 
fi

echo "1) Pull latest from origin/main"
if $DRY_RUN; then
  echo "   git pull origin main --ff-only"
else
  git pull origin main --ff-only
fi

echo
echo "2) Show local changes (if any)"
git status --porcelain

if [[ -n "$(git status --porcelain)" ]]; then
  echo
  echo "Local changes detected." 
  echo "3) Stage all changes and create a commit"
  if $DRY_RUN; then
    echo "   git add . && git commit -m \"chore: apply fixes and trigger publish\""
  else
    git add .
    # Only commit if there's something staged
    if ! git diff --cached --quiet; then
      git commit -m "chore: apply fixes and trigger publish"
    else
      echo "Nothing to commit after staging."
    fi
  fi
else
  echo "No local changes to commit."
fi

echo
echo "4) Push to origin main (force-with-lease)"
if $DRY_RUN; then
  echo "   git push --force-with-lease origin main"
else
  git push --force-with-lease origin main
fi

echo
echo "DONE. If you ran in dry-run mode, re-run with: ./scripts/pull_push_publish.sh --run"
