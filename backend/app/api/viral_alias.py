"""
Alias Viral API Router

Provides simplified endpoint paths (/api/viral/*) expected by the current
frontend code, delegating to the primary router logic in `viral.py` while
wrapping responses in the envelope shapes the frontend service & hooks
anticipate (e.g., {"trends": [...]}, {"drafts": [...]}, {"brief": {...}}).

This is an interim compatibility layer. Once the frontend is updated to use
the versioned base path (/viral-service/api/v1/viral/*) directly, this file
can be deprecated and removed.
"""

from fastapi import APIRouter, Depends, HTTPException
from typing import List, Dict, Any, Optional
import logging

from .viral import (
    get_trends as core_get_trends,
    get_drafts as core_get_drafts,
    get_daily_brief as core_get_daily_brief,
    analyze_posts as core_analyze_posts,
    generate_content as core_generate_content,
    AnalyzeRequest, GenerateRequest, TrendAnalysis, ContentDraft
)

logger = logging.getLogger(__name__)

alias_router = APIRouter()

@alias_router.get("/api/viral/health")
async def health_check_alias() -> Dict[str, Any]:
    """Lightweight health alias returning status format expected by frontend."""
    # Frontend healthCheck currently expects { status: 'UP' }
    return {"status": "UP", "service": "viral-api-alias"}


@alias_router.get("/api/viral/trends")
async def alias_get_trends(category: Optional[str] = None, limit: int = 10) -> Dict[str, List[TrendAnalysis]]:
    """Alias endpoint returning trends envelope."""
    trends = await core_get_trends(category=category, limit=limit)
    return {"trends": trends}


@alias_router.get("/api/viral/drafts")
async def alias_get_drafts(category: Optional[str] = None, limit: int = 5, platform: Optional[str] = None) -> Dict[str, List[ContentDraft]]:
    drafts = await core_get_drafts(category=category, limit=limit, platform=platform)
    return {"drafts": drafts}


@alias_router.get("/api/viral/brief")
async def alias_get_brief(date: Optional[str] = None) -> Dict[str, Any]:
    brief = await core_get_daily_brief(date=date)
    return {"brief": brief}


@alias_router.post("/api/viral/analyze")
async def alias_analyze(request: AnalyzeRequest) -> Dict[str, List[TrendAnalysis]]:
    analyses = await core_analyze_posts(request)
    return {"trends": analyses}


@alias_router.post("/api/viral/generate")
async def alias_generate(request: GenerateRequest) -> Dict[str, List[ContentDraft]]:
    drafts = await core_generate_content(request)
    return {"drafts": drafts}
