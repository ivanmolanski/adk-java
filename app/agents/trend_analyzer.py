from .base_agent import BaseAgent, AgentState
from ..models.schemas import (
    TrendAnalysisRequest, PostAnalysis, ContentCategory,
    CompetitorPostCreate
)
from typing import Dict, Any
import httpx
import json
import re
from ..core.config import get_settings

class TrendAnalyzerAgent(BaseAgent):
    """Pydantic-based trend analysis agent for social media content"""
    
    def __init__(self):
        super().__init__(
            name="TrendAnalyzer",
            description="Analyzes viral social media posts to identify trends and hooks"
        )
        self.settings = get_settings()
    
    async def _execute_impl(self, request: TrendAnalysisRequest, state: AgentState) -> Dict[str, Any]:
        """Analyze post for viral trends and content patterns"""
        
        post_data = request.post_data
        
        # Extract key elements from the post
        hook = self._extract_hook(post_data.caption)
        cta = self._extract_cta(post_data.caption)
        content_category = self._categorize_content(post_data.caption, post_data.hashtags)
        
        # Analyze engagement patterns
        engagement_rate = self._calculate_engagement_rate(post_data)
        virality_score = self._calculate_virality_score(post_data, engagement_rate)
        relevance_score = self._calculate_relevance_score(post_data)
        
        # Extract thematic keywords
        thematic_keywords = self._extract_keywords(post_data.caption, post_data.hashtags)
        
        # Create analysis result
        analysis = PostAnalysis(
            hook=hook,
            cta=cta,
            content_category=content_category,
            thematic_keywords=thematic_keywords,
            educational_value=self._score_educational_value(post_data.caption),
            brand_alignment=self._score_brand_alignment(post_data.caption, thematic_keywords)
        )
        
        # Generate recommendations
        recommendations = self._generate_recommendations(analysis, post_data)
        
        return {
            "analysis": analysis.dict(),
            "relevance_score": relevance_score,
            "virality_score": virality_score,
            "engagement_rate": engagement_rate,
            "recommendations": recommendations
        }
    
    def _extract_hook(self, caption: str) -> str:
        """Extract the first 3-second hook from caption"""
        if not caption:
            return "No caption available"
        
        # Take first sentence or first 50 characters
        sentences = re.split(r'[.!?]', caption)
        first_sentence = sentences[0].strip() if sentences else caption[:50]
        
        # Look for common hook patterns
        hook_patterns = [
            r"^(Did you know|Here's why|The secret to|What if I told you)",
            r"^(Stop|Wait|Hold up|Before you)",
            r"^(This is|This will|Here's how)",
        ]
        
        for pattern in hook_patterns:
            if re.search(pattern, first_sentence, re.IGNORECASE):
                return first_sentence
        
        return first_sentence[:100] + "..." if len(first_sentence) > 100 else first_sentence
    
    def _extract_cta(self, caption: str) -> str:
        """Extract call-to-action from caption"""
        if not caption:
            return "No CTA identified"
        
        # Common CTA patterns for aesthetics
        cta_patterns = [
            r"(book|schedule|call|contact).{0,20}(consultation|appointment)",
            r"(link in bio|swipe up|dm us|comment below)",
            r"(visit|follow|check out).{0,20}(profile|website|link)",
            r"(tag|share|save).{0,10}(this|post|friend)"
        ]
        
        caption_lower = caption.lower()
        for pattern in cta_patterns:
            match = re.search(pattern, caption_lower)
            if match:
                # Find the sentence containing the CTA
                sentences = re.split(r'[.!?]', caption)
                for sentence in sentences:
                    if pattern in sentence.lower():
                        return sentence.strip()
                return match.group(0)
        
        # Default CTA if none found
        return "No explicit CTA found"
    
    def _categorize_content(self, caption: str, hashtags: list) -> ContentCategory:
        """Categorize content based on caption and hashtags"""
        if not caption:
            return ContentCategory.GENERAL
        
        content_lower = caption.lower()
        hashtags_str = " ".join(hashtags).lower()
        combined_text = f"{content_lower} {hashtags_str}"
        
        # Category keywords
        category_keywords = {
            ContentCategory.PROCESS_DEMYSTIFIED: [
                "treatment", "procedure", "process", "step by step", "what happens",
                "during", "session", "appointment", "injection", "laser"
            ],
            ContentCategory.SCIENCE_EXPLAINED: [
                "how it works", "science", "collagen", "elastin", "hyaluronic", 
                "biostimulator", "mechanism", "technology", "infrared", "ultrasound"
            ],
            ContentCategory.TRANSFORMATION: [
                "before", "after", "results", "transformation", "improvement",
                "week", "month", "progress", "difference", "amazing"
            ],
            ContentCategory.MYTH_BUSTING: [
                "myth", "truth", "fact", "wrong", "misconception", "actually",
                "reality", "debunk", "false", "correct"
            ]
        }
        
        # Score each category
        category_scores = {}
        for category, keywords in category_keywords.items():
            score = sum(1 for keyword in keywords if keyword in combined_text)
            category_scores[category] = score
        
        # Return category with highest score
        if category_scores:
            best_category = max(category_scores, key=category_scores.get)
            if category_scores[best_category] > 0:
                return best_category
        
        return ContentCategory.GENERAL
    
    def _calculate_engagement_rate(self, post_data: CompetitorPostCreate) -> float:
        """Calculate engagement rate"""
        total_engagements = post_data.likes + post_data.comments + post_data.shares
        
        if post_data.views > 0:
            return (total_engagements / post_data.views) * 100
        elif post_data.likes > 0:
            # Estimate views based on likes (rough approximation)
            estimated_views = post_data.likes * 20  # Conservative estimate
            return (total_engagements / estimated_views) * 100
        
        return 0.0
    
    def _calculate_virality_score(self, post_data: CompetitorPostCreate, engagement_rate: float) -> float:
        """Calculate virality score (0-100)"""
        # Weighted scoring
        like_score = min(post_data.likes / 1000, 10) * 3  # Max 30 points
        comment_score = min(post_data.comments / 100, 10) * 2  # Max 20 points
        share_score = min(post_data.shares / 50, 10) * 3  # Max 30 points
        engagement_score = min(engagement_rate, 10) * 2  # Max 20 points
        
        total_score = like_score + comment_score + share_score + engagement_score
        return min(total_score, 100)
    
    def _calculate_relevance_score(self, post_data: CompetitorPostCreate) -> float:
        """Calculate relevance to MD Aesthetics services (0-100)"""
        if not post_data.caption:
            return 0.0
        
        # MD Aesthetics service keywords
        service_keywords = [
            "ultherapy", "radiesse", "skintyte", "duo-c-lift", "vivier",
            "botox", "filler", "injection", "facial", "skin", "anti-aging",
            "collagen", "wrinkles", "firm", "lift", "smooth", "aesthetic"
        ]
        
        caption_lower = post_data.caption.lower()
        hashtags_str = " ".join(post_data.hashtags).lower()
        combined_text = f"{caption_lower} {hashtags_str}"
        
        relevance_count = sum(1 for keyword in service_keywords if keyword in combined_text)
        relevance_score = min((relevance_count / len(service_keywords)) * 100, 100)
        
        return relevance_score
    
    def _extract_keywords(self, caption: str, hashtags: list) -> list:
        """Extract thematic keywords"""
        if not caption:
            return hashtags[:5]  # Return first 5 hashtags if no caption
        
        # Combine caption and hashtags
        text = f"{caption} {' '.join(hashtags)}"
        
        # Aesthetic industry keywords
        aesthetic_keywords = [
            "aesthetic", "beauty", "skincare", "antiaging", "facial", "treatment",
            "collagen", "wrinkles", "skin", "firm", "lift", "smooth", "glow",
            "injection", "filler", "botox", "laser", "ultrasound", "professional"
        ]
        
        found_keywords = []
        text_lower = text.lower()
        
        for keyword in aesthetic_keywords:
            if keyword in text_lower:
                found_keywords.append(keyword)
        
        # Add hashtags
        for hashtag in hashtags[:3]:  # Top 3 hashtags
            if hashtag not in found_keywords:
                found_keywords.append(hashtag.replace('#', ''))
        
        return found_keywords[:10]  # Return top 10 keywords
    
    def _score_educational_value(self, caption: str) -> float:
        """Score educational value of the content (0-10)"""
        if not caption:
            return 0.0
        
        educational_indicators = [
            "learn", "understand", "know", "science", "how", "why", "what",
            "explain", "education", "fact", "research", "study", "benefit"
        ]
        
        caption_lower = caption.lower()
        edu_count = sum(1 for indicator in educational_indicators if indicator in caption_lower)
        
        # Length bonus for detailed explanations
        length_bonus = min(len(caption) / 100, 2)  # Max 2 points for length
        
        return min(edu_count + length_bonus, 10)
    
    def _score_brand_alignment(self, caption: str, keywords: list) -> float:
        """Score alignment with MD Aesthetics brand (0-10)"""
        if not caption:
            return 0.0
        
        # Brand voice indicators
        brand_indicators = [
            "professional", "clinical", "physician", "medical", "results",
            "science", "technology", "expert", "consultation", "treatment"
        ]
        
        caption_lower = caption.lower()
        brand_count = sum(1 for indicator in brand_indicators if indicator in caption_lower)
        
        # Keyword alignment
        md_keywords = ["radiesse", "ultherapy", "skintyte", "vivier", "aesthetic"]
        keyword_alignment = sum(1 for keyword in keywords if any(mk in keyword.lower() for mk in md_keywords))
        
        total_score = (brand_count * 0.6) + (keyword_alignment * 0.4)
        return min(total_score, 10)
    
    def _generate_recommendations(self, analysis: PostAnalysis, post_data: CompetitorPostCreate) -> list:
        """Generate actionable recommendations"""
        recommendations = []
        
        # Hook recommendations
        if "no caption" in analysis.hook.lower():
            recommendations.append("Create a compelling opening hook to grab attention in the first 3 seconds")
        elif len(analysis.hook) > 100:
            recommendations.append("Shorten the hook for better impact - aim for under 50 characters")
        
        # CTA recommendations
        if "no cta" in analysis.cta.lower():
            recommendations.append("Add a clear call-to-action directing viewers to book a consultation")
        
        # Educational value
        if analysis.educational_value < 5:
            recommendations.append("Increase educational content to build trust and authority")
        
        # Brand alignment
        if analysis.brand_alignment < 5:
            recommendations.append("Better align content with MD Aesthetics services and brand voice")
        
        # Category-specific recommendations
        if analysis.content_category == ContentCategory.TRANSFORMATION:
            recommendations.append("Consider adding before/after transformations for MD Aesthetics services")
        elif analysis.content_category == ContentCategory.SCIENCE_EXPLAINED:
            recommendations.append("Adapt scientific explanation for MD Aesthetics treatments like Duo-C-Lift")
        
        return recommendations