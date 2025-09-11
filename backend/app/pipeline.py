"""Pipeline orchestration for ingest -> analyze -> generate -> persist.

This module provides a single entry point `process_posts` which accepts raw
scraped post dictionaries (already normalized) and:
 1. Persists posts (idempotent upsert) via ViralPostRepository
 2. Runs heuristic TrendAnalyzer batch
 3. Generates ContentCreator drafts from analyses
 4. Persists analyses & drafts via repositories
 5. Appends learning composites to LearningStore

It returns a structured dict summarizing counts and payload references for
use in API responses or scheduling jobs.
"""
from __future__ import annotations

from typing import List, Dict, Any, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from datetime import datetime
import logging
import uuid

from app.agents.trend_analyzer import TrendAnalyzer, ViralPostData
from app.agents.content_creator import ContentCreator, TrendInput, Platform
from app.repositories.viral_repositories import (
    ViralPostRepository,
    TrendAnalysisRepository,
    ContentDraftRepository,
    LearningStore,
)

logger = logging.getLogger(__name__)


async def process_posts(*, session: AsyncSession, posts: List[Dict[str, Any]], refine: bool = False) -> Dict[str, Any]:
    if not posts:
        return {"ingested": 0, "analyses": 0, "drafts": 0, "items": []}
    post_repo = ViralPostRepository(session)
    analysis_repo = TrendAnalysisRepository(session)
    draft_repo = ContentDraftRepository(session)

    # 1. Normalize & upsert posts
    normalized: List[Dict[str, Any]] = []
    for raw in posts:
        try:
            pid = str(raw.get("id") or raw.get("post_id") or raw.get("postURL") or uuid.uuid4())
            normalized.append({
                "id": pid,
                "platform": raw.get("platform", "unknown"),
                "profile": raw.get("profile") or raw.get("author", "unknown"),
                "caption": raw.get("caption", ""),
                "hashtags": raw.get("hashtags", []) or [],
                "engagement_rate": float(raw.get("engagement_rate") or 0),
                "likes": int(raw.get("likes") or 0),
                "comments": int(raw.get("comments") or 0),
                "shares": int(raw.get("shares") or 0),
                "views": int(raw.get("views") or 0),
                "post_url": raw.get("post_url") or raw.get("postURL") or raw.get("url", ""),
                "scraped_at": raw.get("scraped_at") or datetime.utcnow(),
            })
        except Exception as exc:  # noqa: BLE001
            logger.warning("Skipping malformed post: %s", exc)
    await post_repo.upsert_many(normalized)

    # 2. Heuristic analysis
    analyzer = TrendAnalyzer()
    agent_posts = [
        ViralPostData(
            id=p["id"], platform=p["platform"], profile=p["profile"], caption=p["caption"],
            hashtags=p["hashtags"], engagement_rate=p["engagement_rate"], likes=p["likes"],
            comments=p["comments"], shares=p["shares"], views=p["views"], post_url=p["post_url"],
            scraped_at=p["scraped_at"] if isinstance(p["scraped_at"], datetime) else datetime.fromisoformat(p["scraped_at"])  # type: ignore[arg-type]
        ) for p in normalized
    ]
    analyses = analyzer.analyze_batch(agent_posts)
    analyses_payload: List[Dict[str, Any]] = []
    for a in analyses:
        analyses_payload.append({
            "post_id": a.post_id,
            "hook": a.hook,
            "cta": a.cta,
            "content_category": a.content_category.value if hasattr(a.content_category, 'value') else str(a.content_category),
            "relevance_score": a.relevance_score,
            "virality_score": a.virality_score,
            "summary": a.summary,
            "key_themes": a.key_themes,
            "engagement_factors": a.engagement_factors,
            "compliance_notes": a.compliance_notes,
            "analyzed_at": a.analyzed_at,
        })
    created_analyses = await analysis_repo.create_many(analyses_payload)

    # 3. Content generation
    creator = ContentCreator()
    trend_inputs: List[TrendInput] = [
        TrendInput(
            hook=a.hook, cta=a.cta, content_category=ap.get("content_category"),
            virality_score=ap.get("virality_score"), key_themes=ap.get("key_themes", []),
            engagement_factors=ap.get("engagement_factors", [])
        ) for a, ap in zip(analyses, analyses_payload)
    ]
    drafts = creator.generate_batch(trend_inputs, Platform.INSTAGRAM, refine=refine)
    draft_payload: List[Dict[str, Any]] = []
    hook_map = {a.hook: a.post_id for a in analyses}
    for d in drafts:
        draft_payload.append({
            "id": d.id,
            "analysis_id": hook_map.get(d.caption.split('\n')[0]),  # best-effort match
            "platform": d.platform.value,
            "caption": d.caption,
            "hashtags": d.hashtags,
            "suggested_media_type": d.suggested_media_type.value,
            "target_service": getattr(d.target_service, 'value', str(d.target_service)),
            "compliance_checked": d.compliance_checked,
            "brand_alignment_score": d.brand_alignment_score,
            "estimated_engagement": d.estimated_engagement,
            "suggested_visuals": d.suggested_visuals,
            "posting_tips": d.posting_tips,
            "created_at": d.created_at,
        })
    created_drafts = await draft_repo.create_many(draft_payload)

    await session.commit()

    # 4. Learning store appends (analysis + drafts)
    for a in created_analyses:
        await LearningStore.append({
            'type': 'analysis',
            'id': a.id,
            'post_id': a.post_id,
            'hook': a.hook,
            'cta': a.cta,
            'category': a.content_category,
            'summary': a.summary,
            'relevance_score': a.relevance_score,
            'virality_score': a.virality_score,
            'themes': a.key_themes,
            'engagement_factors': a.engagement_factors,
            'timestamp': a.analyzed_at.isoformat(),
        })
    for d in created_drafts:
        await LearningStore.append({
            'type': 'draft',
            'id': d.id,
            'analysis_id': d.analysis_id,
            'platform': d.platform,
            'hashtags': d.hashtags,
            'suggested_media_type': d.suggested_media_type,
            'brand_alignment_score': d.brand_alignment_score,
            'timestamp': d.created_at.isoformat(),
        })

    logger.info("Pipeline complete: ingested=%d analyses=%d drafts=%d", len(normalized), len(analyses), len(drafts))
    return {
        "ingested": len(normalized),
        "analyses": len(analyses),
        "drafts": len(drafts),
        "analysis_ids": [a.id for a in created_analyses],
        "draft_ids": [d.id for d in created_drafts],
    }
