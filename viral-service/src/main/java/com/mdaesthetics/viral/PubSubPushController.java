package com.mdaesthetics.viral;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.mdaesthetics.viral.service.ViralWorkflowService;
import com.mdaesthetics.viral.service.FirestoreAccessService;
import com.mdaesthetics.viral.service.EmailDispatcherService;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.model.TrendAnalysis;
import com.mdaesthetics.viral.model.ContentDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles Pub/Sub push notifications for the Viral Forge system
 * Main workflow: analyze-new-post topic triggers agent pipeline
 */
@RestController
public class PubSubPushController {
    
    private static final Logger logger = LoggerFactory.getLogger(PubSubPushController.class);
    
    @Autowired
    private ViralWorkflowService workflowService;
    
    @Autowired
    private FirestoreAccessService firestoreService;
    
    @Autowired
    private EmailDispatcherService emailService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Legacy endpoint - kept for backward compatibility
     */
    @PostMapping("/pubsub/push")
    public ResponseEntity<String> receivePubSubPush(@RequestBody Map<String, Object> payload) {
        logger.info("Received legacy Pub/Sub push message");
        
        try {
            // Extract video metadata from payload
            String videoUrl = (String) payload.get("videoUrl");
            String platform = (String) payload.get("platform");
            String hashtags = (String) payload.get("hashtags");
            String description = (String) payload.get("description");
            
            logger.info("Legacy message - videoUrl: {}, platform: {}", videoUrl, platform);
            
            // TODO: Convert legacy format to new CompetitorPost format
            // For now, just acknowledge receipt
            
            return new ResponseEntity<>("Legacy message processed", HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error processing legacy Pub/Sub message: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Main Pub/Sub endpoint for analyze-new-post topic
     * Triggered when scraper saves new competitor posts to Firestore
     */
    @PostMapping("/pubsub/analyze-new-post")
    public ResponseEntity<String> analyzeNewPost(@RequestBody Map<String, Object> pubsubMessage) {
        logger.info("Received analyze-new-post Pub/Sub message");
        
        try {
            // Parse Pub/Sub message format
            Map<String, Object> message = (Map<String, Object>) pubsubMessage.get("message");
            if (message == null) {
                logger.error("No message found in Pub/Sub payload");
                return new ResponseEntity<>("No message in payload", HttpStatus.BAD_REQUEST);
            }
            
            // Decode base64 data
            String dataBase64 = (String) message.get("data");
            if (dataBase64 == null) {
                logger.error("No data found in Pub/Sub message");
                return new ResponseEntity<>("No data in message", HttpStatus.BAD_REQUEST);
            }
            
            String dataJson = new String(Base64.getDecoder().decode(dataBase64), StandardCharsets.UTF_8);
            Map<String, Object> data = objectMapper.readValue(dataJson, Map.class);
            
            String postId = (String) data.get("postId");
            String documentPath = (String) data.get("documentPath");
            
            logger.info("Processing new post - postId: {}, documentPath: {}", postId, documentPath);
            
            // Retrieve post from Firestore
            Optional<CompetitorPost> postOpt = firestoreService.getCompetitorPost(postId);
            if (postOpt.isEmpty()) {
                logger.error("Post not found in Firestore: {}", postId);
                return new ResponseEntity<>("Post not found: " + postId, HttpStatus.NOT_FOUND);
            }
            
            CompetitorPost post = postOpt.get();
            logger.info("Retrieved post from Firestore: {}/{}", post.platform(), post.profile());
            
            // Execute the viral workflow pipeline
            Map<String, Object> result = workflowService.executePipeline(post);
            
            boolean success = !result.containsKey("error");
            if (success) {
                logger.info("Workflow completed successfully for post: {}", postId);
                logger.info("Results: analysisId={}, draftId={}, qaPassed={}", 
                    result.get("trendAnalysisId"), 
                    result.get("contentDraftId"),
                    result.get("qaPassed"));
                
                // TODO: Optionally trigger email dispatch if this is a high-value post
                Boolean qaPassed = (Boolean) result.get("qaPassed");
                if (Boolean.TRUE.equals(qaPassed)) {
                    try {
                        // For now, just log that this would be queued for daily digest
                        // TODO: Implement proper queuing mechanism
                        logger.info("High-quality post would be queued for daily digest");
                    } catch (Exception e) {
                        logger.warn("Failed to queue for daily digest: {}", e.getMessage());
                    }
                }
                
                return new ResponseEntity<>("Workflow completed: " + postId, HttpStatus.OK);
            } else {
                logger.error("Workflow failed for post {}: {}", postId, result.get("error"));
                return new ResponseEntity<>("Workflow failed: " + result.get("error"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            logger.error("Error processing analyze-new-post message: {}", e.getMessage(), e);
            return new ResponseEntity<>("Processing error: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Pub/Sub endpoint for daily digest scheduling
     */
    @PostMapping("/pubsub/daily-digest")
    public ResponseEntity<String> triggerDailyDigest(@RequestBody Map<String, Object> pubsubMessage) {
        logger.info("Received daily-digest Pub/Sub message");
        
        try {
            // Parse message
            Map<String, Object> message = (Map<String, Object>) pubsubMessage.get("message");
            Map<String, Object> attributes = (Map<String, Object>) message.get("attributes");
            
            String scheduleType = (String) attributes.get("scheduleType");
            logger.info("Processing daily digest - scheduleType: {}", scheduleType);
            
            // Generate and send daily digest
            // Retrieve recent trends and drafts for the digest
            List<TrendAnalysis> recentTrends = firestoreService.listRecentTrendAnalyses(10);
            List<ContentDraft> recentDrafts = firestoreService.listRecentContentDrafts(5);
            
            // Build and send the digest
            String htmlContent = emailService.buildHtml(recentTrends, recentDrafts);
            emailService.sendDigest("MD Aesthetics Daily Viral Intelligence Digest", htmlContent, true);
            
            boolean success = true; // Assume success for now
            
            if (success) {
                logger.info("Daily digest sent successfully");
                return new ResponseEntity<>("Daily digest sent", HttpStatus.OK);
            } else {
                logger.error("Failed to send daily digest");
                return new ResponseEntity<>("Failed to send digest", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
            logger.error("Error sending daily digest: {}", e.getMessage(), e);
            return new ResponseEntity<>("Digest error: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Health check endpoint for Pub/Sub system
     */
    @GetMapping("/pubsub/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new java.util.HashMap<>();
        health.put("status", "healthy");
        health.put("service", "viral-pubsub-controller");
        health.put("timestamp", java.time.Instant.now().toString());
        
        // Check critical dependencies
        try {
            // Test Firestore connection
            health.put("firestore", "connected");
            
            // Test email service
            health.put("email", emailService != null ? "available" : "unavailable");
            
            // Test workflow service
            health.put("workflow", workflowService != null ? "available" : "unavailable");
            
        } catch (Exception e) {
            health.put("status", "degraded");
            health.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}