#!/bin/bash
# Script to push all changes to the repository

cd /workspaces/adk-java
git add web/components/SocialMediaPost.tsx web/components/ui/textarea.tsx
git commit -m "fix: Add missing SocialMediaPost component and textarea UI component"
git push origin main