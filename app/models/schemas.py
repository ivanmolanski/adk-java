from pydantic import BaseModel, Field, validator
from typing import List, Optional, Dict, Any, Literal
from datetime import datetime
from enum import Enum

class ContentCategory(str, Enum):
    """Content categories for viral analysis"""
    PROCESS_DEMYSTIFIED = "Process Demystified"
    SCIENCE_EXPLAINED = "Science Explained"
    TRANSFORMATION = "Transformation"
    MYTH_BUSTING = "Myth Busting"
    GENERAL = "General"

class Platform(str, Enum):
    """Social media platforms"""
    INSTAGRAM = "instagram"
    TIKTOK = "tiktok"
    FACEBOOK = "facebook"
    YOUTUBE = "youtube"

class PostAnalysis(BaseModel):
    """Analysis result for a social media post"""
    hook: str = Field(..., description="The 3-second hook that grabs attention")
    cta: str = Field(..., description="Call-to-action identified in the post")
    content_category: ContentCategory = Field(..., description="Category of content")
    pacing: Optional[str] = Field(None, description="Video pacing analysis")
    audio_used: Optional[str] = Field(None, description="Audio/music analysis")
    thematic_keywords: List[str] = Field(default_factory=list, description="Key themes")
    educational_value: float = Field(ge=0, le=10, description="Educational value score 0-10")
    brand_alignment: float = Field(ge=0, le=10, description="Alignment with MD Aesthetics brand 0-10")

class CompetitorPostCreate(BaseModel):
    """Schema for creating competitor posts"""
    platform: Platform
    profile_url: str = Field(..., max_length=500)
    post_url: str = Field(..., max_length=500)
    caption: Optional[str] = None
    hashtags: List[str] = Field(default_factory=list)
    likes: int = Field(default=0, ge=0)
    comments: int = Field(default=0, ge=0)
    shares: int = Field(default=0, ge=0)
    views: int = Field(default=0, ge=0)
    post_date: Optional[datetime] = None
    
    @validator('hashtags')
    def validate_hashtags(cls, v):
        """Ensure hashtags start with #"""
        return [tag if tag.startswith('#') else f'#{tag}' for tag in v]

class CompetitorPostResponse(CompetitorPostCreate):
    """Schema for competitor post responses"""
    id: int
    engagement_rate: Optional[float] = None
    scraped_at: datetime
    relevance_score: Optional[float] = None
    virality_score: Optional[float] = None
    content_category: Optional[ContentCategory] = None
    hook_analysis: Optional[str] = None
    cta_analysis: Optional[str] = None
    
    class Config:
        from_attributes = True

class GeneratedContentCreate(BaseModel):
    """Schema for creating generated content"""
    source_post_id: Optional[int] = None
    platform: Platform
    caption: str = Field(..., min_length=10, max_length=2200)
    hashtags: List[str] = Field(..., min_items=5, max_items=15)
    suggested_media_type: str = Field(..., max_length=100)
    
    @validator('caption')
    def validate_caption_compliance(cls, v):
        """Ensure compliance with MD Aesthetics rules"""
        forbidden_words = ['botox']
        v_lower = v.lower()
        for word in forbidden_words:
            if word in v_lower:
                raise ValueError(f"Caption contains forbidden word: {word}. Use 'Tox', 'Neuromodulator', or 'Neurotoxin' instead.")
        return v
    
    @validator('hashtags')
    def validate_hashtags_content(cls, v):
        """Validate hashtag content and format"""
        validated_hashtags = []
        for tag in v:
            tag = tag.strip()
            if not tag.startswith('#'):
                tag = f'#{tag}'
            # Remove any spaces or special chars except underscores
            tag = ''.join(c for c in tag if c.isalnum() or c in ['#', '_'])
            if len(tag) > 1:  # Must have content after #
                validated_hashtags.append(tag)
        return validated_hashtags

class GeneratedContentResponse(GeneratedContentCreate):
    """Schema for generated content responses"""
    id: int
    compliance_checked: bool = False
    brand_voice_score: Optional[float] = None
    created_at: datetime
    approved: bool = False
    
    class Config:
        from_attributes = True

class AgentRequest(BaseModel):
    """Base request for agent operations"""
    session_id: Optional[str] = None
    context: Dict[str, Any] = Field(default_factory=dict)

class TrendAnalysisRequest(AgentRequest):
    """Request for trend analysis agent"""
    post_data: CompetitorPostCreate
    analyze_competitors: bool = True
    include_historical: bool = False

class ContentCreationRequest(AgentRequest):
    """Request for content creation agent"""
    trend_analysis: PostAnalysis
    target_services: List[str] = Field(
        default=["Duo-C-Lift", "SkinTyte", "Radiesse", "Vivier"],
        description="MD Aesthetics services to focus on"
    )
    tone: Literal["educational", "conversational", "authoritative"] = "educational"

class ComplianceCheckRequest(AgentRequest):
    """Request for compliance checking"""
    content: GeneratedContentCreate
    strict_mode: bool = True

class EmailDigestRequest(BaseModel):
    """Request for email digest generation"""
    date_range: Optional[str] = "24h"
    include_generated_content: bool = True
    recipients: Optional[List[str]] = None

class AgentResponse(BaseModel):
    """Base response from agents"""
    success: bool
    message: str
    session_id: str
    execution_time: float
    data: Optional[Dict[str, Any]] = None
    errors: List[str] = Field(default_factory=list)

class TrendAnalysisResponse(AgentResponse):
    """Response from trend analysis agent"""
    analysis: Optional[PostAnalysis] = None
    relevance_score: Optional[float] = None
    virality_score: Optional[float] = None
    recommendations: List[str] = Field(default_factory=list)

class ContentCreationResponse(AgentResponse):
    """Response from content creation agent"""
    generated_content: Optional[GeneratedContentResponse] = None
    alternative_versions: List[GeneratedContentResponse] = Field(default_factory=list)
    brand_voice_score: Optional[float] = None

class ComplianceCheckResponse(AgentResponse):
    """Response from compliance check agent"""
    compliant: bool
    issues: List[str] = Field(default_factory=list)
    suggestions: List[str] = Field(default_factory=list)
    modified_content: Optional[GeneratedContentCreate] = None