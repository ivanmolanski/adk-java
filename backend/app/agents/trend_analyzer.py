"""TrendAnalyzer Agent - analyzes viral social media posts for MD Aesthetics.

Clean rewrite with guaranteed 2-space indentation and consistent quotes.
"""
from __future__ import annotations

import logging
import re
from datetime import datetime
from enum import Enum
from typing import List, Optional

from pydantic import BaseModel, Field, field_validator, ConfigDict

logger = logging.getLogger(__name__)


class ContentCategory(str, Enum):
  """Content categorization for MD Aesthetics focus areas."""
  PROCESS_DEMYSTIFIED = 'process_demystified'
  SCIENCE_EXPLAINED = 'science_explained'
  TRANSFORMATION = 'transformation'
  MYTH_BUSTING = 'myth_busting'
  EDUCATIONAL = 'educational'
  BEHIND_SCENES = 'behind_scenes'


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
  model_config = ConfigDict(str_strip_whitespace=True)
  
  post_id: str
  hook: str = Field(..., description='The opening 3-second hook')
  cta: str = Field(..., description='Call-to-action identified')
  content_category: ContentCategory
  relevance_score: float = Field(
    ..., ge=0.0, le=1.0, description='Relevance to MD Aesthetics (0-1)'
  )
  virality_score: float = Field(
    ..., ge=0.0, le=1.0, description='Viral potential score (0-1)'
  )
  summary: str = Field(..., description='Brief analysis summary')
  key_themes: List[str] = Field(
    default_factory=list, description='Key themes identified'
  )
  engagement_factors: List[str] = Field(
    default_factory=list, description='Factors driving engagement'
  )
  compliance_notes: Optional[str] = Field(
    None, description='Compliance concerns if any'
  )
  analyzed_at: datetime = Field(default_factory=datetime.utcnow)

  @field_validator('relevance_score', 'virality_score')
  @classmethod
  def validate_scores(cls, value: float) -> float:
    """Validate scores are in range 0-1."""
    if not 0 <= value <= 1:
      raise ValueError('Score must be between 0 and 1')
    return value


