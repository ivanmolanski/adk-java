#!/bin/bash
set -e  # Exit immediately if a command exits with a non-zero status

echo "=== Starting comprehensive push process ==="
echo "1. Checking git status..."
git status

echo "2. Adding all changes..."
git add .

echo "3. Checking for changes to commit..."
if git diff --cached --quiet; then
  echo "No changes to commit. Everything is up to date."
else
  echo "4. Committing changes..."
  git commit -m "fix: Full comprehensive update for build and deployment"
  
  echo "5. Pushing to GitHub..."
  git push origin main
fi

echo "=== Push process completed ==="
echo "Latest commit:"
git log -1 --pretty=format:"%h - %an, %ar : %s"