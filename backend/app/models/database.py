"""Database setup and models for MD Aesthetics viral intelligence system."""

import os
from datetime import datetime
from typing import Optional

from sqlalchemy import (
    Column, String, Integer, Float, DateTime, Text, Boolean, JSON, ForeignKey
)
from sqlalchemy.orm import declarative_base, relationship
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker

DATABASE_URL = os.getenv("DB_URL", "sqlite+aiosqlite:///./viral.db")

engine = create_async_engine(DATABASE_URL, echo=False, future=True)
AsyncSessionLocal = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

# Alias for compatibility with scheduler
async_session_maker = AsyncSessionLocal

Base = declarative_base()

class ViralPost(Base):
    __tablename__ = "viral_posts"
    id = Column(String, primary_key=True, index=True)
    platform = Column(String, index=True)
    profile = Column(String, index=True)
    caption = Column(Text)
    hashtags = Column(JSON, default=list)
    engagement_rate = Column(Float)
    likes = Column(Integer, default=0)
    comments = Column(Integer, default=0)
    shares = Column(Integer, default=0)
    views = Column(Integer, default=0)
    post_url = Column(String, unique=True)
    scraped_at = Column(DateTime, default=datetime.utcnow)
    created_at = Column(DateTime, default=datetime.utcnow)
    analyses = relationship("TrendAnalysisRecord", back_populates="post")

class TrendAnalysisRecord(Base):
    __tablename__ = "trend_analysis"
    id = Column(String, primary_key=True, index=True)
    post_id = Column(String, ForeignKey("viral_posts.id", ondelete="CASCADE"))
    hook = Column(Text)
    cta = Column(Text)
    content_category = Column(String, index=True)
    relevance_score = Column(Float)
    virality_score = Column(Float)
    summary = Column(Text)
    key_themes = Column(JSON, default=list)
    engagement_factors = Column(JSON, default=list)
    compliance_notes = Column(Text, nullable=True)
    analyzed_at = Column(DateTime, default=datetime.utcnow)
    created_at = Column(DateTime, default=datetime.utcnow)
    post = relationship("ViralPost", back_populates="analyses")
    drafts = relationship("ContentDraftRecord", back_populates="analysis")

class ContentDraftRecord(Base):
    __tablename__ = "content_drafts"
    id = Column(String, primary_key=True, index=True)
    analysis_id = Column(String, ForeignKey("trend_analysis.id", ondelete="SET NULL"), nullable=True)
    platform = Column(String, index=True)
    caption = Column(Text)
    hashtags = Column(JSON, default=list)
    suggested_media_type = Column(String)
    target_service = Column(String)
    compliance_checked = Column(Boolean, default=True)
    brand_alignment_score = Column(Float)
    estimated_engagement = Column(String)
    suggested_visuals = Column(Text, nullable=True)
    posting_tips = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    analysis = relationship("TrendAnalysisRecord", back_populates="drafts")

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

async def get_session() -> AsyncSession:
    async with AsyncSessionLocal() as session:
        yield session
