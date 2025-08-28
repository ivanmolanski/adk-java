#!/bin/bash
# Script to push all changes to the repository with full force

cd /workspaces/adk-java
git add --all
git commit -m "fix: Push all changes to fix build issues"
git push origin main