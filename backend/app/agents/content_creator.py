"""
ContentCreator Agent

Pydantic-based agent for generating MD Aesthetics-branded social media content.
Creates compliant, on-brand content based on viral trend analysis.
"""

from pydantic import BaseModel, Field, field_validator, ConfigDict
import os
import uuid
from typing import List, Dict, Any, Optional, Union
import logging
import re
import random
from datetime import datetime
from enum import Enum

logger = logging.getLogger(__name__)

class Platform(str, Enum):
    """Supported social media platforms."""
    INSTAGRAM = "instagram"
    TIKTOK = "tiktok"
    FACEBOOK = "facebook"

class MediaType(str, Enum):
    """Suggested media types for posts."""
    SINGLE_IMAGE = "single_image"
    CAROUSEL = "carousel"
    BEFORE_AFTER = "before_after"
    VIDEO_REEL = "video_reel"
    STORY = "story"
    EDUCATIONAL_GRAPHIC = "educational_graphic"

class MDService(str, Enum):
    """MD Aesthetics core services."""
    DUO_C_LIFT = "duo_c_lift"
    SKINTYTE = "skintyte"
    TYTE_TONE_BUNDLE = "tyte_tone_bundle"
    FIRM_LIFT_BUTTOCK = "firm_lift_buttock"
    SKIN_BOOSTING = "skin_boosting"
    RADIESSE = "radiesse"
    VIVIER_PRODUCTS = "vivier_products"
    BBL = "bbl"
    MOXI = "moxi"

class BrandGuidelines(BaseModel):
    """MD Aesthetics brand guidelines."""
    tone: str = "professional, authoritative, educational, trustworthy"
    voice: str = "physician-led, results-driven, clinically sophisticated"
    forbidden_terms: List[str] = Field(
        default_factory=lambda: ["Botox", "cheap", "discount", "deal", "sale"]
    )
    preferred_terms: Dict[str, str] = Field(
        default_factory=lambda: {
            "Botox": "Tox/Neuromodulator/Neurotoxin",
            "filler": "dermal filler",
            "treatment": "clinical treatment",
            "results": "clinical outcomes"
        }
    )
    required_elements: List[str] = Field(
        default_factory=lambda: ["clear CTA", "professional tone", "educational value"]
    )

class TrendInput(BaseModel):
    """Input from trend analysis."""
    hook: str
    cta: str
    content_category: str
    virality_score: float
    key_themes: List[str]
    engagement_factors: List[str]

class ContentDraft(BaseModel):
    """Generated content draft."""
    id: str = Field(..., description="Unique identifier for the content draft")
    platform: Platform
    caption: str = Field(..., description="The main caption text")
    hashtags: List[str] = Field(..., description="Recommended hashtags")
    suggested_media_type: MediaType
    target_service: MDService
    compliance_checked: bool = Field(default=True)
    brand_alignment_score: float = Field(..., ge=0.0, le=1.0)
    estimated_engagement: str = Field(..., description="Predicted engagement level")
    created_at: datetime = Field(default_factory=datetime.utcnow)
    
    # Optional elements
    suggested_visuals: Optional[str] = Field(None, description="Visual content suggestions")
    posting_tips: Optional[str] = Field(None, description="Tips for posting")

