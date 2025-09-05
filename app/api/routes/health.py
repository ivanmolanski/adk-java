from fastapi import APIRouter, HTTPException
from typing import Dict, Any

router = APIRouter()

@router.get("/health")
async def health_check() -> Dict[str, Any]:
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "MD Aesthetics Viral Forge API",
        "version": "2.0.0"
    }

@router.get("/status")
async def system_status() -> Dict[str, Any]:
    """Detailed system status"""
    return {
        "api": "operational",
        "database": "connected",
        "agents": "ready",
        "services": {
            "trend_analyzer": "active",
            "content_creator": "active", 
            "compliance_agent": "active",
            "email_dispatcher": "active"
        }
    }