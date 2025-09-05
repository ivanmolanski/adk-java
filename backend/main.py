#!/usr/bin/env python3
"""
FastAPI Backend for MD Aesthetics Viral Content System

This is the main application entry point for the Python/FastAPI backend
that replaces the Java/Spring Boot viral-service.
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
import os
from pathlib import Path
import logging
from typing import Dict, Any

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Import route modules
from app.api.viral import router as viral_router
from app.api.agents import router as agents_router

def create_app() -> FastAPI:
    """Create and configure the FastAPI application."""
    
    app = FastAPI(
        title="MD Aesthetics Viral Content API",
        description="Python/FastAPI backend for competitive intelligence and content generation",
        version="2.0.0",
        docs_url="/docs",
        redoc_url="/redoc"
    )

    # Configure CORS
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],  # Configure appropriately for production
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Health check endpoint
    @app.get("/api/v1/health")
    async def health_check() -> Dict[str, Any]:
        """Health check endpoint."""
        return {
            "status": "healthy",
            "service": "md-aesthetics-viral-api",
            "version": "2.0.0",
            "backend": "FastAPI/Python",
            "database": "PostgreSQL/Supabase"
        }

    # Include API routers
    app.include_router(viral_router, prefix="/api/v1/viral", tags=["viral"])
    app.include_router(agents_router, prefix="/api/v1/agents", tags=["agents"])

    return app

# Create the app instance
app = create_app()

if __name__ == "__main__":
    # Run the server
    port = int(os.getenv("PORT", "3453"))
    host = os.getenv("HOST", "0.0.0.0")
    
    logger.info(f"Starting MD Aesthetics Viral Content API on {host}:{port}")
    uvicorn.run(
        "main:app",
        host=host,
        port=port,
        reload=True,
        log_level="info"
    )