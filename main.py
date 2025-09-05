from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPBearer
from contextlib import asynccontextmanager
import logging
import os
from dotenv import load_dotenv

from app.core.database import init_db
from app.api.routes import viral_content, agents, health
from app.core.config import get_settings

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    logger.info("Starting MD Aesthetics Viral Forge API")
    await init_db()
    yield
    # Shutdown
    logger.info("Shutting down API")

# Create FastAPI app
app = FastAPI(
    title="MD Aesthetics Viral Forge API",
    description="Pydantic-based agent system for viral content analysis and generation",
    version="2.0.0",
    lifespan=lifespan
)

# Configure CORS
settings = get_settings()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Security
security = HTTPBearer()

# Include routers
app.include_router(health.router, prefix="/api/v1", tags=["health"])
app.include_router(viral_content.router, prefix="/api/v1/viral", tags=["viral-content"])
app.include_router(agents.router, prefix="/api/v1/agents", tags=["agents"])

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "message": "MD Aesthetics Viral Forge API",
        "version": "2.0.0",
        "status": "running"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app", 
        host="0.0.0.0", 
        port=int(os.getenv("SERVER_PORT", 3453)),
        reload=True if os.getenv("ENVIRONMENT", "development") == "development" else False
    )