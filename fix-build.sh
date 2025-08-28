#!/bin/bash
# Fix for Firebase build issue

cd /workspaces/adk-java

# Commit and push changes
git add web/next.config.js web/tsconfig.json tsconfig.json package.json web/next.config.ts firebase.json web/public/.nojekyll
git commit -m "fix: Update Next.js configuration to exclude functions during build"
git push origin main