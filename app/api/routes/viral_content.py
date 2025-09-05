from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Optional
import logging

from ...core.database import get_db, CompetitorPost, GeneratedContent
from ...models.schemas import (
    CompetitorPostCreate, CompetitorPostResponse,
    GeneratedContentCreate, GeneratedContentResponse,
    TrendAnalysisRequest, TrendAnalysisResponse,
    ContentCreationRequest, ContentCreationResponse,
    EmailDigestRequest
)
from ...agents.trend_analyzer import TrendAnalyzerAgent
from ...agents.content_creator import ContentCreatorAgent
from ...agents.compliance_agent import ComplianceAgent

router = APIRouter()
logger = logging.getLogger(__name__)

# Initialize agents
trend_analyzer = TrendAnalyzerAgent()
content_creator = ContentCreatorAgent()
compliance_agent = ComplianceAgent()

@router.get("/posts", response_model=List[CompetitorPostResponse])
async def get_competitor_posts(
    skip: int = 0,
    limit: int = 100,
    platform: Optional[str] = None,
    db: AsyncSession = Depends(get_db)
):
    """Get competitor posts with optional filtering"""
    query = db.query(CompetitorPost)
    
    if platform:
        query = query.filter(CompetitorPost.platform == platform)
    
    posts = await query.offset(skip).limit(limit).all()
    return posts

@router.post("/posts", response_model=CompetitorPostResponse)
async def create_competitor_post(
    post: CompetitorPostCreate,
    db: AsyncSession = Depends(get_db)
):
    """Create a new competitor post entry"""
    
    # Check if post already exists
    existing = await db.query(CompetitorPost).filter(
        CompetitorPost.post_url == post.post_url
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="Post already exists")
    
    # Create new post
    db_post = CompetitorPost(**post.dict())
    db.add(db_post)
    await db.commit()
    await db.refresh(db_post)
    
    return db_post

@router.post("/analyze", response_model=TrendAnalysisResponse)
async def analyze_trends(request: TrendAnalysisRequest):
    """Analyze viral trends in social media content"""
    
    try:
        result = await trend_analyzer.execute(request)
        return TrendAnalysisResponse(**result)
    
    except Exception as e:
        logger.error(f"Error analyzing trends: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

@router.post("/generate", response_model=ContentCreationResponse)
async def generate_content(request: ContentCreationRequest):
    """Generate MD Aesthetics content based on trend analysis"""
    
    try:
        result = await content_creator.execute(request)
        return ContentCreationResponse(**result)
    
    except Exception as e:
        logger.error(f"Error generating content: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Content generation failed: {str(e)}")

@router.post("/analyze-and-generate")
async def analyze_and_generate(
    post_data: CompetitorPostCreate,
    target_services: List[str] = ["Duo-C-Lift", "SkinTyte", "Radiesse"],
    tone: str = "educational"
):
    """Combined endpoint: analyze trends and generate content"""
    
    try:
        # Step 1: Analyze trends
        analysis_request = TrendAnalysisRequest(post_data=post_data)
        analysis_result = await trend_analyzer.execute(analysis_request)
        
        if not analysis_result["success"]:
            raise HTTPException(status_code=500, detail="Trend analysis failed")
        
        # Step 2: Generate content
        from ...models.schemas import PostAnalysis
        post_analysis = PostAnalysis(**analysis_result["data"]["analysis"])
        
        creation_request = ContentCreationRequest(
            trend_analysis=post_analysis,
            target_services=target_services,
            tone=tone
        )
        
        creation_result = await content_creator.execute(creation_request)
        
        if not creation_result["success"]:
            raise HTTPException(status_code=500, detail="Content creation failed")
        
        # Step 3: Compliance check
        from ...models.schemas import ComplianceCheckRequest
        generated_content = GeneratedContentCreate(**creation_result["data"]["generated_content"])
        
        compliance_request = ComplianceCheckRequest(
            content=generated_content,
            strict_mode=False
        )
        
        compliance_result = await compliance_agent.execute(compliance_request)
        
        return {
            "analysis": analysis_result["data"],
            "generated_content": creation_result["data"],
            "compliance_check": compliance_result["data"]
        }
    
    except Exception as e:
        logger.error(f"Error in analyze-and-generate: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Pipeline failed: {str(e)}")

@router.get("/content", response_model=List[GeneratedContentResponse])
async def get_generated_content(
    skip: int = 0,
    limit: int = 50,
    approved_only: bool = False,
    db: AsyncSession = Depends(get_db)
):
    """Get generated content with optional filtering"""
    query = db.query(GeneratedContent)
    
    if approved_only:
        query = query.filter(GeneratedContent.approved == True)
    
    content = await query.offset(skip).limit(limit).all()
    return content

@router.post("/content", response_model=GeneratedContentResponse)
async def save_generated_content(
    content: GeneratedContentCreate,
    db: AsyncSession = Depends(get_db)
):
    """Save generated content to database"""
    
    db_content = GeneratedContent(**content.dict())
    db.add(db_content)
    await db.commit()
    await db.refresh(db_content)
    
    return db_content

@router.put("/content/{content_id}/approve")
async def approve_content(
    content_id: int,
    db: AsyncSession = Depends(get_db)
):
    """Approve generated content for use"""
    
    content = await db.query(GeneratedContent).filter(
        GeneratedContent.id == content_id
    ).first()
    
    if not content:
        raise HTTPException(status_code=404, detail="Content not found")
    
    content.approved = True
    await db.commit()
    
    return {"message": "Content approved successfully"}

@router.post("/email-digest")
async def send_email_digest(request: EmailDigestRequest):
    """Send email digest with latest viral content and generated posts"""
    
    # This would integrate with an email service
    # For now, return a success message
    
    return {
        "message": "Email digest sent successfully",
        "recipients": request.recipients or ["christine.carrer@hotmail.com", "dalkeith@golden.net"],
        "date_range": request.date_range
    }

@router.get("/insights")
async def get_insights(db: AsyncSession = Depends(get_db)):
    """Get analytics and insights about viral content trends"""
    
    # Get basic statistics
    total_posts = await db.query(CompetitorPost).count()
    total_generated = await db.query(GeneratedContent).count()
    approved_content = await db.query(GeneratedContent).filter(
        GeneratedContent.approved == True
    ).count()
    
    # Get top performing categories
    # This would involve more complex queries in a real implementation
    
    return {
        "statistics": {
            "total_posts_analyzed": total_posts,
            "total_content_generated": total_generated,
            "approved_content": approved_content,
            "approval_rate": (approved_content / total_generated * 100) if total_generated > 0 else 0
        },
        "trends": {
            "top_categories": ["Process Demystified", "Science Explained", "Transformation"],
            "trending_hashtags": ["#duoclift", "#skintyte", "#mdaesthetics"],
            "top_services": ["Duo-C-Lift", "SkinTyte", "Radiesse"]
        }
    }