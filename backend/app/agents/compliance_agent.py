"""
ComplianceAgent

Pydantic-based agent for validating content against MD Aesthetics brand guidelines
and medical aesthetics regulations.
"""

from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
import logging
import re
from datetime import datetime

logger = logging.getLogger(__name__)

class ComplianceIssue(BaseModel):
    """Model for compliance issues."""
    issue_type: str = Field(..., description="Type of compliance issue")
    severity: str = Field(..., description="Severity: low, medium, high, critical")
    description: str = Field(..., description="Description of the issue")
    suggestion: Optional[str] = Field(None, description="Suggested fix")

class ComplianceResult(BaseModel):
    """Model for compliance check results."""
    content_id: str
    is_compliant: bool = Field(..., description="Overall compliance status")
    issues: List[ComplianceIssue] = Field(default_factory=list)
    score: float = Field(..., ge=0.0, le=1.0, description="Compliance score (0-1)")
    approved_content: Optional[str] = Field(None, description="Auto-corrected content")
    checked_at: datetime = Field(default_factory=datetime.utcnow)

class ComplianceAgent(BaseModel):
    """
    Pydantic-based ComplianceAgent for MD Aesthetics.
    
    Validates content against:
    - Brand guidelines and voice
    - Medical aesthetics regulations
    - Forbidden terms and phrases
    - Professional standards
    """
    
    name: str = "ComplianceAgent"
    version: str = "2.0.0"
    description: str = "Validates content compliance for MD Aesthetics"
    
    # Brand compliance rules
    forbidden_terms: List[str] = Field(
        default_factory=lambda: [
            "Botox", "cheap", "discount", "deal", "sale", "miracle", "cure",
            "guaranteed results", "instant", "permanent", "pain-free"
        ]
    )
    
    required_replacements: Dict[str, str] = Field(
        default_factory=lambda: {
            "Botox": "Tox/Neuromodulator/Neurotoxin",
            "cheap": "affordable",
            "instant": "quick",
            "pain-free": "comfortable"
        }
    )
    
    professional_tone_indicators: List[str] = Field(
        default_factory=lambda: [
            "physician", "clinical", "medical grade", "treatment", "consultation",
            "assessment", "professional", "advanced", "technology"
        ]
    )
    
    def check_compliance(self, content: str, content_id: str = None) -> ComplianceResult:
        """
        Check content compliance against all rules.
        
        Args:
            content: The content to check
            content_id: Optional identifier for the content
            
        Returns:
            ComplianceResult with issues and score
        """
        if not content_id:
            content_id = f"content_{datetime.utcnow().isoformat()}"
        
        logger.info(f"Checking compliance for {content_id}")
        
        issues = []
        approved_content = content
        
        # Check forbidden terms
        forbidden_issues = self._check_forbidden_terms(content)
        issues.extend(forbidden_issues)
        
        # Apply auto-corrections
        approved_content = self._apply_auto_corrections(approved_content)
        
        # Check professional tone
        tone_issues = self._check_professional_tone(content)
        issues.extend(tone_issues)
        
        # Check medical claims
        medical_issues = self._check_medical_claims(content)
        issues.extend(medical_issues)
        
        # Check length and structure
        structure_issues = self._check_structure(content)
        issues.extend(structure_issues)
        
        # Calculate compliance score
        score = self._calculate_compliance_score(issues)
        
        # Determine overall compliance
        critical_issues = [i for i in issues if i.severity == "critical"]
        is_compliant = len(critical_issues) == 0 and score >= 0.7
        
        return ComplianceResult(
            content_id=content_id,
            is_compliant=is_compliant,
            issues=issues,
            score=score,
            approved_content=approved_content if approved_content != content else None
        )
    
    def _check_forbidden_terms(self, content: str) -> List[ComplianceIssue]:
        """Check for forbidden terms."""
        issues = []
        content_lower = content.lower()
        
        for term in self.forbidden_terms:
            if term.lower() in content_lower:
                severity = "critical" if term in ["Botox", "guaranteed results"] else "high"
                suggestion = self.required_replacements.get(term, f"Remove or rephrase '{term}'")
                
                issues.append(ComplianceIssue(
                    issue_type="forbidden_term",
                    severity=severity,
                    description=f"Contains forbidden term: '{term}'",
                    suggestion=f"Replace with: {suggestion}"
                ))
        
        return issues
    
    def _apply_auto_corrections(self, content: str) -> str:
        """Apply automatic corrections for known issues."""
        corrected = content
        
        for forbidden, replacement in self.required_replacements.items():
            # Case-insensitive replacement
            pattern = re.compile(re.escape(forbidden), re.IGNORECASE)
            corrected = pattern.sub(replacement, corrected)
        
        return corrected
    
    def _check_professional_tone(self, content: str) -> List[ComplianceIssue]:
        """Check for professional tone indicators."""
        issues = []
        content_lower = content.lower()
        
        # Count professional indicators
        professional_count = sum(1 for indicator in self.professional_tone_indicators 
                                if indicator in content_lower)
        
        if professional_count == 0:
            issues.append(ComplianceIssue(
                issue_type="tone",
                severity="medium",
                description="Content lacks professional tone indicators",
                suggestion="Add terms like 'clinical', 'physician-led', or 'medical grade'"
            ))
        
        # Check for overly casual language
        casual_terms = ["awesome", "amazing", "wow", "omg", "super", "totally"]
        casual_count = sum(1 for term in casual_terms if term in content_lower)
        
        if casual_count > 2:
            issues.append(ComplianceIssue(
                issue_type="tone",
                severity="medium",
                description="Content contains too many casual terms",
                suggestion="Use more professional language"
            ))
        
        return issues
    
    def _check_medical_claims(self, content: str) -> List[ComplianceIssue]:
        """Check for problematic medical claims."""
        issues = []
        content_lower = content.lower()
        
        # Problematic medical claims
        problematic_claims = [
            "cure", "heal", "medical treatment", "guaranteed results",
            "permanent results", "no side effects", "risk-free"
        ]
        
        for claim in problematic_claims:
            if claim in content_lower:
                issues.append(ComplianceIssue(
                    issue_type="medical_claim",
                    severity="critical",
                    description=f"Contains problematic medical claim: '{claim}'",
                    suggestion="Remove medical claims or add appropriate disclaimers"
                ))
        
        # Check for pricing mentions (should be in consultation)
        pricing_indicators = ["$", "£", "€", "price", "cost", "fee"]
        if any(indicator in content for indicator in pricing_indicators):
            issues.append(ComplianceIssue(
                issue_type="pricing",
                severity="high",
                description="Contains pricing information",
                suggestion="Direct to consultation for pricing discussions"
            ))
        
        return issues
    
    def _check_structure(self, content: str) -> List[ComplianceIssue]:
        """Check content structure and length."""
        issues = []
        
        # Check length
        if len(content) > 2200:  # Instagram limit
            issues.append(ComplianceIssue(
                issue_type="length",
                severity="high",
                description=f"Content too long ({len(content)} characters)",
                suggestion="Shorten to under 2200 characters for Instagram"
            ))
        
        # Check for call-to-action
        cta_indicators = ["book", "call", "contact", "schedule", "consultation", "dm", "link"]
        has_cta = any(indicator in content.lower() for indicator in cta_indicators)
        
        if not has_cta:
            issues.append(ComplianceIssue(
                issue_type="structure",
                severity="medium",
                description="Missing clear call-to-action",
                suggestion="Add CTA like 'Book consultation' or 'Contact us'"
            ))
        
        return issues
    
    def _calculate_compliance_score(self, issues: List[ComplianceIssue]) -> float:
        """Calculate overall compliance score."""
        if not issues:
            return 1.0
        
        # Weight by severity
        severity_weights = {
            "low": 0.05,
            "medium": 0.15,
            "high": 0.25,
            "critical": 0.4
        }
        
        total_deduction = sum(severity_weights.get(issue.severity, 0.2) for issue in issues)
        score = max(0.0, 1.0 - total_deduction)
        
        return score