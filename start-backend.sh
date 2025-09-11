#!/bin/bash
# Start the Python FastAPI backend

set -e

echo "🚀 Starting MD Aesthetics Python Backend..."

# Check if we're in the right directory
if [ ! -f "backend/main.py" ]; then
    echo "❌ Error: backend/main.py not found. Please run this script from the project root."
    exit 1
fi

# Check if .env exists
if [ ! -f ".env" ]; then
    echo "⚠️  Warning: .env file not found. Using default configuration."
    echo "   Run './setup-dev.sh' to create .env from template."
fi

# Activate virtual environment if present
if [ -d ".venv" ]; then
    echo "🔧 Activating virtual environment (.venv)"
    # shellcheck disable=SC1091
    source .venv/bin/activate || echo "⚠️  Failed to activate .venv; continuing with system python"
fi

# Set environment variables
export PYTHONPATH="${PYTHONPATH}:$(pwd)"

# Determine python executable (prefer venv)
PY_EXEC="python3"
if command -v python >/dev/null 2>&1; then
    PY_EXEC="python"
fi

# Start the backend
echo "🌐 Starting FastAPI server on http://localhost:3453"
echo "📝 API documentation will be available at http://localhost:3453/docs"
echo ""

cd backend
"${PY_EXEC}" main.py