from .base_agent import BaseAgent, AgentState
from ..models.schemas import (
    ContentCreationRequest, GeneratedContentCreate, GeneratedContentResponse,
    PostAnalysis, Platform, ContentCategory
)
from typing import Dict, Any, List
import httpx
import json
import re
import random
from ..core.config import get_settings

class ContentCreatorAgent(BaseAgent):
    """Pydantic-based content creation agent for MD Aesthetics"""
    
    def __init__(self):
        super().__init__(
            name="ContentCreator",
            description="Creates superior MD Aesthetics content based on viral trend analysis"
        )
        self.settings = get_settings()
        self.md_services = {
            "Duo-C-Lift": "Our signature combination of Ultherapy + Radiesse for ultimate lifting and firming",
            "SkinTyte": "Advanced infrared technology for skin tightening and firming",
            "Radiesse": "Biostimulator that rebuilds natural collagen for long-lasting results",
            "Vivier": "Medical-grade skincare with scientifically proven ingredients",
            "BBL": "BroadBand Light technology for skin rejuvenation",
            "Moxi": "Fractional laser for skin texture and tone improvement"
        }
        
        # Compliance rules
        self.forbidden_words = ["botox"]
        self.approved_alternatives = ["Tox", "Neuromodulator", "Neurotoxin"]
    
    async def _execute_impl(self, request: ContentCreationRequest, state: AgentState) -> Dict[str, Any]:
        """Generate superior MD Aesthetics content based on trend analysis"""
        
        analysis = request.trend_analysis
        target_services = request.target_services
        tone = request.tone
        
        # Generate main content
        main_content = await self._generate_content(analysis, target_services, tone, state)
        
        # Generate alternative versions
        alternative_versions = await self._generate_alternatives(analysis, target_services, tone, 2)
        
        # Calculate brand voice score
        brand_voice_score = self._calculate_brand_voice_score(main_content)
        
        return {
            "generated_content": main_content.dict(),
            "alternative_versions": [alt.dict() for alt in alternative_versions],
            "brand_voice_score": brand_voice_score
        }
    
    async def _generate_content(self, analysis: PostAnalysis, target_services: List[str], 
                              tone: str, state: AgentState) -> GeneratedContentCreate:
        """Generate main content piece"""
        
        # Select primary service to focus on
        primary_service = self._select_primary_service(analysis, target_services)
        
        # Create enhanced hook
        hook = self._create_enhanced_hook(analysis.hook, primary_service, tone)
        
        # Generate educational content
        educational_content = self._generate_educational_content(
            primary_service, analysis.content_category, tone
        )
        
        # Create call-to-action
        cta = self._create_md_cta(primary_service)
        
        # Combine into caption
        caption = f"{hook}\n\n{educational_content}\n\n{cta}"
        
        # Ensure compliance
        caption = self._ensure_compliance(caption)
        
        # Generate hashtags
        hashtags = self._generate_hashtags(primary_service, analysis.thematic_keywords)
        
        # Determine suggested media type
        media_type = self._suggest_media_type(analysis.content_category)
        
        return GeneratedContentCreate(
            platform=Platform.INSTAGRAM,  # Default to Instagram
            caption=caption,
            hashtags=hashtags,
            suggested_media_type=media_type
        )
    
    def _select_primary_service(self, analysis: PostAnalysis, target_services: List[str]) -> str:
        """Select the most relevant MD Aesthetics service"""
        
        # Map content categories to recommended services
        category_service_mapping = {
            ContentCategory.PROCESS_DEMYSTIFIED: ["SkinTyte", "Duo-C-Lift"],
            ContentCategory.SCIENCE_EXPLAINED: ["Radiesse", "Vivier"],
            ContentCategory.TRANSFORMATION: ["Duo-C-Lift", "SkinTyte"],
            ContentCategory.MYTH_BUSTING: ["Radiesse", "Vivier"]
        }
        
        # Get recommended services for this category
        recommended = category_service_mapping.get(analysis.content_category, target_services)
        
        # Find intersection with target services
        available_services = [s for s in recommended if s in target_services]
        
        if available_services:
            # Prioritize based on keywords
            for service in available_services:
                if any(keyword in service.lower() for keyword in analysis.thematic_keywords):
                    return service
            return available_services[0]
        
        # Fallback to first target service
        return target_services[0] if target_services else "Duo-C-Lift"
    
    def _create_enhanced_hook(self, original_hook: str, service: str, tone: str) -> str:
        """Create an enhanced hook focused on MD Aesthetics service"""
        
        # Hook templates based on tone
        hooks = {
            "educational": [
                f"Here's the science behind {service} that most people don't know:",
                f"What makes {service} different from other treatments?",
                f"The truth about {service} results:",
                f"Why {service} is changing the game in aesthetic medicine:"
            ],
            "conversational": [
                f"Can we talk about {service} for a second?",
                f"You guys keep asking about {service}, so here's the tea:",
                f"Real talk about {service}:",
                f"Let's break down {service} together:"
            ],
            "authoritative": [
                f"As a medical practice, here's what you need to know about {service}:",
                f"The clinical evidence for {service} speaks for itself:",
                f"After thousands of {service} treatments, here's what we've learned:",
                f"From a physician's perspective, {service} delivers results because:"
            ]
        }
        
        hook_options = hooks.get(tone, hooks["educational"])
        return random.choice(hook_options)
    
    def _generate_educational_content(self, service: str, category: ContentCategory, tone: str) -> str:
        """Generate educational content about the service"""
        
        service_info = self.md_services.get(service, "Advanced aesthetic treatment")
        
        if category == ContentCategory.PROCESS_DEMYSTIFIED:
            return self._create_process_content(service, service_info, tone)
        elif category == ContentCategory.SCIENCE_EXPLAINED:
            return self._create_science_content(service, service_info, tone)
        elif category == ContentCategory.TRANSFORMATION:
            return self._create_transformation_content(service, service_info, tone)
        elif category == ContentCategory.MYTH_BUSTING:
            return self._create_mythbusting_content(service, service_info, tone)
        else:
            return self._create_general_content(service, service_info, tone)
    
    def _create_process_content(self, service: str, info: str, tone: str) -> str:
        """Create process-focused content"""
        if "SkinTyte" in service:
            return ("SkinTyte uses advanced infrared light to heat the deeper layers of your skin, "
                   "stimulating collagen production for natural firming. The treatment is comfortable "
                   "with no downtime - you can literally come in during your lunch break! "
                   "Most clients see gradual improvement over 2-3 months as new collagen builds.")
        
        elif "Duo-C-Lift" in service:
            return ("Our Duo-C-Lift combines two powerhouse treatments: Ultherapy for deep tissue "
                   "lifting and Radiesse for volume and collagen stimulation. First, we map your "
                   "treatment areas, then deliver precise energy to lift and tighten from within. "
                   "The entire process takes about 90 minutes with results lasting 12-18 months.")
        
        elif "Radiesse" in service:
            return ("Unlike temporary fillers, Radiesse is a biostimulator that works in two phases: "
                   "immediate volume correction and long-term collagen building. We inject the "
                   "calcium-based microspheres which provide instant lift, then over 3-6 months, "
                   "your body builds new collagen around them for natural, lasting results.")
        
        return f"{info}. Our trained medical team ensures every step is performed with precision and care."
    
    def _create_science_content(self, service: str, info: str, tone: str) -> str:
        """Create science-focused content"""
        if "Radiesse" in service:
            return ("Here's the fascinating science: Radiesse contains calcium hydroxylapatite - "
                   "the same mineral found in your bones and teeth. When injected, it creates a "
                   "scaffold that signals your fibroblasts to produce Type I collagen. This isn't "
                   "just temporary volume - it's actual tissue regeneration that can last 2+ years.")
        
        elif "SkinTyte" in service:
            return ("SkinTyte technology delivers precise infrared energy at 1440nm wavelength - "
                   "the optimal depth for heating collagen fibers. This controlled heating causes "
                   "immediate collagen contraction AND triggers neocollagenesis. The result? "
                   "Skin that's visibly firmer and tighter, naturally.")
        
        elif "Vivier" in service:
            return ("Medical-grade skincare isn't just marketing - it's about ingredient penetration "
                   "and concentration. Vivier's pharmaceutical-grade Vitamin C (15-20%) penetrates "
                   "deeper than cosmetic products, while their retinol complex delivers results "
                   "without the irritation of traditional retinoids.")
        
        return f"The science behind {service}: {info}"
    
    def _create_transformation_content(self, service: str, info: str, tone: str) -> str:
        """Create transformation-focused content"""
        return (f"Real transformation isn't just about looking different - it's about looking like "
               f"the best version of yourself. {service} works by targeting the root cause of "
               f"aging, not just covering symptoms. Our clients consistently tell us they feel "
               f"more confident and energized after their treatments. Results speak louder than promises.")
    
    def _create_mythbusting_content(self, service: str, info: str, tone: str) -> str:
        """Create myth-busting content"""
        myths = {
            "Radiesse": "MYTH: All fillers are the same. TRUTH: Radiesse actually builds your own collagen.",
            "SkinTyte": "MYTH: Skin tightening requires surgery. TRUTH: Advanced technology can firm skin non-invasively.",
            "Vivier": "MYTH: All skincare is basically the same. TRUTH: Medical-grade ingredients work differently.",
            "Duo-C-Lift": "MYTH: You need surgery for real lifting. TRUTH: Combined energy treatments can lift naturally."
        }
        
        myth_text = myths.get(service, f"MYTH: {service} is just a trend. TRUTH: It's backed by clinical evidence.")
        return f"{myth_text} As a physician-led practice, we only offer treatments with proven results."
    
    def _create_general_content(self, service: str, info: str, tone: str) -> str:
        """Create general content"""
        return (f"What sets MD Aesthetics apart? We don't follow trends - we follow science. "
               f"{service} is part of our curated selection of treatments that deliver real, "
               f"measurable results. Every treatment is performed by our trained medical team "
               f"with your safety and satisfaction as our top priorities.")
    
    def _create_md_cta(self, service: str) -> str:
        """Create MD Aesthetics specific call-to-action"""
        ctas = [
            f"Ready to learn if {service} is right for you? Book your consultation - link in bio 📲",
            f"Want to see {service} results for yourself? Schedule your complimentary consultation 💫",
            f"Curious about {service}? Our medical team is here to answer all your questions 💬",
            f"Book your {service} consultation today and take the first step toward your best skin ✨"
        ]
        
        return random.choice(ctas)
    
    def _generate_hashtags(self, service: str, keywords: List[str]) -> List[str]:
        """Generate MD Aesthetics branded hashtags"""
        
        # Core MD Aesthetics hashtags
        core_hashtags = ["#mdaesthetics", "#physicianled", "#medicalgrade"]
        
        # Service-specific hashtags
        service_hashtags = {
            "Duo-C-Lift": ["#duoclift", "#ultherapy", "#radiesse", "#nonsurgicallift"],
            "SkinTyte": ["#skintyte", "#skinfirming", "#nonsurgical", "#collagenboosting"],
            "Radiesse": ["#radiesse", "#biostimulator", "#collagenbuilding", "#naturalresults"],
            "Vivier": ["#vivierskin", "#medicalgradeskincare", "#vitamins", "#clinicalresults"]
        }
        
        # Location hashtags
        location_hashtags = ["#torontoaesthetics", "#whitbymedspa", "#durhamregion"]
        
        # Treatment type hashtags
        treatment_hashtags = ["#aestheticmedicine", "#skincare", "#antiaging", "#confidence"]
        
        # Combine all hashtags
        hashtags = core_hashtags.copy()
        hashtags.extend(service_hashtags.get(service, []))
        hashtags.extend(location_hashtags)
        hashtags.extend(treatment_hashtags)
        
        # Add keyword-based hashtags
        for keyword in keywords[:3]:
            hashtag = f"#{keyword.lower().replace(' ', '').replace('#', '')}"
            if hashtag not in hashtags and len(hashtag) > 1:
                hashtags.append(hashtag)
        
        # Ensure we have 8-12 hashtags
        return hashtags[:12]
    
    def _suggest_media_type(self, category: ContentCategory) -> str:
        """Suggest appropriate media type based on content category"""
        media_suggestions = {
            ContentCategory.PROCESS_DEMYSTIFIED: "Process video or carousel showing treatment steps",
            ContentCategory.SCIENCE_EXPLAINED: "Educational graphic or animation explaining the science",
            ContentCategory.TRANSFORMATION: "Before/after photos or transformation video",
            ContentCategory.MYTH_BUSTING: "Text-overlay video or carousel debunking myths",
            ContentCategory.GENERAL: "Professional photo with text overlay"
        }
        
        return media_suggestions.get(category, "Professional photo with informative caption")
    
    def _ensure_compliance(self, caption: str) -> str:
        """Ensure content complies with MD Aesthetics guidelines"""
        
        # Replace forbidden words
        caption_lower = caption.lower()
        for forbidden in self.forbidden_words:
            if forbidden in caption_lower:
                replacement = random.choice(self.approved_alternatives)
                # Replace while preserving case
                caption = re.sub(f"\\b{forbidden}\\b", replacement, caption, flags=re.IGNORECASE)
        
        # Ensure professional tone
        unprofessional_words = ["cheap", "deal", "discount", "sale"]
        for word in unprofessional_words:
            caption = re.sub(f"\\b{word}\\b", "value", caption, flags=re.IGNORECASE)
        
        return caption
    
    def _calculate_brand_voice_score(self, content: GeneratedContentCreate) -> float:
        """Calculate how well content aligns with MD Aesthetics brand voice"""
        
        caption = content.caption.lower()
        
        # Positive brand indicators
        positive_indicators = [
            "physician", "medical", "clinical", "science", "professional", "results",
            "consultation", "treatment", "advanced", "technology", "evidence"
        ]
        
        # Negative brand indicators
        negative_indicators = [
            "cheap", "deal", "discount", "quick fix", "miracle", "instant"
        ]
        
        positive_score = sum(2 for indicator in positive_indicators if indicator in caption)
        negative_score = sum(1 for indicator in negative_indicators if indicator in caption)
        
        # Hashtag quality score
        hashtag_score = 0
        md_hashtags = ["#mdaesthetics", "#physicianled", "#medicalgrade"]
        for hashtag in content.hashtags:
            if hashtag.lower() in [h.lower() for h in md_hashtags]:
                hashtag_score += 1
        
        # Calculate final score (0-10)
        total_score = positive_score + hashtag_score - negative_score
        return min(max(total_score, 0), 10)
    
    async def _generate_alternatives(self, analysis: PostAnalysis, target_services: List[str], 
                                   tone: str, count: int) -> List[GeneratedContentCreate]:
        """Generate alternative content versions"""
        alternatives = []
        
        # Use different services for alternatives
        available_services = [s for s in target_services if s in self.md_services.keys()]
        
        for i in range(count):
            # Use different service if available
            if i < len(available_services) and i > 0:
                alt_service = available_services[i]
            else:
                alt_service = random.choice(available_services) if available_services else "Duo-C-Lift"
            
            # Use different tone approach
            alt_tones = ["educational", "conversational", "authoritative"]
            alt_tone = alt_tones[i % len(alt_tones)]
            
            alt_content = await self._generate_content(analysis, [alt_service], alt_tone, None)
            alternatives.append(alt_content)
        
        return alternatives