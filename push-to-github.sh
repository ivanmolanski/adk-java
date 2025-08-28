#!/bin/bash

# Script to push changes to GitHub
echo "Adding changes to Git..."
git add functions/src/index.ts

echo "Committing changes..."
git commit -m "fix: Refactor Firebase Functions to use v2 API correctly"

echo "Pushing changes to GitHub..."
git push origin main

echo "Done! Firebase auto-publish should be triggered."