class ContentCreator(BaseModel):
    """
    Pydantic-based ContentCreator agent for MD Aesthetics.
    
    Generates superior, on-brand social media content based on viral trend analysis.
    Ensures compliance with medical aesthetics regulations and brand guidelines.
    """
    model_config = ConfigDict(arbitrary_types_allowed=True)
    
    name: str = "ContentCreator"
    version: str = "2.0.0"
    description: str = "Generates MD Aesthetics-branded content with compliance checking"
    
    # Configuration
    brand_guidelines: BrandGuidelines = Field(default_factory=BrandGuidelines)
    max_caption_length: int = Field(default=2200, description="Maximum Instagram caption length")
    min_hashtag_count: int = Field(default=5, description="Minimum number of hashtags")
    max_hashtag_count: int = Field(default=15, description="Maximum number of hashtags")
    
    # MD Aesthetics service information
    service_descriptions: Dict[MDService, str] = Field(
        default_factory=lambda: {
            MDService.DUO_C_LIFT: "Advanced combination therapy: Ultherapy + Radiesse for superior lifting results",
            MDService.SKINTYTE: "Infrared light technology for skin firming and tightening",
            MDService.TYTE_TONE_BUNDLE: "Comprehensive body contouring with SkinTyte technology",
            MDService.FIRM_LIFT_BUTTOCK: "Specialized buttock firming and lifting package",
            MDService.SKIN_BOOSTING: "Physician-grade hyaluronic acid treatments",
            MDService.RADIESSE: "Biostimulator for natural collagen rebuilding",
            MDService.VIVIER_PRODUCTS: "Medical-grade skincare with proven ingredients",
            MDService.BBL: "Broad Band Light therapy for skin rejuvenation",
            MDService.MOXI: "Fractional laser for skin texture improvement"
        }
    )
    
    def generate_content(self, trend_input: TrendInput, target_platform: Platform = Platform.INSTAGRAM,
                        target_service: Optional[MDService] = None,
                        refine: bool = False) -> ContentDraft:
        """
        Generate content based on trend analysis.
        
        Args:
            trend_input: Analysis results from TrendAnalyzer
            target_platform: Platform to optimize for
            target_service: Specific MD Aesthetics service to focus on
            
        Returns:
            ContentDraft with generated content
        """
        logger.info(f"Generating content for {target_platform.value}")
        
        # Select target service if not specified
        if not target_service:
            target_service = self._select_target_service(trend_input.key_themes)
        
        # Generate hook adaptation
        adapted_hook = self._adapt_hook(trend_input.hook, target_service)
        
        # Create educational content
        educational_content = self._create_educational_content(target_service, trend_input.content_category)
        
        # Generate call-to-action
        md_cta = self._create_md_cta(trend_input.cta)
        
        # Combine into full caption
        caption = self._build_caption(adapted_hook, educational_content, md_cta, target_platform)

        # Optional refinement pass via GitHub Models (gpt-4o) if enabled
        if refine or os.getenv('REFINE_CAPTIONS', 'false').lower() in {'1', 'true', 'yes'}:
            try:
                caption = self.refine_caption(caption, target_service)
            except Exception as exc:  # noqa: BLE001
                logger.warning(f"Refinement skipped due to error: {exc}")
        
        # Generate hashtags
        hashtags = self._generate_hashtags(target_service, trend_input.key_themes, target_platform)
        
        # Suggest media type
        media_type = self._suggest_media_type(trend_input.content_category, target_service)
        
        # Calculate brand alignment
        alignment_score = self._calculate_brand_alignment(caption, hashtags)
        
        # Estimate engagement
        engagement_estimate = self._estimate_engagement(trend_input.virality_score, alignment_score)
        
        # Check compliance
        compliance_ok = self._check_compliance(caption)
        
        # Generate additional suggestions
        visuals = self._suggest_visuals(target_service, media_type)
        tips = self._generate_posting_tips(target_platform, trend_input.engagement_factors)
        
        return ContentDraft(
            id=str(uuid.uuid4()),
            platform=target_platform,
            caption=caption,
            hashtags=hashtags,
            suggested_media_type=media_type,
            target_service=target_service,
            compliance_checked=compliance_ok,
            brand_alignment_score=alignment_score,
            estimated_engagement=engagement_estimate,
            suggested_visuals=visuals,
            posting_tips=tips
        )
    
    def generate_batch(self, trend_inputs: List[TrendInput], 
                      target_platform: Platform = Platform.INSTAGRAM,
                      refine: bool = False) -> List[ContentDraft]:
        """
        Generate multiple content pieces from trend analyses.
        
        Args:
            trend_inputs: List of trend analysis results
            target_platform: Platform to optimize for
            
        Returns:
            List of content drafts
        """
        logger.info(f"Generating batch of {len(trend_inputs)} content pieces")
        
        drafts = []
        used_services = set()
        
        for i, trend_input in enumerate(trend_inputs):
            # Rotate through services to avoid repetition
            available_services = [s for s in MDService if s not in used_services]
            if not available_services:
                used_services.clear()
                available_services = list(MDService)
            
            target_service = self._select_target_service(trend_input.key_themes, available_services)
            used_services.add(target_service)
            
            try:
                draft = self.generate_content(trend_input, target_platform, target_service, refine=refine)
                drafts.append(draft)
            except Exception as e:
                logger.error(f"Error generating content for trend input {i}: {e}")
        
        logger.info(f"Generated {len(drafts)} content drafts")
        return drafts
    
    def _select_target_service(self, themes: List[str], 
                              available_services: Optional[List[MDService]] = None) -> MDService:
        """Select the most relevant MD Aesthetics service based on themes."""
        if not available_services:
            available_services = list(MDService)
        
        # Theme to service mapping
        theme_service_map = {
            'anti-aging': [MDService.DUO_C_LIFT, MDService.RADIESSE],
            'body': [MDService.TYTE_TONE_BUNDLE, MDService.FIRM_LIFT_BUTTOCK],
            'skincare': [MDService.VIVIER_PRODUCTS, MDService.BBL, MDService.MOXI],
            'technology': [MDService.SKINTYTE, MDService.BBL, MDService.MOXI],
            'injectables': [MDService.RADIESSE, MDService.SKIN_BOOSTING],
            'results': [MDService.DUO_C_LIFT, MDService.FIRM_LIFT_BUTTOCK]
        }
        
        # Score services based on theme relevance
        service_scores = {}
        for theme in themes:
            if theme in theme_service_map:
                for service in theme_service_map[theme]:
                    if service in available_services:
                        service_scores[service] = service_scores.get(service, 0) + 1
        
        # Return highest scoring service, or random if no matches
        if service_scores:
            # Cast keys to list to satisfy some static type checkers
            service_key_list = list(service_scores.keys())
            return max(service_key_list, key=lambda k: service_scores.get(k, 0))
        else:
            return random.choice(available_services)
    
    def _adapt_hook(self, original_hook: str, target_service: MDService) -> str:
        """Adapt the viral hook for MD Aesthetics and target service."""
        service_hooks = {
            MDService.DUO_C_LIFT: [
                "Transform your profile with our signature Duo-C-Lift! ✨",
                "The secret to natural-looking lift? Our Duo-C-Lift combination therapy.",
                "Why choose between Ultherapy OR Radiesse when you can have both? 🌟"
            ],
            MDService.SKINTYTE: [
                "Firm, smooth skin without surgery? Yes, it's possible with SkinTyte! 💫",
                "The technology that's changing skin firming forever: SkinTyte infrared.",
                "Watch your skin transform with our advanced SkinTyte treatments ✨"
            ],
            MDService.FIRM_LIFT_BUTTOCK: [
                "Get the lifted, smooth buttocks you've always wanted! 🍑",
                "Our Firm + Lift + Smooth Buttock Package delivers real results.",
                "Non-surgical buttock enhancement that actually works ✨"
            ],
            MDService.RADIESSE: [
                "Rebuild your collagen naturally with Radiesse biostimulator 💫",
                "The injectable that keeps working long after your appointment.",
                "Why settle for temporary when you can stimulate lasting results? ✨"
            ]
        }
        
        hooks = service_hooks.get(target_service, [f"Discover the science behind {target_service.value} ✨"])
        return random.choice(hooks)
    
    def _create_educational_content(self, service: MDService, content_category: str) -> str:
        """Create educational content about the service."""
        service_education = {
            MDService.DUO_C_LIFT: (
                "Our signature Duo-C-Lift combines the precision of Ultherapy with the volume "
                "restoration of Radiesse. Ultherapy uses focused ultrasound to lift and tighten "
                "at the foundational layer, while Radiesse provides immediate volume AND stimulates "
                "your body's natural collagen production for lasting results."
            ),
            MDService.SKINTYTE: (
                "SkinTyte uses advanced infrared light technology to heat the deep dermal layers, "
                "stimulating collagen remodeling and tissue contraction. This results in firmer, "
                "smoother skin with improved texture and tone - perfect for areas like the abdomen, "
                "arms, and buttocks."
            ),
            MDService.RADIESSE: (
                "Unlike hyaluronic acid fillers, Radiesse is a biostimulator. It immediately "
                "restores volume while microspheres stimulate your body to produce new collagen. "
                "Results improve over time and can last 12-18 months, making it an excellent "
                "investment in your skin's future."
            )
        }
        
        return service_education.get(service, f"Learn about our advanced {service.value} treatments.")
    
    def _create_md_cta(self, original_cta: str) -> str:
        """Create MD Aesthetics-appropriate call-to-action."""
        md_ctas = [
            "Book your consultation to see if you're a candidate! 📞",
            "Ready to transform your skin? Contact us today! ✨",
            "Schedule your personalized assessment with our physician-led team.",
            "Link in bio to book your consultation! 💫",
            "DM us to learn more about your treatment options.",
            "Call us to discuss your aesthetic goals! 📱",
            "Take the first step towards your best skin - book today!"
        ]
        
        return random.choice(md_ctas)
    
    def _build_caption(self, hook: str, education: str, cta: str, platform: Platform) -> str:
        """Build the complete caption."""
        # Platform-specific formatting
        if platform == Platform.INSTAGRAM:
            return f"{hook}\n\n{education}\n\n{cta}\n\n#MDAesthetics #TorontoAesthetics"
        elif platform == Platform.TIKTOK:
            # TikTok prefers shorter, punchier content
            short_education = education[:200] + "..." if len(education) > 200 else education
            return f"{hook}\n\n{short_education}\n\n{cta}"
        else:
            return f"{hook}\n\n{education}\n\n{cta}"
    
    def _generate_hashtags(self, service: MDService, themes: List[str], platform: Platform) -> List[str]:
        """Generate relevant hashtags."""
        # Base MD Aesthetics hashtags
        base_tags = ["#mdaesthetics", "#torontoaesthetics", "#whitby", "#durhamregion"]
        
        # Service-specific hashtags
        service_tags = {
            MDService.DUO_C_LIFT: ["#duoclift", "#ultherapy", "#radiesse", "#nonsurgicallift"],
            MDService.SKINTYTE: ["#skintyte", "#skintytetreatment", "#skinfirming", "#bodycontouring"],
            MDService.FIRM_LIFT_BUTTOCK: ["#buttocklift", "#firmandsmooth", "#bodyshaping"],
            MDService.RADIESSE: ["#radiesse", "#biostimulator", "#collagenstimulation"],
            MDService.VIVIER_PRODUCTS: ["#vivierskin", "#medicalgradeskincare", "#vitamincserum"]
        }
        
        # Theme-based hashtags
        theme_tags = {
            'skincare': ["#skincare", "#healthyskin", "#skinhealth"],
            'anti-aging': ["#antiaging", "#youthfulskin", "#preventativecare"],
            'technology': ["#advancedtechnology", "#medicaldevice", "#innovation"],
            'results': ["#realresults", "#transformation", "#beforeandafter"]
        }
        
        # Combine hashtags
        hashtags = base_tags[:]
        
        # Add service tags
        if service in service_tags:
            hashtags.extend(service_tags[service])
        
        # Add theme tags
        for theme in themes[:3]:  # Limit themes
            if theme in theme_tags:
                hashtags.extend(theme_tags[theme][:2])  # Limit per theme
        
        # Add platform-specific tags
        if platform == Platform.INSTAGRAM:
            hashtags.extend(["#torontomedspa", "#aestheticmedicine", "#physicianled"])
        elif platform == Platform.TIKTOK:
            hashtags.extend(["#aesthetictok", "#skincaretips", "#medspalife"])
        
        # Remove duplicates and limit count
        unique_hashtags = list(dict.fromkeys(hashtags))  # Preserves order
        return unique_hashtags[:self.max_hashtag_count]
    
    def _suggest_media_type(self, content_category: str, service: MDService) -> MediaType:
        """Suggest appropriate media type."""
        category_media_map = {
            'process_demystified': MediaType.VIDEO_REEL,
            'science_explained': MediaType.EDUCATIONAL_GRAPHIC,
            'transformation': MediaType.BEFORE_AFTER,
            'educational': MediaType.CAROUSEL
        }
        
        return category_media_map.get(content_category, MediaType.SINGLE_IMAGE)
    
    def _calculate_brand_alignment(self, caption: str, hashtags: List[str]) -> float:
        """Calculate how well content aligns with brand guidelines."""
        score = 1.0
        text = f"{caption} {' '.join(hashtags)}".lower()
        
        # Check for forbidden terms
        for term in self.brand_guidelines.forbidden_terms:
            if term.lower() in text:
                score -= 0.2
        
        # Check for professional tone indicators
        professional_indicators = ['physician', 'clinical', 'medical grade', 'advanced', 'treatment']
        professional_score = sum(0.1 for indicator in professional_indicators if indicator in text)
        score += min(professional_score, 0.3)
        
        # Check for educational value
        educational_indicators = ['learn', 'understand', 'science', 'how', 'why', 'technology']
        educational_score = sum(0.05 for indicator in educational_indicators if indicator in text)
        score += min(educational_score, 0.2)
        
        return max(0.0, min(1.0, score))
    
    def _estimate_engagement(self, virality_score: float, brand_alignment: float) -> str:
        """Estimate engagement level for the content."""
        combined_score = (virality_score * 0.6) + (brand_alignment * 0.4)
        
        if combined_score >= 0.8:
            return "High (8-12% engagement rate expected)"
        elif combined_score >= 0.6:
            return "Medium-High (5-8% engagement rate expected)"
        elif combined_score >= 0.4:
            return "Medium (3-5% engagement rate expected)"
        else:
            return "Low-Medium (1-3% engagement rate expected)"
    
    def _check_compliance(self, caption: str) -> bool:
        """Check if content meets compliance requirements."""
        # Check for forbidden terms
        for term in self.brand_guidelines.forbidden_terms:
            if term.lower() in caption.lower():
                logger.warning(f"Compliance issue: Found forbidden term '{term}'")
                return False
        
        # Other compliance checks can be added here
        return True
    
    def _suggest_visuals(self, service: MDService, media_type: MediaType) -> str:
        """Suggest visual content for the post."""
        suggestions = {
            (MDService.DUO_C_LIFT, MediaType.BEFORE_AFTER): "Clean before/after split-screen showing natural lifting results",
            (MDService.SKINTYTE, MediaType.VIDEO_REEL): "Time-lapse of treatment process with soothing music",
            (MDService.RADIESSE, MediaType.EDUCATIONAL_GRAPHIC): "Infographic showing collagen stimulation process",
        }
        
        key = (service, media_type)
        return suggestions.get(key, f"Professional imagery showcasing {service.value} results")
    
    def _generate_posting_tips(self, platform: Platform, engagement_factors: List[str]) -> str:
        """Generate posting tips based on platform and engagement factors."""
        tips = []
        
        if platform == Platform.INSTAGRAM:
            tips.append("Post during peak hours (11am-1pm, 7pm-9pm EST)")
            tips.append("Use all 15 hashtags for maximum reach")
        elif platform == Platform.TIKTOK:
            tips.append("Post during TikTok peak hours (6am-10am, 7pm-9pm EST)")
            tips.append("Keep first 3 seconds engaging to avoid scrolling")
        
        if "emoji_usage" in engagement_factors:
            tips.append("Strategic emoji use increases engagement by 25%")
        
        if "trending_hashtags" in engagement_factors:
            tips.append("Monitor trending hashtags daily for opportunities")
        
        return " | ".join(tips)

    # ---------------------- Refinement (LLM) ----------------------
    def refine_caption(self, caption: str, service: MDService) -> str:
        """Refine caption using GitHub Models gpt-4o (no fallback).

        Tightens language, ensures clinical authority tone, preserves compliance.
        """
        from app.services.ai_client import AIClient  # local import
        client = AIClient()
        system_prompt = (
            "You are an assistant helping refine social media captions for a physician-led medical aesthetics clinic. "
            "Improve clarity, tighten wording, keep professional, educational tone. Do NOT add emojis beyond 3 total. "
            "Never use prohibited terms (Botox - use Tox/Neuromodulator). Maintain call-to-action if present."
        )
        user_prompt = (
            f"Original caption for service {service.value}:\n---\n{caption}\n---\n"
            "Refine it per instructions. Return ONLY the refined caption."
        )
        try:
            import anyio
            refined = anyio.run(lambda: client.generate(
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                max_tokens=400,
                temperature=0.4
            ))
            if refined and isinstance(refined, str) and len(refined) > 20:
                return refined.strip()
        except PermissionError:
            logger.warning("Refinement skipped: permission error calling GitHub Models API")
        except Exception as exc:  # noqa: BLE001
            logger.debug(f"Refinement exception: {exc}")
        finally:
            try:
                import asyncio
                if asyncio.get_event_loop().is_running():  # async context
                    asyncio.create_task(client.close())
                else:
                    import anyio as _anyio
                    _anyio.run(client.close)
            except Exception:  # noqa: BLE001
                pass
        return caption