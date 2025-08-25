package com.mdaesthetics.viral.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class TrendAnalyzerService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrendAnalyzerService.class);
    private final ObjectMapper objectMapper;
    
    public TrendAnalyzerService() {
        this.objectMapper = new ObjectMapper();
    }
    
    public Map<String, Object> analyzePost(Map<String, Object> postData) {
        try {
            logger.info("Starting trend analysis for post: {}", postData.get("platform"));
            
            // Convert post data to JSON string for analysis
            String postJson = objectMapper.writeValueAsString(postData);
            
            // Analyze content using business logic (placeholder for ADK agent integration)
            Map<String, Object> analysis = performTrendAnalysis(postData);
            
            logger.info("Trend analysis completed with category: {}", analysis.get("category"));
            return analysis;
            
        } catch (Exception e) {
            logger.error("Error in trend analysis", e);
            throw new RuntimeException("Trend analysis failed", e);
        }
    }
    
    private Map<String, Object> performTrendAnalysis(Map<String, Object> postData) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Analyze content based on keywords and patterns
        String caption = (String) postData.getOrDefault("caption", "");
        String platform = (String) postData.getOrDefault("platform", "instagram");
        
        // Determine category based on content patterns
        String category = determineCategoryFromContent(caption);
        String hook = extractHook(caption);
        String cta = extractCallToAction(caption);
        String educationalPoint = extractEducationalPoint(caption, category);
        
        analysis.put("category", category);
        analysis.put("hook", hook);
        analysis.put("cta", cta);
        analysis.put("educational_point", educationalPoint);
        analysis.put("engagement_score", calculateEngagementScore(postData));
        analysis.put("virality_score", calculateViralityScore(postData));
        analysis.put("relevance_score", calculateRelevanceScore(caption));
        
        return analysis;
    }
    
    private String determineCategoryFromContent(String caption) {
        if (caption.toLowerCase().contains("before") && caption.toLowerCase().contains("after")) {
            return "Transformation";
        } else if (caption.toLowerCase().contains("process") || caption.toLowerCase().contains("how")) {
            return "Process Demystified";
        } else if (caption.toLowerCase().contains("science") || caption.toLowerCase().contains("technology")) {
            return "Science Explained";
        } else if (caption.toLowerCase().contains("myth") || caption.toLowerCase().contains("truth")) {
            return "Myth Busting";
        }
        return "Transformation"; // Default category
    }
    
    private String extractHook(String caption) {
        // Extract first sentence or compelling opening
        String[] sentences = caption.split("[.!?]");
        if (sentences.length > 0) {
            return sentences[0].trim() + "...";
        }
        return "Before & After results that speak for themselves";
    }
    
    private String extractCallToAction(String caption) {
        if (caption.toLowerCase().contains("book")) return "Book your consultation";
        if (caption.toLowerCase().contains("call")) return "Call us today";
        if (caption.toLowerCase().contains("dm")) return "DM us for details";
        if (caption.toLowerCase().contains("link")) return "Link in bio";
        return "Book your consultation";
    }
    
    private String extractEducationalPoint(String caption, String category) {
        switch (category) {
            case "Transformation":
                return "SkinTyte technology uses infrared light to stimulate collagen";
            case "Process Demystified":
                return "Our physician-led approach ensures safe, effective treatments";
            case "Science Explained":
                return "Clinical-grade treatments deliver superior results";
            default:
                return "Advanced aesthetic technology with proven results";
        }
    }
    
    private int calculateViralityScore(Map<String, Object> postData) {
        double engagementScore = calculateEngagementScore(postData);
        if (engagementScore > 10) return 9;
        if (engagementScore > 5) return 7;
        if (engagementScore > 2) return 5;
        return 3;
    }
    
    private int calculateRelevanceScore(String caption) {
        int score = 5; // Base score
        String lowerCaption = caption.toLowerCase();
        
        // Check for aesthetic-related keywords
        if (lowerCaption.contains("aesthetic") || lowerCaption.contains("beauty")) score += 2;
        if (lowerCaption.contains("skintyte") || lowerCaption.contains("ultherapy")) score += 3;
        if (lowerCaption.contains("toronto") || lowerCaption.contains("whitby")) score += 1;
        
        return Math.min(score, 10);
    }
    
    private double calculateEngagementScore(Map<String, Object> postData) {
        try {
            Integer likes = (Integer) postData.getOrDefault("likes", 0);
            Integer comments = (Integer) postData.getOrDefault("comments", 0);
            Integer shares = (Integer) postData.getOrDefault("shares", 0);
            
            return (likes + comments + shares) / 1000.0; // Simplified calculation
        } catch (Exception e) {
            logger.warn("Error calculating engagement score, using default", e);
            return 5.0;
        }
    }
}