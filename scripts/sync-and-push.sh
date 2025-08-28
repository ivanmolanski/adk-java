#!/usr/bin/env bash
# Helper script: fetch, pull (rebase) and push current branch to origin
# Usage: ./scripts/sync-and-push.sh [--force]

set -euo pipefail

FORCE=false
if [ "${1-}" = "--force" ]; then
  FORCE=true
fi

echo "Remote origin:"
git remote -v

echo "Fetching all remotes and pruning deleted refs..."
git fetch --all --prune

BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: $BRANCH"

echo "Status (short):"
git status --porcelain=2 --branch || true

# If there are uncommitted changes prompt user to stash or commit
if [ -n "$(git status --porcelain)" ]; then
  echo "You have uncommitted changes. Please commit or stash them before running this script."
  git status --short
  exit 1
fi

echo "Pulling latest from origin/$BRANCH (rebase)..."
git pull --rebase origin "$BRANCH"

if [ "$FORCE" = true ]; then
  echo "Pushing with --force-with-lease to origin/$BRANCH"
  git push --force-with-lease origin "$BRANCH"
else
  echo "Pushing to origin/$BRANCH"
  git push origin "$BRANCH"
fi

echo "Done. Run 'git branch -vv' and 'git log --oneline origin/$BRANCH..$BRANCH' to verify."
