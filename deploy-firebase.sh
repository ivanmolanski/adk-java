#!/bin/bash
set -e

# Add changes
git add functions/src/index.ts

# Commit changes
git commit -m "Fix Firebase Functions v2 imports and function definitions"

# Force push to trigger auto-deployment
git push -f origin main

# Deploy functions directly if needed
cd functions
npm run build
npm run deploy