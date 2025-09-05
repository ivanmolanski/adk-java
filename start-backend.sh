#!/bin/bash

# MD Aesthetics Viral Forge - Python Backend Startup Script

echo "🚀 Starting MD Aesthetics Viral Forge Python Backend..."

# Check if Python 3.8+ is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed"
    exit 1
fi

# Create virtual environment if it doesn't exist
if [ ! -d "venv" ]; then
    echo "📦 Creating Python virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "🔄 Activating virtual environment..."
source venv/bin/activate

# Install/upgrade dependencies
echo "📥 Installing dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

# Set environment variables
export PYTHONPATH="${PYTHONPATH}:$(pwd)"

# Check database connectivity (optional)
echo "🔗 Checking database connection..."
python3 -c "
import asyncio
from app.core.database import init_db
try:
    asyncio.run(init_db())
    print('✅ Database connection successful')
except Exception as e:
    print(f'⚠️ Database connection warning: {e}')
"

# Start the FastAPI server
echo "🌟 Starting FastAPI server on port 3453..."
echo "📊 Dashboard will be available at: http://localhost:3453"
echo "📖 API docs will be available at: http://localhost:3453/docs"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

python3 main.py