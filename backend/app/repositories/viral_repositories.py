"""Repository layer for Viral Intelligence domain.

Encapsulates all persistence operations for:
 - ViralPost
 - TrendAnalysisRecord
 - ContentDraftRecord

This abstraction reduces duplication in API routers and facilitates
unit testing by allowing repositories to be mocked.
"""

from __future__ import annotations

from typing import List, Optional, Iterable, Sequence, Dict, Any
from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import joinedload
import uuid

from app.models.database import (
    ViralPost as ViralPostRecord,
    TrendAnalysisRecord,
    ContentDraftRecord,
)

import json
from pathlib import Path
import os
import asyncio

LEARNING_STORE_PATH = Path(os.getenv('LEARNING_STORE_PATH', 'data/learning_store.jsonl'))  # type: ignore[name-defined]

class LearningStore:
    """Append-only JSONL persistence for combined analysis + draft outputs.

    Acts as a lightweight knowledge base the system can mine for patterns
    (e.g., successful hooks, CTA phrasing) without introducing a heavier
    vector DB yet.
    """

    _lock = asyncio.Lock()

    @classmethod
    async def append(cls, record: dict) -> None:
        LEARNING_STORE_PATH.parent.mkdir(parents=True, exist_ok=True)
        line = json.dumps(record, ensure_ascii=False)
        async with cls._lock:
            # Use thread executor to avoid blocking event loop on file IO
            loop = asyncio.get_running_loop()
            await loop.run_in_executor(None, cls._write_line, line)

    @staticmethod
    def _write_line(line: str) -> None:
        with LEARNING_STORE_PATH.open('a', encoding='utf-8') as f:  # noqa: PTH123
            f.write(line + '\n')

    @classmethod
    async def tail(cls, n: int = 50) -> list[dict]:
        if not LEARNING_STORE_PATH.exists():
            return []
        loop = asyncio.get_running_loop()
        content = await loop.run_in_executor(None, LEARNING_STORE_PATH.read_text, 'utf-8')
        lines = [l for l in content.splitlines() if l.strip()]
        result: list[dict] = []
        for raw in lines[-n:]:
            try:
                result.append(json.loads(raw))
            except json.JSONDecodeError:
                continue
        return result

class LearningRepository:
    """Facade combining DB repositories with JSONL learning store."""

    def __init__(self, session: AsyncSession):
        self.posts = ViralPostRepository(session)
        self.analyses = TrendAnalysisRepository(session)
        self.drafts = ContentDraftRepository(session)

    async def persist_batch(self, posts: list[dict], analyses: list[dict], drafts: list[dict]) -> None:
        await self.posts.upsert_many(posts)
        created_analyses = await self.analyses.create_many(analyses)
        created_drafts = await self.drafts.create_many(drafts)
        # Append summarized composite entries to learning store
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

    async def recent_learning(self, n: int = 25) -> list[dict]:
        return await LearningStore.tail(n)


class ViralPostRepository:
    """Data access for ViralPost records."""

    def __init__(self, session: AsyncSession):
        self.session = session

    async def list_posts(self, platform: Optional[str] = None, limit: int = 50) -> List[ViralPostRecord]:
        stmt = select(ViralPostRecord).order_by(ViralPostRecord.scraped_at.desc()).limit(limit)
        if platform:
            stmt = stmt.where(ViralPostRecord.platform == platform)
        return list((await self.session.execute(stmt)).scalars().all())

    async def get(self, post_id: str) -> Optional[ViralPostRecord]:
        return await self.session.get(ViralPostRecord, post_id)

    async def upsert_many(self, posts: Iterable[Dict[str, Any]]) -> None:
        """Insert posts if they do not already exist.

        Args:
            posts: Iterable of dict-like objects containing post fields.
        """
        for p in posts:
            existing = await self.get(p["id"])  # type: ignore[index]
            if existing:
                continue
            record = ViralPostRecord(
                id=p["id"],
                platform=p["platform"],
                profile=p["profile"],
                caption=p["caption"],
                hashtags=p.get("hashtags", []),
                engagement_rate=p.get("engagement_rate", 0.0),
                likes=p.get("likes", 0),
                comments=p.get("comments", 0),
                shares=p.get("shares", 0),
                views=p.get("views", 0),
                post_url=p["post_url"],
                scraped_at=p.get("scraped_at", datetime.utcnow()),
            )
            self.session.add(record)
        await self.session.flush()


class TrendAnalysisRepository:
    """Data access for TrendAnalysis records."""

    def __init__(self, session: AsyncSession):
        self.session = session

    async def list(self, category: Optional[str] = None, limit: int = 10) -> List[TrendAnalysisRecord]:
        stmt = select(TrendAnalysisRecord).order_by(TrendAnalysisRecord.analyzed_at.desc()).limit(limit)
        if category:
            stmt = stmt.where(TrendAnalysisRecord.content_category == category)
        return list((await self.session.execute(stmt)).scalars().all())

    async def create_many(self, analyses: Iterable[Dict[str, Any]]) -> List[TrendAnalysisRecord]:
        created: List[TrendAnalysisRecord] = []
        for a in analyses:
            record = TrendAnalysisRecord(
                id=a.get("id", str(uuid.uuid4())),
                post_id=a["post_id"],
                hook=a["hook"],
                cta=a["cta"],
                content_category=a["content_category"],
                relevance_score=a["relevance_score"],
                virality_score=a["virality_score"],
                summary=a["summary"],
                key_themes=a.get("key_themes", []),
                engagement_factors=a.get("engagement_factors", []),
                compliance_notes=a.get("compliance_notes"),
                analyzed_at=a.get("analyzed_at", datetime.utcnow()),
            )
            self.session.add(record)
            created.append(record)
        await self.session.flush()
        return created

    async def map_hook_to_id(self) -> Dict[str, str]:
        stmt = select(TrendAnalysisRecord.hook, TrendAnalysisRecord.id)
        rows = (await self.session.execute(stmt)).all()
        return {hook: id_ for hook, id_ in rows}


class ContentDraftRepository:
    """Data access for ContentDraft records."""

    def __init__(self, session: AsyncSession):
        self.session = session

    async def list(self, platform: Optional[str] = None, limit: int = 5) -> List[ContentDraftRecord]:
        stmt = select(ContentDraftRecord).order_by(ContentDraftRecord.created_at.desc()).limit(limit)
        if platform:
            stmt = stmt.where(ContentDraftRecord.platform == platform)
        return list((await self.session.execute(stmt)).scalars().all())

    async def create_many(self, drafts: Iterable[Dict[str, Any]]) -> List[ContentDraftRecord]:
        created: List[ContentDraftRecord] = []
        for d in drafts:
            record = ContentDraftRecord(
                id=d.get("id", str(uuid.uuid4())),
                analysis_id=d.get("analysis_id"),
                platform=d["platform"],
                caption=d["caption"],
                hashtags=d.get("hashtags", []),
                suggested_media_type=d["suggested_media_type"],
                target_service=d.get("target_service"),
                compliance_checked=d.get("compliance_checked", True),
                brand_alignment_score=d.get("brand_alignment_score", 0.0),
                estimated_engagement=d.get("estimated_engagement"),
                suggested_visuals=d.get("suggested_visuals"),
                posting_tips=d.get("posting_tips"),
                created_at=d.get("created_at", datetime.utcnow()),
            )
            self.session.add(record)
            created.append(record)
        await self.session.flush()
        return created
