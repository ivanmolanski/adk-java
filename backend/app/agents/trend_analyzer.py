"""
TrendAnalyzer Agent

Pydantic-based agent for analyzing viral social media posts.
Extracts hooks, CTAs, content categories, and calculates relevance scores.
"""

from pydantic import BaseModel, Field, validator
from typing import List, Dict, Any, Optional
import logging
import re
from datetime import datetime
from enum import Enum

logger = logging.getLogger(__name__)

class ContentCategory(str, Enum):
    """Content categorization for MD Aesthetics focus areas."""
    PROCESS_DEMYSTIFIED = "process_demystified"
    SCIENCE_EXPLAINED = "science_explained"
    TRANSFORMATION = "transformation"
    MYTH_BUSTING = "myth_busting"
    EDUCATIONAL = "educational"
    BEHIND_SCENES = "behind_scenes"

class ViralPostData(BaseModel):
    """Input model for viral post data."""
    id: str
    platform: str
    profile: str
    caption: str
    hashtags: List[str]
    engagement_rate: float
    likes: int
    comments: int
    shares: int = 0
    views: int = 0
    post_url: str
    scraped_at: datetime

class TrendAnalysisResult(BaseModel):
    """Output model for trend analysis results."""
    post_id: str
    hook: str = Field(..., description="The opening 3-second hook")
    cta: str = Field(..., description="Call-to-action identified")
    content_category: ContentCategory
    relevance_score: float = Field(..., ge=0.0, le=1.0, description="Relevance to MD Aesthetics (0-1)")
    virality_score: float = Field(..., ge=0.0, le=1.0, description="Viral potential score (0-1)")
    summary: str = Field(..., description="Brief analysis summary")
    key_themes: List[str] = Field(default_factory=list, description="Key themes identified")
    engagement_factors: List[str] = Field(default_factory=list, description="Factors driving engagement")
    compliance_notes: Optional[str] = Field(None, description="Compliance concerns if any")
    analyzed_at: datetime = Field(default_factory=datetime.utcnow)

    @validator('relevance_score', 'virality_score')
    def validate_scores(cls, v):
        """Ensure scores are between 0 and 1."""
        if not 0 <= v <= 1:
            raise ValueError('Score must be between 0 and 1')
        return v

