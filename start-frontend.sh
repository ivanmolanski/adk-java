#!/bin/bash
# Start the Next.js frontend

set -e

echo "🚀 Starting MD Aesthetics Frontend..."

# Check if package.json exists
if [ ! -f "package.json" ]; then
    echo "❌ Error: package.json not found. Please run this script from the project root."
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing Node.js dependencies..."
    npm install
fi

# Start the frontend
echo "🌐 Starting Next.js development server on http://localhost:3000"
echo ""

npm run dev