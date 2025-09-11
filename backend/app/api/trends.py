"""Endpoints for retrieving recent trend analyses and generated content drafts."""
from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException
from typing import List, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from datetime import datetime, timedelta

from app.models.database import get_session
from app.repositories.viral_repositories import LearningStore

trends_router = APIRouter()

@trends_router.get("/trends", summary="Recent trend analyses")
async def get_trends(limit: int = 20) -> Dict[str, Any]:
  items = await LearningStore.tail(limit * 3)  # oversample then filter
  analyses: List[Dict[str, Any]] = []
  for item in reversed(items):  # newest last in file; reverse to get newest first
    if item.get('type') == 'analysis':
      analyses.append(item)
    if len(analyses) >= limit:
      break
  return {"count": len(analyses), "analyses": analyses}


@trends_router.get("/content-drafts", summary="Recent generated content drafts")
async def get_content_drafts(limit: int = 20) -> Dict[str, Any]:
  items = await LearningStore.tail(limit * 4)
  drafts: List[Dict[str, Any]] = []
  for item in reversed(items):
    if item.get('type') == 'draft':
      drafts.append(item)
    if len(drafts) >= limit:
      break
  return {"count": len(drafts), "drafts": drafts}
