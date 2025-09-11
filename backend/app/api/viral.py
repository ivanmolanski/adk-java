"""
Viral Content API Endpoints

This module handles viral content analysis, trend identification,
and content generation for MD Aesthetics.
"""

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
import logging
from datetime import datetime
import uuid

# Import agents
from ..agents.trend_analyzer import TrendAnalyzer, ViralPostData
from ..agents.content_creator import ContentCreator, TrendInput, Platform
from ..agents.compliance_agent import ComplianceAgent
from ..agents.email_dispatcher import EmailDispatcher
from ..agents.scraping_orchestrator import ScrapingOrchestrator
from app.models.database import (
  get_session, ViralPost as ViralPostRecord,
  TrendAnalysisRecord, ContentDraftRecord
)
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.repositories.viral_repositories import (
  ViralPostRepository, TrendAnalysisRepository, ContentDraftRepository
)

logger = logging.getLogger(__name__)

# Legacy scraping module removal note:
# Original implementation attempted to import a local selenium/bs4 based
# ViralContentScraper. We have replaced scraping with the Apify-powered
# ScrapingOrchestrator for reliability and reduced maintenance. The legacy
# import is intentionally removed to avoid dual pathways drifting. The
# /scrape endpoint now delegates to the same orchestrator used by
# /scrape/apify for backward compatibility.

router = APIRouter()

# Pydantic Models
class ViralPost(BaseModel):
  """Model for viral social media posts."""
  id: str
  platform: str = Field(..., description="Platform (instagram, tiktok)")
  profile: str = Field(..., description="Profile username")
  caption: str = Field(..., description="Post caption")
  hashtags: List[str] = Field(default_factory=list, description="Hashtags used")
  engagement_rate: float = Field(..., description="Engagement rate percentage")
  likes: int = Field(default=0, description="Number of likes")
  comments: int = Field(default=0, description="Number of comments")
  shares: int = Field(default=0, description="Number of shares")
  views: int = Field(default=0, description="Number of views")
  post_url: str = Field(..., description="URL to the original post")
  scraped_at: datetime = Field(default_factory=datetime.utcnow)

class TrendAnalysis(BaseModel):
  """Model for trend analysis results."""
  post_id: str
  hook: str = Field(..., description="The 3-second hook")
  cta: str = Field(..., description="Call-to-action identified")
  content_category: str = Field(..., description="Content category")
  relevance_score: float = Field(..., ge=0.0, le=1.0,
                                 description="Relevance to MD Aesthetics (0-1)")
  virality_score: float = Field(..., ge=0.0, le=1.0,
                                description="Viral potential score (0-1)")
  summary: str = Field(..., description="Brief analysis summary")
  key_themes: List[str] = Field(default_factory=list,
                                description="Key themes identified")
  engagement_factors: List[str] = Field(default_factory=list,
                                        description="Engagement factors")
  compliance_notes: Optional[str] = Field(None,
                                          description="Compliance concerns")
  analyzed_at: datetime = Field(default_factory=datetime.utcnow)

class ContentDraft(BaseModel):
  """Model for generated content drafts."""
  id: str = Field(..., description="Unique identifier for the content draft")
  platform: str
  caption: str
  hashtags: List[str]
  suggested_media_type: str
  compliance_checked: bool = True
  created_at: datetime = Field(default_factory=datetime.utcnow)

class AnalyzeRequest(BaseModel):
  """Request model for content analysis."""
  posts: List[ViralPost]

class GenerateRequest(BaseModel):
  """Request model for content generation."""
  trend_analysis: List[TrendAnalysis]
  brand_guidelines: Optional[Dict[str, Any]] = None

# API Endpoints
@router.get("/posts", response_model=List[ViralPost])
async def get_viral_posts(
  platform: Optional[str] = None,
  limit: int = 50,
  session: AsyncSession = Depends(get_session)
) -> List[ViralPost]:
  """
  Get viral posts from competitor analysis.

  Args:
  platform: Filter by platform (instagram, tiktok)
  limit: Maximum number of posts to return
  """
  logger.info("Fetching viral posts - platform: %s, limit: %s", platform, limit)
  repo = ViralPostRepository(session)
  rows = await repo.list_posts(platform=platform, limit=limit)
  return [
    ViralPost(
      id=str(getattr(r, 'id')),
      platform=str(getattr(r, 'platform')),
      profile=str(getattr(r, 'profile')),
      caption=str(getattr(r, 'caption')),
      hashtags=list(getattr(r, 'hashtags') or []),
      engagement_rate=float(getattr(r, 'engagement_rate') or 0.0),
      likes=int(getattr(r, 'likes') or 0),
      comments=int(getattr(r, 'comments') or 0),
      shares=int(getattr(r, 'shares') or 0),
      views=int(getattr(r, 'views') or 0),
      post_url=str(getattr(r, 'post_url')),
      scraped_at=getattr(r, 'scraped_at')
    ) for r in rows
  ]

