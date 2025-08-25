package com.mdaesthetics.viral.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class QAService {
    
    private static final Logger logger = LoggerFactory.getLogger(QAService.class);
    
    public QAService() {
        // Initialize any QA validation resources
    }
    
    public Map<String, Object> validateContent(Map<String, Object> content) {
        try {
            logger.info("Starting QA validation for content");
            
            Map<String, Object> qaResults = new HashMap<>();
            
            String caption = (String) content.get("caption");
            List<String> hashtags = (List<String>) content.get("hashtags");
            Boolean complianceChecked = (Boolean) content.get("complianceChecked");
            
            // Compliance checks
            boolean hasProhibitedWords = checkProhibitedWords(caption);
            boolean hasValidCTA = checkCallToAction(caption);
            boolean hasAppropriateHashtags = checkHashtags(hashtags);
            boolean hasBrandAlignment = checkBrandAlignment(caption);
            
            // Calculate overall compliance score
            int score = 0;
            if (!hasProhibitedWords) score += 25; // No prohibited words like "Botox"
            if (hasValidCTA) score += 25; // Has clear call-to-action
            if (hasAppropriateHashtags) score += 25; // Has relevant hashtags
            if (hasBrandAlignment) score += 25; // Aligns with MDAesthetics brand
            
            boolean compliancePass = score >= 75;
            
            qaResults.put("score", score);
            qaResults.put("compliancePass", compliancePass);
            qaResults.put("hasProhibitedWords", hasProhibitedWords);
            qaResults.put("hasValidCTA", hasValidCTA);
            qaResults.put("hasAppropriateHashtags", hasAppropriateHashtags);
            qaResults.put("hasBrandAlignment", hasBrandAlignment);
            
            if (!compliancePass) {
                qaResults.put("issues", getComplianceIssues(hasProhibitedWords, hasValidCTA, hasAppropriateHashtags, hasBrandAlignment));
            }
            
            logger.info("QA validation completed with score: {}", score);
            return qaResults;
            
        } catch (Exception e) {
            logger.error("Error in QA validation", e);
            throw new RuntimeException("QA validation failed", e);
        }
    }
    
    private boolean checkProhibitedWords(String caption) {
        if (caption == null) return false;
        String lowerCaption = caption.toLowerCase();
        return lowerCaption.contains("botox") || lowerCaption.contains("price") || lowerCaption.contains("$");
    }
    
    private boolean checkCallToAction(String caption) {
        if (caption == null) return false;
        String lowerCaption = caption.toLowerCase();
        return lowerCaption.contains("book") || lowerCaption.contains("consult") || 
               lowerCaption.contains("call") || lowerCaption.contains("link in bio");
    }
    
    private boolean checkHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) return false;
        return hashtags.contains("#torontoaesthetics") || hashtags.contains("#mdaesthetics");
    }
    
    private boolean checkBrandAlignment(String caption) {
        if (caption == null) return false;
        String lowerCaption = caption.toLowerCase();
        return lowerCaption.contains("consultation") || lowerCaption.contains("clinical") ||
               lowerCaption.contains("skintyte") || lowerCaption.contains("duoclift") ||
               lowerCaption.contains("vivier");
    }
    
    private List<String> getComplianceIssues(boolean hasProhibited, boolean hasValidCTA, 
                                           boolean hasHashtags, boolean hasBrandAlignment) {
        List<String> issues = new java.util.ArrayList<>();
        
        if (hasProhibited) issues.add("Contains prohibited words (e.g., 'Botox', pricing)");
        if (!hasValidCTA) issues.add("Missing clear call-to-action");
        if (!hasHashtags) issues.add("Missing required branded hashtags");
        if (!hasBrandAlignment) issues.add("Content doesn't align with MDAesthetics brand voice");
        
        return issues;
    }
}