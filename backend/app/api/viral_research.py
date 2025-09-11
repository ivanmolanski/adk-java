"""Viral Research Endpoint

Provides on-demand combined heuristic + AI-enhanced viral research analysis.
"""
from __future__ import annotations

from fastapi import APIRouter, HTTPException, Depends  # adjust import
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime
import logging

from app.agents.trend_analyzer import TrendAnalyzer, ViralPostData
from app.services.ai_client import AIClient
from app.models.database import get_session
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.viral_repositories import LearningStore, LearningRepository  # updated import

logger = logging.getLogger(__name__)

research_router = APIRouter()

class ViralResearchPost(BaseModel):
  id: str
  platform: str
  profile: str
  caption: str
  hashtags: List[str] = Field(default_factory=list)
  engagement_rate: float
  likes: int
  comments: int
  shares: int = 0
  views: int = 0
  post_url: str
  scraped_at: datetime = Field(default_factory=datetime.utcnow)

class ViralResearchRequest(BaseModel):
  posts: List[ViralResearchPost]
  enrich_with_ai: bool = True
  model: Optional[str] = None

class ViralResearchResult(BaseModel):
  post_id: str
  hook: str
  cta: str
  content_category: str
  relevance_score: float
  virality_score: float
  summary: str
  key_themes: List[str]
  engagement_factors: List[str]
  compliance_notes: Optional[str]
  ai_enrichment: Optional[Dict[str, Any]] = None
  analyzed_at: datetime

@research_router.post('/research', response_model=List[ViralResearchResult])
async def perform_viral_research(request: ViralResearchRequest, session: AsyncSession = Depends(get_session)) -> List[ViralResearchResult]:
  # existing logic preserved, add persistence
  if not request.posts:
    raise HTTPException(status_code=400, detail='No posts provided')
  analyzer = TrendAnalyzer()
  agent_posts: List[ViralPostData] = []
  for p in request.posts:
    agent_posts.append(ViralPostData(
      id=p.id,
      platform=p.platform,
      profile=p.profile,
      caption=p.caption,
      hashtags=p.hashtags,
      engagement_rate=p.engagement_rate,
      likes=p.likes,
      comments=p.comments,
      shares=p.shares,
      views=p.views,
      post_url=p.post_url,
      scraped_at=p.scraped_at
    ))
  heuristics = analyzer.analyze_batch(agent_posts)
  results: List[ViralResearchResult] = []
  ai_client: Optional[AIClient] = None
  if request.enrich_with_ai:
    ai_client = AIClient()
  analyses_payload: list[dict] = []
  posts_payload: list[dict] = [p.model_dump() for p in request.posts]
  for h in heuristics:
    enrichment = None
    if ai_client:
      try:
        prompt = [{"role": "system", "content": "You are an expert medical aesthetics social media strategist. Return STRICT JSON."}, {"role": "user", "content": ('Enhance this heuristic viral post analysis with deeper competitor insight. ' 'Respond JSON with keys: optimized_hook, improved_cta, educational_point, risk_flags, suggested_hashtags.\n' f'Heuristic JSON: {{"hook":"{h.hook}","cta":"{h.cta}","summary":"{h.summary}","category":"{h.content_category.value if hasattr(h.content_category, "value") else h.content_category}"}}')}]
        raw = await ai_client.generate(messages=prompt, model=request.model)
        enrichment = AIClient.extract_json_block(raw) or {"raw": raw}
      except Exception as exc:  # noqa: BLE001
        logger.error('AI enrichment failed for post %s: %s', h.post_id, exc)
        enrichment = {"error": str(exc)}
    analyses_payload.append({
      'post_id': h.post_id,
      'hook': h.hook,
      'cta': h.cta,
      'content_category': h.content_category.value if hasattr(h.content_category, 'value') else str(h.content_category),
      'relevance_score': h.relevance_score,
      'virality_score': h.virality_score,
      'summary': h.summary,
      'key_themes': h.key_themes,
      'engagement_factors': h.engagement_factors,
      'compliance_notes': h.compliance_notes,
      'analyzed_at': h.analyzed_at
    })
    results.append(ViralResearchResult(
      post_id=h.post_id,
      hook=h.hook,
      cta=h.cta,
      content_category=h.content_category.value if hasattr(h.content_category, 'value') else str(h.content_category),
      relevance_score=h.relevance_score,
      virality_score=h.virality_score,
      summary=h.summary,
      key_themes=h.key_themes,
      engagement_factors=h.engagement_factors,
      compliance_notes=h.compliance_notes,
      ai_enrichment=enrichment,
      analyzed_at=h.analyzed_at
    ))
  # Persist
  repo = LearningRepository(session)
  await repo.persist_batch(posts_payload, analyses_payload, drafts=[])
  await session.commit()
  if ai_client:
    await ai_client.close()
  return results

@research_router.get('/learning/recent')
async def recent_learning(limit: int = 25):
  data = await LearningStore.tail(limit)
  return { 'items': data, 'count': len(data) }
