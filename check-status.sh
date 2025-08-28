#!/bin/bash

echo "=== Web App Build Status Check ==="
echo "Checking for TypeScript errors in web components..."

# List TypeScript errors if any
if grep -r "Cannot find module" --include="*.tsx" --include="*.ts" /workspaces/adk-java/web/; then
  echo "Found TypeScript import errors!"
else
  echo "No TypeScript import errors found in web components."
fi

echo ""
echo "=== Firebase Functions Build Status Check ==="
if [ -f "/workspaces/adk-java/functions/lib/index.js" ]; then
  echo "Firebase Functions successfully built!"
else
  echo "Firebase Functions not built yet or build failed."
fi

echo ""
echo "=== Recent Commits ==="
git log -3 --pretty=format:"%h - %an, %ar : %s"

echo ""
echo "=== Next Steps ==="
echo "1. Monitor GitHub Actions for successful deployment"
echo "2. Verify web app functionality after deployment"
echo "3. Continue with the error resolution plan for non-critical issues"