class TrendAnalyzer(BaseModel):
  """Analyzes viral posts for MD Aesthetics competitive intelligence."""
  model_config = ConfigDict(arbitrary_types_allowed=True)

  name: str = 'TrendAnalyzer'
  version: str = '2.0.0'
  description: str = 'Analyzes viral content for MD Aesthetics competitive intelligence'

  min_engagement_threshold: float = Field(
    default=5.0, description='Minimum engagement rate to consider'
  )
  relevant_keywords: List[str] = Field(
    default_factory=lambda: [
      'aesthetic', 'skincare', 'treatment', 'facial', 'botox', 'filler',
      'ultherapy', 'radiesse', 'skintype', 'lift', 'smooth', 'firm',
      'vitamin c', 'medical grade', 'physician', 'consultation'
    ]
  )

  def analyze_post(self, post: ViralPostData) -> TrendAnalysisResult:
    """Analyze a single post and return structured heuristics."""
    logger.info('Analyzing post %s from %s', post.id, post.profile)
    hook = self._extract_hook(post.caption)
    cta = self._extract_cta(post.caption)
    category = self._categorize_content(post.caption)
    relevance_score = self._calculate_relevance_score(post)
    virality_score = self._calculate_virality_score(post)
    themes = self._extract_themes(post.caption, post.hashtags)
    engagement_factors = self._identify_engagement_factors(post)
    summary = self._generate_summary(post, category, relevance_score, virality_score)
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
    logger.info('Analyzing batch of %d posts', len(posts))
    results: List[TrendAnalysisResult] = []
    for post in posts:
      if post.engagement_rate < self.min_engagement_threshold:
        logger.debug('Skipping post %s - low engagement (%s%%)', post.id, post.engagement_rate)
        continue
      try:
        results.append(self.analyze_post(post))
      except (ValueError, RuntimeError) as exc:  # narrow typical processing errors
        logger.error('Error analyzing post %s: %s', post.id, exc)
    logger.info('Completed analysis of %d posts', len(results))
    return results

  # --------------------- Internal helpers ---------------------------------
  def _extract_hook(self, caption: str) -> str:
    sentences = re.split(r'[.!?]+', caption.strip())
    if sentences and sentences[0]:
      hook = sentences[0].strip()
      return hook[:100] + '...' if len(hook) > 100 else hook
    return caption[:50] + '...' if len(caption) > 50 else caption

  def _extract_cta(self, caption: str) -> str:
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
        start = max(0, match.start() - 20)
        end = min(len(caption), match.end() + 20)
        return caption[start:end].strip()
    action_words = ['book', 'call', 'visit', 'schedule', 'contact', 'dm']
    words = caption.lower().split()
    for i, _word in enumerate(words[-10:], start=len(words) - 10):
      if any(action in _word for action in action_words):
        return ' '.join(words[i:]).strip()
    return 'No clear CTA identified'

  def _categorize_content(self, caption: str) -> ContentCategory:
    caption_lower = caption.lower()
    if any(w in caption_lower for w in ['procedure', 'treatment', 'process', 'how', 'step']):
      return ContentCategory.PROCESS_DEMYSTIFIED
    if any(w in caption_lower for w in ['science', 'research', 'study', 'collagen', 'peptide']):
      return ContentCategory.SCIENCE_EXPLAINED
    if any(w in caption_lower for w in ['before', 'after', 'results', 'transformation']):
      return ContentCategory.TRANSFORMATION
    if any(w in caption_lower for w in ['myth', 'fact', 'truth', 'debunk', 'misconception']):
      return ContentCategory.MYTH_BUSTING
    return ContentCategory.EDUCATIONAL

  def _calculate_relevance_score(self, post: ViralPostData) -> float:
    text = f"{post.caption} {' '.join(post.hashtags)}".lower()
    keyword_matches = sum(1 for keyword in self.relevant_keywords if keyword in text)
    keyword_score = min(keyword_matches * 0.1, 0.5)
    platform_score = 0.1 if post.platform == 'instagram' else 0.05
    engagement_score = min(post.engagement_rate * 0.02, 0.3)
    competitor_profiles = ['_thelookaesthetics', 'subtle.enhancements', 'skinvitalityofficial']
    profile_score = 0.2 if any(c in post.profile.lower() for c in competitor_profiles) else 0.1
    total_score = keyword_score + platform_score + engagement_score + profile_score
    return min(total_score, 1.0)

  def _calculate_virality_score(self, post: ViralPostData) -> float:
    engagement_score = min(post.engagement_rate * 0.05, 0.5)
    likes_score = min(post.likes / 10000 * 0.2, 0.2)
    comments_score = min(post.comments / 100 * 0.2, 0.2)
    platform_multiplier = 1.2 if post.platform == 'tiktok' else 1.0
    total_score = (engagement_score + likes_score + comments_score) * platform_multiplier
    return min(total_score, 1.0)

  def _extract_themes(self, caption: str, hashtags: List[str]) -> List[str]:
    themes: List[str] = []
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
      if any(k in text for k in keywords):
        themes.append(theme)
    return themes[:5]

  def _identify_engagement_factors(self, post: ViralPostData) -> List[str]:
    factors: List[str] = []
    if post.engagement_rate > 10:
      factors.append('high_engagement_rate')
    if post.likes > 1000:
      factors.append('high_like_count')
    if post.comments > 50:
      factors.append('high_comment_count')
    if any(e in post.caption for e in ['✨', '💫', '🔥', '💖']):
      factors.append('emoji_usage')
    if any('trend' in tag.lower() for tag in post.hashtags):
      factors.append('trending_hashtags')
    return factors

  def _generate_summary(self, post: ViralPostData, category: ContentCategory, relevance: float, virality: float) -> str:  # noqa: D401,E501
    return (
      f"{category.value.replace('_', ' ').title()} content from {post.profile} with "
      f"{relevance:.1%} relevance and {virality:.1%} viral potential. "
      f"Engagement: {post.engagement_rate}% ({post.likes} likes, {post.comments} comments)"
    )

  def _check_compliance(self, caption: str) -> Optional[str]:
    issues: List[str] = []
    medical_claims = ['cure', 'heal', 'medical treatment', 'guaranteed results']
    for claim in medical_claims:
      if claim in caption.lower():
        issues.append(f"Contains medical claim: '{claim}'")
    if any(ch in caption for ch in ['$', '£', '€']) or 'price' in caption.lower():
      issues.append('Contains pricing information')
    return '; '.join(issues) if issues else None
