from .base_agent import BaseAgent, AgentState
from ..models.schemas import ComplianceCheckRequest, ComplianceCheckResponse, GeneratedContentCreate
from typing import Dict, Any, List
import re

class ComplianceAgent(BaseAgent):
    """Pydantic-based compliance checking agent for MD Aesthetics content"""
    
    def __init__(self):
        super().__init__(
            name="ComplianceAgent",
            description="Ensures content compliance with MD Aesthetics guidelines and regulations"
        )
        
        # Compliance rules
        self.forbidden_words = {
            "botox": ["Tox", "Neuromodulator", "Neurotoxin"],
            "cheap": ["affordable", "value"],
            "instant": ["gradual", "progressive"],
            "miracle": ["effective", "proven"],
            "guaranteed": ["expected", "typical"]
        }
        
        self.required_elements = [
            "call_to_action",
            "hashtags",
            "service_mention"
        ]
        
        self.brand_voice_requirements = [
            "professional_tone",
            "educational_content",
            "physician_led_messaging"
        ]
    
    async def _execute_impl(self, request: ComplianceCheckRequest, state: AgentState) -> Dict[str, Any]:
        """Check content for compliance with all guidelines"""
        
        content = request.content
        strict_mode = request.strict_mode
        
        # Perform all compliance checks
        word_compliance = self._check_forbidden_words(content.caption)
        structure_compliance = self._check_content_structure(content)
        brand_compliance = self._check_brand_voice(content)
        hashtag_compliance = self._check_hashtags(content.hashtags)
        
        # Collect all issues
        all_issues = []
        all_issues.extend(word_compliance["issues"])
        all_issues.extend(structure_compliance["issues"])
        all_issues.extend(brand_compliance["issues"])
        all_issues.extend(hashtag_compliance["issues"])
        
        # Generate suggestions
        suggestions = self._generate_suggestions(all_issues, content)
        
        # Create modified content if needed
        modified_content = None
        if all_issues and not strict_mode:
            modified_content = self._auto_fix_content(content, all_issues)
        
        # Determine if compliant
        is_compliant = len(all_issues) == 0
        
        return {
            "compliant": is_compliant,
            "issues": all_issues,
            "suggestions": suggestions,
            "modified_content": modified_content.dict() if modified_content else None,
            "compliance_details": {
                "word_compliance": word_compliance,
                "structure_compliance": structure_compliance,
                "brand_compliance": brand_compliance,
                "hashtag_compliance": hashtag_compliance
            }
        }
    
    def _check_forbidden_words(self, caption: str) -> Dict[str, Any]:
        """Check for forbidden words and suggest alternatives"""
        issues = []
        suggestions = []
        
        caption_lower = caption.lower()
        
        for forbidden, alternatives in self.forbidden_words.items():
            if forbidden in caption_lower:
                issues.append(f"Contains forbidden word: '{forbidden}'")
                suggestions.append(f"Replace '{forbidden}' with: {', '.join(alternatives)}")
        
        return {
            "compliant": len(issues) == 0,
            "issues": issues,
            "suggestions": suggestions
        }
    
    def _check_content_structure(self, content: GeneratedContentCreate) -> Dict[str, Any]:
        """Check for required content structure elements"""
        issues = []
        suggestions = []
        
        caption = content.caption
        
        # Check for call-to-action
        cta_patterns = [
            r"book|schedule|call|contact|visit|consultation|appointment",
            r"link in bio|dm us|comment|swipe",
            r"learn more|find out|discover"
        ]
        
        has_cta = any(re.search(pattern, caption, re.IGNORECASE) for pattern in cta_patterns)
        if not has_cta:
            issues.append("Missing clear call-to-action")
            suggestions.append("Add a call-to-action like 'Book your consultation - link in bio'")
        
        # Check for service mention
        md_services = ["duo-c-lift", "skintyte", "radiesse", "vivier", "ultherapy", "bbl", "moxi"]
        has_service = any(service in caption.lower() for service in md_services)
        if not has_service:
            issues.append("No MD Aesthetics service mentioned")
            suggestions.append("Mention at least one MD Aesthetics service")
        
        # Check caption length
        if len(caption) < 50:
            issues.append("Caption too short - needs more educational content")
            suggestions.append("Expand caption to include more educational information")
        elif len(caption) > 2200:
            issues.append("Caption too long - may be cut off on some platforms")
            suggestions.append("Shorten caption to under 2200 characters")
        
        # Check for hashtags
        if len(content.hashtags) < 5:
            issues.append("Too few hashtags - need at least 5")
            suggestions.append("Add more relevant hashtags for better reach")
        elif len(content.hashtags) > 15:
            issues.append("Too many hashtags - may appear spammy")
            suggestions.append("Reduce to 8-12 high-quality hashtags")
        
        return {
            "compliant": len(issues) == 0,
            "issues": issues,
            "suggestions": suggestions
        }
    
    def _check_brand_voice(self, content: GeneratedContentCreate) -> Dict[str, Any]:
        """Check alignment with MD Aesthetics brand voice"""
        issues = []
        suggestions = []
        
        caption = content.caption.lower()
        
        # Check for professional tone indicators
        professional_indicators = [
            "physician", "medical", "clinical", "professional", "expert",
            "consultation", "treatment", "science", "technology"
        ]
        
        professional_score = sum(1 for indicator in professional_indicators if indicator in caption)
        
        if professional_score < 2:
            issues.append("Lacks professional medical tone")
            suggestions.append("Include more professional/medical terminology")
        
        # Check for educational content
        educational_indicators = [
            "learn", "understand", "science", "how", "why", "research",
            "study", "evidence", "results", "benefits"
        ]
        
        educational_score = sum(1 for indicator in educational_indicators if indicator in caption)
        
        if educational_score < 1:
            issues.append("Lacks educational value")
            suggestions.append("Add educational information about the treatment or process")
        
        # Check for unprofessional language
        unprofessional_words = [
            "cheap", "sale", "deal", "discount", "omg", "lol", "yolo",
            "amazing deal", "unbelievable", "too good to be true"
        ]
        
        for word in unprofessional_words:
            if word in caption:
                issues.append(f"Unprofessional language: '{word}'")
                suggestions.append(f"Remove or replace '{word}' with more professional language")
        
        return {
            "compliant": len(issues) == 0,
            "issues": issues,
            "suggestions": suggestions,
            "scores": {
                "professional_score": professional_score,
                "educational_score": educational_score
            }
        }
    
    def _check_hashtags(self, hashtags: List[str]) -> Dict[str, Any]:
        """Check hashtag compliance and relevance"""
        issues = []
        suggestions = []
        
        # Check for required MD Aesthetics hashtags
        required_hashtags = ["#mdaesthetics"]
        for required in required_hashtags:
            if not any(required.lower() == hashtag.lower() for hashtag in hashtags):
                issues.append(f"Missing required hashtag: {required}")
                suggestions.append(f"Add {required} hashtag")
        
        # Check hashtag format
        for hashtag in hashtags:
            if not hashtag.startswith('#'):
                issues.append(f"Invalid hashtag format: '{hashtag}' (must start with #)")
                suggestions.append(f"Fix hashtag format: #{hashtag}")
            
            # Check for spaces in hashtags
            if ' ' in hashtag:
                issues.append(f"Hashtag contains spaces: '{hashtag}'")
                suggestions.append(f"Remove spaces from hashtag: {hashtag.replace(' ', '')}")
            
            # Check hashtag length
            if len(hashtag) > 50:
                issues.append(f"Hashtag too long: '{hashtag}'")
                suggestions.append("Shorten overly long hashtags")
        
        # Check for location hashtags
        location_hashtags = ["#toronto", "#whitby", "#durham", "#ontario", "#canada"]
        has_location = any(loc in [h.lower() for h in hashtags] for loc in location_hashtags)
        if not has_location:
            issues.append("Missing location hashtags")
            suggestions.append("Add location-based hashtags like #torontoaesthetics or #whitbymedspa")
        
        return {
            "compliant": len(issues) == 0,
            "issues": issues,
            "suggestions": suggestions
        }
    
    def _generate_suggestions(self, issues: List[str], content: GeneratedContentCreate) -> List[str]:
        """Generate actionable suggestions based on issues found"""
        suggestions = []
        
        # Priority suggestions based on common issues
        if any("forbidden word" in issue for issue in issues):
            suggestions.append("Review and replace all non-compliant terminology")
        
        if any("call-to-action" in issue for issue in issues):
            suggestions.append("Add: 'Ready to learn more? Book your consultation - link in bio 📲'")
        
        if any("educational" in issue for issue in issues):
            suggestions.append("Include information about the science or process behind the treatment")
        
        if any("professional" in issue for issue in issues):
            suggestions.append("Use more clinical terminology like 'treatment', 'consultation', 'physician-led'")
        
        if any("hashtag" in issue for issue in issues):
            suggestions.append("Include #mdaesthetics and location-specific hashtags")
        
        # Content-specific suggestions
        if len(content.caption) < 100:
            suggestions.append("Expand content to better educate potential clients")
        
        return suggestions
    
    def _auto_fix_content(self, content: GeneratedContentCreate, issues: List[str]) -> GeneratedContentCreate:
        """Attempt to automatically fix common compliance issues"""
        
        fixed_caption = content.caption
        fixed_hashtags = content.hashtags.copy()
        
        # Fix forbidden words
        for forbidden, alternatives in self.forbidden_words.items():
            if forbidden in fixed_caption.lower():
                # Use first alternative
                fixed_caption = re.sub(
                    f"\\b{forbidden}\\b", 
                    alternatives[0], 
                    fixed_caption, 
                    flags=re.IGNORECASE
                )
        
        # Add CTA if missing
        cta_patterns = [
            r"book|schedule|call|contact|visit|consultation|appointment",
            r"link in bio|dm us|comment|swipe"
        ]
        has_cta = any(re.search(pattern, fixed_caption, re.IGNORECASE) for pattern in cta_patterns)
        if not has_cta:
            fixed_caption += "\n\nReady to learn more? Book your consultation - link in bio 📲"
        
        # Fix hashtags
        if "#mdaesthetics" not in [h.lower() for h in fixed_hashtags]:
            fixed_hashtags.append("#mdaesthetics")
        
        # Ensure hashtags start with #
        fixed_hashtags = [h if h.startswith('#') else f'#{h}' for h in fixed_hashtags]
        
        # Remove spaces from hashtags
        fixed_hashtags = [h.replace(' ', '') for h in fixed_hashtags]
        
        # Limit hashtags to 12
        fixed_hashtags = fixed_hashtags[:12]
        
        return GeneratedContentCreate(
            source_post_id=content.source_post_id,
            platform=content.platform,
            caption=fixed_caption,
            hashtags=fixed_hashtags,
            suggested_media_type=content.suggested_media_type
        )