@router.post("/analyze", response_model=List[TrendAnalysis])
async def analyze_posts(request: AnalyzeRequest, session: AsyncSession = Depends(get_session)) -> List[TrendAnalysis]:
  """
  Analyze viral posts for trends, hooks, and CTAs.

  This endpoint uses the TrendAnalyzer agent to process posts
  and extract key insights for content creation.
  """
  logger.info("Analyzing %d posts", len(request.posts))

  # Initialize TrendAnalyzer agent
  analyzer = TrendAnalyzer()

  # Convert API models to agent models
  agent_posts = []
  for post in request.posts:
    agent_post = ViralPostData(
      id=post.id,
      platform=post.platform,
      profile=post.profile,
      caption=post.caption,
      hashtags=post.hashtags,
      engagement_rate=post.engagement_rate,
      likes=post.likes,
      comments=post.comments,
      shares=post.shares,
      views=post.views,
      post_url=post.post_url,
      scraped_at=post.scraped_at
    )
    agent_posts.append(agent_post)

  # Analyze posts using the agent
  agent_results = analyzer.analyze_batch(agent_posts)

  post_repo = ViralPostRepository(session)
  await post_repo.upsert_many([p.dict() for p in request.posts])

  analysis_repo = TrendAnalysisRepository(session)
  records_data = []
  for result in agent_results:
    records_data.append({
      "post_id": result.post_id,
      "hook": result.hook,
      "cta": result.cta,
      "content_category": result.content_category.value if hasattr(result.content_category, 'value') else str(result.content_category),
      "relevance_score": result.relevance_score,
      "virality_score": result.virality_score,
      "summary": result.summary,
      "key_themes": result.key_themes,
      "engagement_factors": result.engagement_factors,
      "compliance_notes": result.compliance_notes,
      "analyzed_at": result.analyzed_at
    })
  await analysis_repo.create_many(records_data)
  await session.commit()

  # Convert agent results back to API models
  api_results = []
  for result in agent_results:
    api_result = TrendAnalysis(
      post_id=result.post_id,
      hook=result.hook,
      cta=result.cta,
      content_category=result.content_category,
      relevance_score=result.relevance_score,
      virality_score=result.virality_score,
      summary=result.summary,
      key_themes=result.key_themes,
      engagement_factors=result.engagement_factors,
      compliance_notes=result.compliance_notes,
      analyzed_at=result.analyzed_at
    )
    api_results.append(api_result)

  logger.info("Completed analysis of %d posts", len(api_results))
  return api_results

@router.post("/generate", response_model=List[ContentDraft])
async def generate_content(request: GenerateRequest, session: AsyncSession = Depends(get_session)) -> List[ContentDraft]:
  """
  Generate MD Aesthetics-branded content based on trend analysis.

  This endpoint uses the ContentCreator agent to create compliant,
  on-brand content drafts.
  """
  logger.info("Generating content from %d analyses",
              len(request.trend_analysis))

  # Initialize ContentCreator agent
  creator = ContentCreator()
  compliance_agent = ComplianceAgent()

  # Convert API models to agent models
  agent_inputs = []
  for analysis in request.trend_analysis:
    agent_input = TrendInput(
      hook=analysis.hook,
      cta=analysis.cta,
      content_category=analysis.content_category,
      virality_score=analysis.virality_score,
      key_themes=analysis.key_themes,
      engagement_factors=analysis.engagement_factors
    )
    agent_inputs.append(agent_input)

  # Generate content using the agent
  agent_drafts = creator.generate_batch(agent_inputs, Platform.INSTAGRAM)

  # Map analysis hook captured in this transaction
  analysis_repo = TrendAnalysisRepository(session)
  hook_to_id = await analysis_repo.map_hook_to_id()

  # Convert agent results back to API models
  api_drafts = []
  draft_records_data = []
  for draft in agent_drafts:
    # Persist draft
    first_line = draft.caption.split('\n')[0]
    related_analysis_id = hook_to_id.get(first_line)
    # Run compliance check (even though ContentCreator does basic compliance)
    compliance_result = compliance_agent.check_compliance(draft.caption, content_id=draft.id)
    # Append compliance notes to caption if auto-correction applied
    if compliance_result.approved_content:
      draft.caption = compliance_result.approved_content
    if not compliance_result.is_compliant:
      draft.caption += "\n\n[Compliance Notes: " + "; ".join(
        f"{iss.issue_type}:{iss.severity}" for iss in compliance_result.issues) + "]"
    # Prepare draft record data
    draft_records_data.append({
      "id": draft.id or str(uuid.uuid4()),
      "analysis_id": related_analysis_id,
      "platform": draft.platform.value,
      "caption": draft.caption,
      "hashtags": draft.hashtags,
      "suggested_media_type": draft.suggested_media_type.value,
      "target_service": draft.target_service.value if hasattr(draft.target_service, 'value') else str(draft.target_service),
      "compliance_checked": compliance_result.is_compliant,
      "brand_alignment_score": draft.brand_alignment_score,
      "estimated_engagement": draft.estimated_engagement,
      "suggested_visuals": draft.suggested_visuals,
      "posting_tips": draft.posting_tips,
      "created_at": draft.created_at
    })
    api_drafts.append(ContentDraft(
      id=draft.id,
      platform=draft.platform.value,
      caption=draft.caption,
      hashtags=draft.hashtags,
      suggested_media_type=draft.suggested_media_type.value,
      compliance_checked=compliance_result.is_compliant,
      created_at=draft.created_at
    ))
  # Persist drafts via repository
  draft_repo = ContentDraftRepository(session)
  await draft_repo.create_many(draft_records_data)
  await session.commit()

  logger.info("Generated %d content drafts", len(api_drafts))
  return api_drafts

