"""Ingestion API for scraped viral posts.

Endpoint: POST /viral-service/api/v1/ingest/posts
Payload: { posts: [...], refine: bool }
"""
from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel, Field, field_validator
from typing import List, Any, Dict, Optional
from datetime import datetime
import logging

from sqlalchemy.ext.asyncio import AsyncSession
from app.models.database import get_session
from app.pipeline import process_posts

logger = logging.getLogger(__name__)

ingest_router = APIRouter()


class IngestPost(BaseModel):
    id: Optional[str] = None
    platform: str
    profile: str
    caption: str = ""
    hashtags: List[str] = Field(default_factory=list)
    engagement_rate: float = 0.0
    likes: int = 0
    comments: int = 0
    shares: int = 0
    views: int = 0
    post_url: Optional[str] = None
    scraped_at: Optional[datetime] = None

    @field_validator('platform')
    @classmethod
    def validate_platform(cls, v: str) -> str:
        """Validate platform is supported."""
        if v.lower() not in {"instagram", "tiktok", "web", "unknown"}:
            raise ValueError("Unsupported platform")
        return v.lower()


class IngestRequest(BaseModel):
    posts: List[IngestPost]
    refine: bool = False


class IngestResponse(BaseModel):
    ingested: int
    analyses: int
    drafts: int
    analysis_ids: List[str]
    draft_ids: List[str]


@ingest_router.post('/ingest/posts', response_model=IngestResponse)
async def ingest_posts(request: IngestRequest, session: AsyncSession = Depends(get_session)) -> IngestResponse:
    if not request.posts:
        raise HTTPException(status_code=400, detail="No posts provided")
    raw_posts = [p.model_dump() for p in request.posts]
    logger.info("Ingesting %d posts (refine=%s)", len(raw_posts), request.refine)
    result = await process_posts(session=session, posts=raw_posts, refine=request.refine)
    # metrics increment
    try:
        import main  # type: ignore
        if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
            metrics = main.app.state.metrics
            metrics['ingested_posts'] = metrics.get('ingested_posts', 0) + result['ingested']
            metrics['analyses_created'] = metrics.get('analyses_created', 0) + result['analyses']
            metrics['drafts_created'] = metrics.get('drafts_created', 0) + result['drafts']
    except Exception:  # noqa: BLE001
        pass
    return IngestResponse(**result)
