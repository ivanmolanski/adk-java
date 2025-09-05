#!/bin/bash

# MD Aesthetics Viral Forge - Development Setup Script

echo "🔧 Setting up MD Aesthetics Viral Forge Development Environment..."

# Install Python dependencies
echo "📦 Installing Python backend dependencies..."
if [ ! -d "venv" ]; then
    python3 -m venv venv
fi

source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

# Install Node.js dependencies for frontend
echo "📦 Installing Node.js frontend dependencies..."
npm install

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p logs
mkdir -p temp
mkdir -p uploads

# Copy environment file if it doesn't exist
if [ ! -f ".env.local" ]; then
    echo "📄 Creating local environment file..."
    cp .env .env.local
    echo "⚠️ Please update .env.local with your specific configuration"
fi

# Set up pre-commit hooks (optional)
echo "🔗 Setting up development tools..."

# Make scripts executable
chmod +x start-backend.sh
chmod +x start-frontend.sh

echo ""
echo "✅ Development environment setup complete!"
echo ""
echo "🚀 To start the services:"
echo "   Backend (Python):  ./start-backend.sh"
echo "   Frontend (Next.js): ./start-frontend.sh"
echo ""
echo "📖 API Documentation: http://localhost:3453/docs"
echo "🌐 Frontend Dashboard: http://localhost:3000"
echo ""