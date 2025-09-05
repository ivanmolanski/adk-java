#!/bin/bash

# MD Aesthetics Viral Forge - Next.js Frontend Startup Script

echo "🌐 Starting MD Aesthetics Viral Forge Frontend..."

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is required but not installed"
    exit 1
fi

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing Node.js dependencies..."
    npm install
fi

# Set environment for development
export NODE_ENV=development

# Start the Next.js development server
echo "🌟 Starting Next.js development server on port 3000..."
echo "🌐 Frontend will be available at: http://localhost:3000"
echo "🔗 Backend API should be running at: http://localhost:3453"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

npm run dev