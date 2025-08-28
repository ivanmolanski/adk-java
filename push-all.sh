#!/bin/bash
# Script to push all changes to the repository

cd /workspaces/adk-java
git add .
git commit -m "fix: Update all files to fix build issues"
git push origin main