#!/bin/bash
set -e

cd /workspaces/adk-java

echo "Adding changes to git..."
git add web/components/ui/input.tsx

echo "Committing changes..."
git commit -m "fix: Add missing input component in web/components/ui directory"

echo "Pushing changes to GitHub..."
git push

echo "Done!"