@router.get("/trends", response_model=List[TrendAnalysis])
async def get_trends(
  category: Optional[str] = None,
  limit: int = 10,
  session: AsyncSession = Depends(get_session)
) -> List[TrendAnalysis]:
  """
  Get trend analysis results.

  Args:
    category: Filter by content category
    limit: Maximum number of trends to return
  """
  logger.info("Fetching trends - category: %s, limit: %s", category, limit)

  repo = TrendAnalysisRepository(session)
  rows = await repo.list(category=category, limit=limit)
  analyses: List[TrendAnalysis] = []
  for r in rows:
    analyses.append(TrendAnalysis(
      post_id=str(getattr(r, 'post_id')),
      hook=str(getattr(r, 'hook')),
      cta=str(getattr(r, 'cta')),
      content_category=str(getattr(r, 'content_category')),
      relevance_score=float(getattr(r, 'relevance_score') or 0.0),
      virality_score=float(getattr(r, 'virality_score') or 0.0),
      summary=str(getattr(r, 'summary')),
      key_themes=list(getattr(r, 'key_themes') or []),
      engagement_factors=list(getattr(r, 'engagement_factors') or []),
      compliance_notes=getattr(r, 'compliance_notes'),
      analyzed_at=getattr(r, 'analyzed_at')
    ))
  return analyses

@router.get("/drafts", response_model=List[ContentDraft])
async def get_drafts(
  category: Optional[str] = None,
  limit: int = 5,
  platform: Optional[str] = None,
  session: AsyncSession = Depends(get_session)
) -> List[ContentDraft]:
  """
  Get content drafts for review.

  Args:
    category: Filter by content category
    limit: Maximum number of drafts to return
    platform: Filter by platform
  """
  logger.info("Fetching drafts - category: %s, limit: %d, platform: %s",
              category, limit, platform)

  repo = ContentDraftRepository(session)
  rows = await repo.list(platform=platform, limit=limit)
  drafts: List[ContentDraft] = []
  for r in rows:
    drafts.append(ContentDraft(
      id=str(getattr(r, 'id')),
      platform=str(getattr(r, 'platform')),
      caption=str(getattr(r, 'caption')),
      hashtags=list(getattr(r, 'hashtags') or []),
      suggested_media_type=str(getattr(r, 'suggested_media_type')),
      compliance_checked=bool(getattr(r, 'compliance_checked')),
      created_at=getattr(r, 'created_at')
    ))
  return drafts

@router.get("/brief")
async def get_daily_brief(date: Optional[str] = None) -> Dict[str, Any]:
  """
  Get daily brief with top trends and drafts.

  Args:
    date: Date for the brief (YYYY-MM-DD format)
  """
  logger.info("Fetching daily brief for date: %s", date)

  # TODO: Implement daily brief generation
  # Mock response for now
  return {
    "date": date or datetime.utcnow().strftime("%Y-%m-%d"),
    "top_trends": [
      {
        "post_id": "trend_1",
        "hook": "✨ 3-second glow up!",
        "virality_score": 0.88,
        "content_category": "Transformation"
      }
    ],
    "content_drafts": [
      {
        "id": "draft_001",
        "platform": "instagram",
        "caption": "Mock draft caption",
        "hashtags": ["#mock"]
      }
    ],
    "summary": "Daily viral content analysis complete"
  }

