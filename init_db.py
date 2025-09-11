#!/usr/bin/env python3
"""Initialize the database schema for MD Aesthetics viral intelligence system."""

import asyncio
import sys
import os

# Add the backend path to sys.path to import modules
sys.path.insert(0, '/workspaces/adk-java/backend')

from app.models.database import init_db

async def main():
    """Initialize database tables."""
    print("🔧 Initializing MD Aesthetics database schema...")
    try:
        await init_db()
        print("✅ Database schema initialized successfully!")
        print("   Tables: viral_posts, trend_analysis, content_drafts")
    except Exception as e:
        print(f"❌ Database initialization failed: {e}")
        return 1
    return 0

if __name__ == "__main__":
    exit_code = asyncio.run(main())
    sys.exit(exit_code)