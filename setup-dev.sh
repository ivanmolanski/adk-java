#!/bin/bash
# Setup script for MD Aesthetics Viral Content System
# This script sets up the Python backend environment

set -e

echo "🚀 Setting up MD Aesthetics Viral Content System..."

# Check if we're in the right directory
if [ ! -f "requirements.txt" ]; then
    echo "❌ Error: requirements.txt not found. Please run this script from the project root."
    exit 1
fi

# Install Python dependencies
echo "📦 Installing Python dependencies..."
pip3 install --user -r requirements.txt

# Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    echo "📝 Creating .env file from template..."
    cp .env.example .env
    echo "⚠️  Please edit .env file with your actual API keys and database credentials"
fi

# Create database directory for local development
mkdir -p data

echo "✅ Setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Edit .env file with your API keys and database credentials"
echo "2. Run './start-backend.sh' to start the Python backend"
echo "3. Run 'npm run dev' to start the Next.js frontend"
echo ""
echo "🌐 API will be available at: http://localhost:3453"
echo "🖥️  Frontend will be available at: http://localhost:3000"