@router.post("/digest/send")
async def send_daily_digest(session: AsyncSession = Depends(get_session)) -> Dict[str, Any]:
  """Generate and send (mock) daily digest email."""
  trend_repo = TrendAnalysisRepository(session)
  draft_repo = ContentDraftRepository(session)
  trends = await trend_repo.list(limit=5)
  drafts = await draft_repo.list(limit=5)
  dispatcher = EmailDispatcher()
  trends_payload = [
    {
      "hook": t.hook,
      "content_category": t.content_category,
      "virality_score": t.virality_score
    } for t in trends
  ]
  drafts_payload = [
    {
      "platform": d.platform,
      "caption": d.caption,
      "id": d.id
    } for d in drafts
  ]
  email_payload = await dispatcher.build_digest(trends=trends_payload, drafts=drafts_payload)
  # In future we will set recipients from config/env
  email_payload.to = ["christine.carrer@hotmail.com", "dalkeith@golden.net"]
  sent = await dispatcher.send_email(email_payload)
  return {"status": "sent" if sent else "failed", "subject": email_payload.subject, "recipients": email_payload.to}

@router.post("/scrape")
async def trigger_scraping() -> Dict[str, Any]:
  """
  Trigger scraping of competitor content.

  This endpoint initiates the scraping process for viral content
  from competitor social media accounts.
  """
  try:
    orchestrator = ScrapingOrchestrator()
  except Exception as e:  # noqa: BLE001
    raise HTTPException(status_code=500, detail=f"Scraping orchestrator init failed: {e}") from e
  try:
    items = await orchestrator.scrape(include_tiktok=False)
    # Try to increment metrics if available
    try:  # best-effort metrics
      import main  # type: ignore
      if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
        main.app.state.metrics['scrape_runs'] = main.app.state.metrics.get('scrape_runs', 0) + 1
        main.app.state.metrics['scrape_items_collected'] = main.app.state.metrics.get('scrape_items_collected', 0) + len(items)
    except Exception:  # noqa: BLE001
      pass
    return {
      "status": "ok",
      "items": len(items),
      "timestamp": datetime.utcnow().isoformat()
    }
  except Exception as e:  # noqa: BLE001
    logger.error("Scraping failed: %s", e)
    try:
      import main  # type: ignore
      if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
        main.app.state.metrics['scrape_failures'] = main.app.state.metrics.get('scrape_failures', 0) + 1
    except Exception:  # noqa: BLE001
      pass
    raise HTTPException(status_code=500, detail=f"Scraping failed: {e}") from e

async def send_email_digest():
  """Background task to send email digest."""
  # TODO: Implement EmailDispatcher agent
  logger.info("Email digest sent (mock)")
  pass

@router.post("/scrape/apify")
async def scrape_with_apify(include_tiktok: bool = False, session: AsyncSession = Depends(get_session)) -> Dict[str, Any]:
  """Run Apify-based scraping orchestrator and ingest results via pipeline."""
  try:
    orchestrator = ScrapingOrchestrator()
  except Exception as e:  # noqa: BLE001
    raise HTTPException(status_code=500, detail=f"Scraping orchestrator init failed: {e}") from e
  try:
    items = await orchestrator.scrape(include_tiktok=include_tiktok)
    # metrics
    try:
      import main  # type: ignore
      if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
        main.app.state.metrics['scrape_runs'] = main.app.state.metrics.get('scrape_runs', 0) + 1
        main.app.state.metrics['scrape_items_collected'] = main.app.state.metrics.get('scrape_items_collected', 0) + len(items)
    except Exception:  # noqa: BLE001
      pass
    if not items:
      return {"status": "empty", "ingested": 0}
    from app.pipeline import process_posts  # local import to avoid cycles
    result = await process_posts(session=session, posts=items, refine=True)
    return {"status": "ok", **result}
  except Exception as e:  # noqa: BLE001
    logger.error("Apify scraping failed: %s", e)
    try:
      import main  # type: ignore
      if hasattr(main, 'app') and hasattr(main.app.state, 'metrics'):
        main.app.state.metrics['scrape_failures'] = main.app.state.metrics.get('scrape_failures', 0) + 1
    except Exception:  # noqa: BLE001
      pass
    raise HTTPException(status_code=500, detail=f"Apify scraping failed: {e}") from e
