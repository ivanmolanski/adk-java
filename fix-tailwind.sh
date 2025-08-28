#!/bin/bash
cd /workspaces/adk-java/web
# Update package.json to add @tailwindcss/postcss
if ! grep -q "@tailwindcss/postcss" package.json; then
  echo "Adding @tailwindcss/postcss to dependencies"
  # Use temporary file for substitution
  sed 's/"tailwindcss": "^3.4.16"/"tailwindcss": "^3.4.16",\n    "@tailwindcss\/postcss": "^1.0.0"/' package.json > package.json.tmp
  mv package.json.tmp package.json
fi

# Commit and push changes
cd /workspaces/adk-java
git add web/postcss.config.mjs web/package.json
git commit -m "fix: Update PostCSS config to use @tailwindcss/postcss for Next.js 15.5.2 compatibility"
git push origin main