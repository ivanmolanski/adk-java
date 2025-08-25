package com.mdaesthetics.viral.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/viral")
@CrossOrigin(origins = "*")
public class ViralAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(ViralAnalysisController.class);
    
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrends(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        logger.info("Fetching trends with limit: {}", limit);
        
        // Sample trend data for testing
        List<Map<String, Object>> sampleTrends = Arrays.asList(
            createTrendSample("trend_1", "Science Explained", "How BBL light penetrates 7 layers of skin", 
                "Book your consultation", "BBL uses IPL technology to target pigment and blood vessels in the dermis",
                Arrays.asList("#bblforever", "#sciencebasedskincare", "#torontoaesthetics"), 0.8, 0.9),
            
            createTrendSample("trend_2", "Process Demystified", "SkinTyte treatment: what to expect during your session",
                "Call us to book", "Infrared energy heats collagen fibers causing immediate tightening",
                Arrays.asList("#skintyte", "#skinlaxity", "#mdaesthetics"), 0.7, 0.85),
                
            createTrendSample("trend_3", "Transformation", "6 months post Duo-C-Lift: neck and jawline results",
                "See if you're a candidate", "Ultherapy builds collagen while Radiesse provides structure",
                Arrays.asList("#duoclift", "#nonsurgicallift", "#radiesse"), 0.9, 0.8),
                
            createTrendSample("trend_4", "Myth Busting", "Why at-home RF devices can't compete with professional SkinTyte",
                "Book professional assessment", "Clinical-grade energy levels require medical supervision",
                Arrays.asList("#mythbusting", "#professionalskincare", "#whitbymedspa"), 0.6, 0.9)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("trends", sampleTrends.subList(0, Math.min(limit, sampleTrends.size())));
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/drafts")
    public ResponseEntity<Map<String, Object>> getDrafts(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        logger.info("Fetching drafts with limit: {}", limit);
        
        // Sample draft data for testing
        List<Map<String, Object>> sampleDrafts = Arrays.asList(
            createDraftSample("draft_1", "instagram", 
                "Transform your skin with our signature Duo-C-Lift! 🌟 Combining Ultherapy's precision with Radiesse's collagen boost = natural-looking lift without surgery. Book your consultation to see if you're a candidate! ✨",
                Arrays.asList("#duoclift", "#ultherapy", "#radiesse", "#torontoaesthetics", "#mdaesthetics"), "video", true),
                
            createDraftSample("draft_2", "instagram",
                "SkinTyte treatment demystified! 💡 Our infrared technology heats collagen to 40-45°C, causing immediate tightening + long-term firming. Perfect for loose skin on face, neck, or body. DM us for your consultation! 🔥",
                Arrays.asList("#skintyte", "#skinlaxity", "#collagenstimulation", "#whitbymedspa", "#firmskin"), "carousel", true),
                
            createDraftSample("draft_3", "tiktok",
                "POV: You discover the science behind BBL treatments 🔬 IPL energy targets melanin + hemoglobin in 7 skin layers = clearer, more even tone. Book your BBL consultation! Link in bio ⚡",
                Arrays.asList("#bblforever", "#sciencebasedskincare", "#ipllaser", "#skintone", "#mdaesthetics"), "video", true)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("drafts", sampleDrafts.subList(0, Math.min(limit, sampleDrafts.size())));
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/brief")
    public ResponseEntity<Map<String, Object>> getBrief() {
        logger.info("Fetching brief");
        
        // Sample daily brief for testing
        Map<String, Object> sampleBrief = new HashMap<>();
        sampleBrief.put("id", "brief_" + java.time.LocalDate.now().toString());
        sampleBrief.put("date", java.time.LocalDate.now().toString());
        sampleBrief.put("summary", "Today's analysis shows strong engagement with science-based content. BBL and skin laxity treatments are trending. Competitors are focusing on educational content with immediate visual results.");
        sampleBrief.put("recommendations", Arrays.asList(
            "Create BBL science explanation video showing light penetration layers",
            "Develop SkinTyte before/during/after content for transparency",
            "Focus on Duo-C-Lift transformation stories with timeline",
            "Emphasize physician-led expertise vs. spa treatments"
        ));
        sampleBrief.put("sentAt", Instant.now().toString());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("brief", sampleBrief);
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "viral-service");
        health.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(health);
    }
    
    private Map<String, Object> createTrendSample(String id, String category, String hook, 
            String cta, String educationalPoint, List<String> hashtags, double viralityScore, double relevanceScore) {
        Map<String, Object> trend = new HashMap<>();
        trend.put("id", id);
        trend.put("category", category);
        trend.put("hook", hook);
        trend.put("callToAction", cta);
        trend.put("educationalPoint", educationalPoint);
        trend.put("extractedHashtags", hashtags);
        trend.put("viralityScore", viralityScore);
        trend.put("relevanceScore", relevanceScore);
        trend.put("analyzedAt", Instant.now().toString());
        return trend;
    }
    
    private Map<String, Object> createDraftSample(String id, String platform, String caption,
            List<String> hashtags, String mediaType, boolean complianceChecked) {
        Map<String, Object> draft = new HashMap<>();
        draft.put("id", id);
        draft.put("platform", platform);
        draft.put("caption", caption);
        draft.put("hashtags", hashtags);
        draft.put("suggestedMediaType", mediaType);
        draft.put("complianceChecked", complianceChecked);
        draft.put("createdAt", Instant.now().toString());
        return draft;
    }
}