class TrendAnalyzer(BaseModel):
    """
    Pydantic-based TrendAnalyzer agent for MD Aesthetics.
    
    Analyzes viral posts from competitors to identify:
    - Effective hooks and opening strategies
    - Call-to-action patterns
    - Content categorization
    - Relevance to MD Aesthetics services
    - Viral potential scoring
    """
    
    name: str = "TrendAnalyzer"
    version: str = "2.0.0"
    description: str = "Analyzes viral content for MD Aesthetics competitive intelligence"
    
    # Configuration
    min_engagement_threshold: float = Field(default=5.0, description="Minimum engagement rate to consider")
    relevant_keywords: List[str] = Field(
        default_factory=lambda: [
            "aesthetic", "skincare", "treatment", "facial", "botox", "filler",
            "ultherapy", "radiesse", "skintype", "lift", "smooth", "firm",
            "vitamin c", "medical grade", "physician", "consultation"
        ]
    )
    
    class Config:
        """Pydantic configuration."""
        arbitrary_types_allowed = True
    
    def analyze_post(self, post: ViralPostData) -> TrendAnalysisResult:
        """
        Analyze a single viral post.
        
        Args:
            post: The viral post data to analyze
            
        Returns:
            TrendAnalysisResult with analysis details
        """
        logger.info(f"Analyzing post {post.id} from {post.profile}")
        
        # Extract hook (first sentence or first 50 characters)
        hook = self._extract_hook(post.caption)
        
        # Identify call-to-action
        cta = self._extract_cta(post.caption)
        
        # Categorize content
        category = self._categorize_content(post.caption, post.hashtags)
        
        # Calculate relevance score
        relevance_score = self._calculate_relevance_score(post)
        
        # Calculate virality score
        virality_score = self._calculate_virality_score(post)
        
        # Extract themes
        themes = self._extract_themes(post.caption, post.hashtags)
        
        # Identify engagement factors
        engagement_factors = self._identify_engagement_factors(post)
        
        # Generate summary
        summary = self._generate_summary(post, category, relevance_score, virality_score)
        
        # Check compliance
        compliance_notes = self._check_compliance(post.caption)
        
        return TrendAnalysisResult(
            post_id=post.id,
            hook=hook,
            cta=cta,
            content_category=category,
            relevance_score=relevance_score,
            virality_score=virality_score,
            summary=summary,
            key_themes=themes,
            engagement_factors=engagement_factors,
            compliance_notes=compliance_notes
        )
    
    def analyze_batch(self, posts: List[ViralPostData]) -> List[TrendAnalysisResult]:
        """
        Analyze multiple posts in batch.
        
        Args:
            posts: List of viral posts to analyze
            
        Returns:
            List of analysis results
        """
        logger.info(f"Analyzing batch of {len(posts)} posts")
        
        results = []
        for post in posts:
            if post.engagement_rate >= self.min_engagement_threshold:
                try:
                    result = self.analyze_post(post)
                    results.append(result)
                except Exception as e:
                    logger.error(f"Error analyzing post {post.id}: {e}")
            else:
                logger.debug(f"Skipping post {post.id} - low engagement ({post.engagement_rate}%)")
        
        logger.info(f"Completed analysis of {len(results)} posts")
        return results
    
    def _extract_hook(self, caption: str) -> str:
        """Extract the opening hook from caption."""
        # First sentence or first 50 characters
        sentences = re.split(r'[.!?]+', caption.strip())
        if sentences and sentences[0]:
            hook = sentences[0].strip()
            return hook[:100] + "..." if len(hook) > 100 else hook
        return caption[:50] + "..." if len(caption) > 50 else caption
    
    def _extract_cta(self, caption: str) -> str:
        """Extract call-to-action from caption."""
        cta_patterns = [
            r'book\s+(?:now|today|consultation)',
            r'call\s+(?:us|now|today)',
            r'link\s+in\s+bio',
            r'dm\s+(?:us|me)',
            r'schedule\s+(?:now|today)',
            r'tap\s+(?:link|bio)',
            r'visit\s+(?:our|the)',
            r'contact\s+us'
        ]
        
        for pattern in cta_patterns:
            match = re.search(pattern, caption.lower())
            if match:
                # Extract surrounding context
                start = max(0, match.start() - 20)
                end = min(len(caption), match.end() + 20)
                return caption[start:end].strip()
        
        # Look for action words at end of caption
        action_words = ['book', 'call', 'visit', 'schedule', 'contact', 'dm']
        words = caption.lower().split()
        for i, word in enumerate(words[-10:], start=len(words)-10):
            if any(action in word for action in action_words):
                return ' '.join(words[i:]).strip()
        
        return "No clear CTA identified"
    
    def _categorize_content(self, caption: str, hashtags: List[str]) -> ContentCategory:
        """Categorize the content type."""
        caption_lower = caption.lower()
        hashtags_lower = [tag.lower() for tag in hashtags]
        
        # Process Demystified indicators
        if any(word in caption_lower for word in ['procedure', 'treatment', 'process', 'how', 'step']):
            return ContentCategory.PROCESS_DEMYSTIFIED
        
        # Science Explained indicators  
        if any(word in caption_lower for word in ['science', 'research', 'study', 'collagen', 'peptide']):
            return ContentCategory.SCIENCE_EXPLAINED
        
        # Transformation indicators
        if any(word in caption_lower for word in ['before', 'after', 'results', 'transformation']):
            return ContentCategory.TRANSFORMATION
        
        # Myth Busting indicators
        if any(word in caption_lower for word in ['myth', 'fact', 'truth', 'debunk', 'misconception']):
            return ContentCategory.MYTH_BUSTING
        
        # Educational default
        return ContentCategory.EDUCATIONAL
    
    def _calculate_relevance_score(self, post: ViralPostData) -> float:
        """Calculate how relevant the post is to MD Aesthetics."""
        score = 0.0
        text = f"{post.caption} {' '.join(post.hashtags)}".lower()
        
        # Keyword matching
        keyword_matches = sum(1 for keyword in self.relevant_keywords if keyword in text)
        keyword_score = min(keyword_matches * 0.1, 0.5)
        
        # Platform bonus
        platform_score = 0.1 if post.platform == 'instagram' else 0.05
        
        # Engagement quality
        engagement_score = min(post.engagement_rate * 0.02, 0.3)
        
        # Profile relevance (competitor profiles get higher scores)
        competitor_profiles = ['_thelookaesthetics', 'subtle.enhancements', 'skinvitalityofficial']
        profile_score = 0.2 if any(comp in post.profile.lower() for comp in competitor_profiles) else 0.1
        
        total_score = keyword_score + platform_score + engagement_score + profile_score
        return min(total_score, 1.0)
    
    def _calculate_virality_score(self, post: ViralPostData) -> float:
        """Calculate the viral potential score."""
        # Engagement rate is primary factor
        engagement_score = min(post.engagement_rate * 0.05, 0.5)
        
        # Absolute numbers matter too
        likes_score = min(post.likes / 10000 * 0.2, 0.2)
        comments_score = min(post.comments / 100 * 0.2, 0.2)
        
        # Platform multiplier
        platform_multiplier = 1.2 if post.platform == 'tiktok' else 1.0
        
        total_score = (engagement_score + likes_score + comments_score) * platform_multiplier
        return min(total_score, 1.0)
    
    def _extract_themes(self, caption: str, hashtags: List[str]) -> List[str]:
        """Extract key themes from the content."""
        themes = []
        
        # Theme keywords mapping
        theme_keywords = {
            'skincare': ['skincare', 'skin', 'facial', 'treatment'],
            'anti-aging': ['aging', 'wrinkles', 'fine lines', 'youth'],
            'injectables': ['botox', 'filler', 'injection', 'radiesse'],
            'technology': ['laser', 'ultherapy', 'radiofrequency', 'microneedling'],
            'results': ['results', 'before', 'after', 'transformation'],
            'education': ['tips', 'advice', 'how to', 'science']
        }
        
        text = f"{caption} {' '.join(hashtags)}".lower()
        
        for theme, keywords in theme_keywords.items():
            if any(keyword in text for keyword in keywords):
                themes.append(theme)
        
        return themes[:5]  # Limit to top 5 themes
    
    def _identify_engagement_factors(self, post: ViralPostData) -> List[str]:
        """Identify factors that likely drove engagement."""
        factors = []
        
        if post.engagement_rate > 10:
            factors.append("high_engagement_rate")
        
        if post.likes > 1000:
            factors.append("high_like_count")
        
        if post.comments > 50:
            factors.append("high_comment_count")
        
        if any(emoji in post.caption for emoji in ['✨', '💫', '🔥', '💖']):
            factors.append("emoji_usage")
        
        if len([tag for tag in post.hashtags if 'trend' in tag.lower()]) > 0:
            factors.append("trending_hashtags")
        
        return factors
    
    def _generate_summary(self, post: ViralPostData, category: ContentCategory, 
                         relevance: float, virality: float) -> str:
        """Generate a summary of the analysis."""
        return (f"{category.value.replace('_', ' ').title()} content from {post.profile} "
                f"with {relevance:.1%} relevance and {virality:.1%} viral potential. "
                f"Engagement: {post.engagement_rate}% ({post.likes} likes, {post.comments} comments)")
    
    def _check_compliance(self, caption: str) -> Optional[str]:
        """Check for potential compliance issues."""
        issues = []
        
        # Check for direct medical claims
        medical_claims = ['cure', 'heal', 'medical treatment', 'guaranteed results']
        for claim in medical_claims:
            if claim in caption.lower():
                issues.append(f"Contains medical claim: '{claim}'")
        
        # Check for pricing mentions
        if any(char in caption for char in ['$', '£', '€']) or 'price' in caption.lower():
            issues.append("Contains pricing information")
        
        return "; ".join(issues) if issues else None