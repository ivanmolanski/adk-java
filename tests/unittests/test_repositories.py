import pytest
import asyncio
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker

from app.models.database import Base
from app.repositories.viral_repositories import (
    ViralPostRepository, TrendAnalysisRepository, ContentDraftRepository
)

TEST_DB_URL = "sqlite+aiosqlite:///./test_repo.db"


@pytest.fixture(scope="module")
def event_loop():
    loop = asyncio.new_event_loop()
    yield loop
    loop.close()


@pytest.fixture(scope="module", async_fixture=True)
async def session():  # type: ignore
    engine = create_async_engine(TEST_DB_URL, echo=False)
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    async_session = sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)
    async with async_session() as s:
        yield s


@pytest.mark.asyncio
async def test_upsert_and_list_posts(session: AsyncSession):
    repo = ViralPostRepository(session)
    await repo.upsert_many([
        {
            "id": "post_1",
            "platform": "instagram",
            "profile": "test_profile",
            "caption": "Test caption",
            "hashtags": ["#test"],
            "engagement_rate": 0.5,
            "likes": 10,
            "comments": 2,
            "shares": 1,
            "views": 100,
            "post_url": "http://example.com/post_1",
        }
    ])
    posts = await repo.list_posts()
    assert len(posts) == 1
    assert posts[0].id == "post_1"


@pytest.mark.asyncio
async def test_create_analyses(session: AsyncSession):
    repo = TrendAnalysisRepository(session)
    created = await repo.create_many([
        {
            "post_id": "post_1",
            "hook": "Hook",
            "cta": "CTA",
            "content_category": "transformation",
            "relevance_score": 0.9,
            "virality_score": 0.8,
            "summary": "Summary",
            "key_themes": ["theme"],
            "engagement_factors": ["factor"],
        }
    ])
    assert len(created) == 1
    found = await repo.list(limit=5)
    assert any(a.hook == "Hook" for a in found)


@pytest.mark.asyncio
async def test_create_drafts(session: AsyncSession):
    repo = ContentDraftRepository(session)
    created = await repo.create_many([
        {
            "platform": "instagram",
            "caption": "Caption",
            "hashtags": ["#x"],
            "suggested_media_type": "single_image",
            "brand_alignment_score": 0.8,
            "estimated_engagement": "High",
        }
    ])
    assert len(created) == 1
    listed = await repo.list(limit=5)
    assert len(listed) >= 1
