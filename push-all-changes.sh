#!/bin/bash
set -e

cd /workspaces/adk-java

echo "Adding all changes to git..."
git add .

echo "Committing changes..."
git commit -m "fix: Comprehensive update for all components and configurations"

echo "Pushing changes to GitHub..."
git push

echo "Done! All changes pushed successfully."