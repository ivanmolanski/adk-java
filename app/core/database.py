from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy import Column, Integer, String, DateTime, Text, Float, Boolean, JSON
from datetime import datetime
import asyncio
from .config import get_settings

# Database models
class Base(DeclarativeBase):
    pass

class CompetitorPost(Base):
    __tablename__ = "competitor_posts"
    
    id = Column(Integer, primary_key=True, index=True)
    platform = Column(String(50), nullable=False)
    profile_url = Column(String(500), nullable=False)
    post_url = Column(String(500), nullable=False, unique=True)
    caption = Column(Text)
    hashtags = Column(JSON)  # List of hashtags
    likes = Column(Integer, default=0)
    comments = Column(Integer, default=0)
    shares = Column(Integer, default=0)
    views = Column(Integer, default=0)
    engagement_rate = Column(Float)
    post_date = Column(DateTime)
    scraped_at = Column(DateTime, default=datetime.utcnow)
    
    # Analysis fields
    relevance_score = Column(Float)
    virality_score = Column(Float)
    content_category = Column(String(100))  # 'Process Demystified', 'Science Explained', etc.
    hook_analysis = Column(Text)
    cta_analysis = Column(Text)

class GeneratedContent(Base):
    __tablename__ = "generated_content"
    
    id = Column(Integer, primary_key=True, index=True)
    source_post_id = Column(Integer, nullable=True)  # Reference to CompetitorPost
    platform = Column(String(50), nullable=False)
    caption = Column(Text, nullable=False)
    hashtags = Column(JSON)  # List of hashtags
    suggested_media_type = Column(String(100))
    compliance_checked = Column(Boolean, default=False)
    brand_voice_score = Column(Float)
    created_at = Column(DateTime, default=datetime.utcnow)
    approved = Column(Boolean, default=False)

class AnalysisSession(Base):
    __tablename__ = "analysis_sessions"
    
    id = Column(Integer, primary_key=True, index=True)
    session_type = Column(String(100), nullable=False)  # 'daily_analysis', 'on_demand'
    posts_analyzed = Column(Integer, default=0)
    content_generated = Column(Integer, default=0)
    started_at = Column(DateTime, default=datetime.utcnow)
    completed_at = Column(DateTime, nullable=True)
    status = Column(String(50), default='running')  # 'running', 'completed', 'failed'
    error_message = Column(Text, nullable=True)

# Database connection
engine = None
async_session = None

async def init_db():
    """Initialize database connection and create tables"""
    global engine, async_session
    
    settings = get_settings()
    engine = create_async_engine(
        settings.database_url,
        echo=settings.debug,
        future=True
    )
    
    async_session = async_sessionmaker(
        engine, class_=AsyncSession, expire_on_commit=False
    )
    
    # Create tables
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

async def get_db() -> AsyncSession:
    """Get database session dependency"""
    if async_session is None:
        await init_db()
    
    async with async_session() as session:
        try:
            yield session
        finally:
            await session.close()

async def close_db():
    """Close database connection"""
    global engine
    if engine:
        await engine.dispose()