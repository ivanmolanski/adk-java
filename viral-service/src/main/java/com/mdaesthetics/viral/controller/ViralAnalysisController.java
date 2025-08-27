package com.mdaesthetics.viral.controller;

import com.mdaesthetics.viral.model.PaginatedResult;
import com.mdaesthetics.viral.model.TrendAnalysis;
import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.dto.TrendAnalysisDto;
import com.mdaesthetics.viral.dto.ContentDraftDto;
import com.mdaesthetics.viral.dto.DtoMapper;
import com.mdaesthetics.viral.dto.TrendDetailDto;
import com.mdaesthetics.viral.dto.DraftDetailDto;
import com.mdaesthetics.viral.service.ViralAggregationService;
import com.mdaesthetics.viral.service.ViralWorkflowService;
import com.mdaesthetics.viral.service.FirestoreAccessService;
import io.micrometer.core.instrument.MeterRegistry;
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
    
    private final ViralWorkflowService viralWorkflowService;
    private final FirestoreAccessService firestoreAccessService;
    private final ViralAggregationService aggregationService;

    private final io.micrometer.core.instrument.Counter trendsListCounter;
    private final io.micrometer.core.instrument.Counter draftsListCounter;
    private final io.micrometer.core.instrument.Counter trendsDetailCounter;
    private final io.micrometer.core.instrument.Counter draftsDetailCounter;

    public ViralAnalysisController(
            MeterRegistry meterRegistry,
            ViralWorkflowService viralWorkflowService,
            FirestoreAccessService firestoreAccessService,
            ViralAggregationService aggregationService) {
        this.viralWorkflowService = viralWorkflowService;
        this.firestoreAccessService = firestoreAccessService;
        this.aggregationService = aggregationService;
        this.trendsListCounter = meterRegistry.counter("api.trends.list.count");
        this.draftsListCounter = meterRegistry.counter("api.drafts.list.count");
        this.trendsDetailCounter = meterRegistry.counter("api.trends.detail.count");
        this.draftsDetailCounter = meterRegistry.counter("api.drafts.detail.count");
    }
    
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrends(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "cursor", required = false) String cursor) {
        logger.info("Fetching trends with limit: {}, cursor: {}", limit, cursor);
        trendsListCounter.increment();

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 10;
        }

        PaginatedResult<TrendAnalysis> result = firestoreAccessService.listTrendAnalysesWithCursor(limit, cursor);
        List<TrendAnalysisDto> trendDtos;

        if (result.items().isEmpty()) {
            trendDtos = List.of(new TrendAnalysisDto(
                    "trend_sample_1",
                    "sample_post_1",
                    "Science Explained",
                    "How BBL light penetrates 7 layers of skin",
                    "Book your consultation",
                    "BBL uses IPL technology to target pigment and blood vessels in the dermis",
                    List.of("#bblforever", "#sciencebasedskincare", "#torontoaesthetics"),
                    0.8,
                    0.9,
                    Instant.now()
            ));
        } else {
            trendDtos = result.items().stream().map(analysis -> DtoMapper.toDto(analysis)).toList();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("trends", trendDtos);
        response.put("count", trendDtos.size());
        response.put("pagination", Map.of(
            "hasMore", result.hasMore(),
            "nextCursor", result.nextCursor() != null ? result.nextCursor() : ""
        ));
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/drafts")
    public ResponseEntity<Map<String, Object>> getDrafts(
            @RequestParam(value = "limit", defaultValue = "5") int limit,
            @RequestParam(value = "cursor", required = false) String cursor) {
        logger.info("Fetching drafts with limit: {}, cursor: {}", limit, cursor);
        draftsListCounter.increment();

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 5;
        }

        PaginatedResult<ContentDraft> result = firestoreAccessService.listContentDraftsWithCursor(limit, cursor);
        List<ContentDraftDto> dtoList;

        if (result.items().isEmpty()) {
            dtoList = List.of(new ContentDraftDto(
                    "draft_sample_1",
                    "trend_sample_1",
                    "SkinTyte",
                    "Loose skin? Infrared tightening is back.",
                    "We pair SkinTyte with clinical-grade collagen support to firm and smooth without downtime.",
                    List.of("#skintyte", "#firmandsmooth", "#torontoaesthetics", "#mdaesthetics"),
                    "DM to see if you're a candidate for our Tyte & Tone bundle.",
                    true,
                    true,
                    "Auto-generated sample",
                    Instant.now()
            ));
        } else {
            dtoList = result.items().stream().map(draft -> DtoMapper.toDto(draft)).toList();
        }

        Map<String,Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("drafts", dtoList);
        response.put("count", dtoList.size());
        response.put("pagination", Map.of(
            "hasMore", result.hasMore(),
            "nextCursor", result.nextCursor() != null ? result.nextCursor() : ""
        ));
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

    @GetMapping("/trends/{id}")
    public ResponseEntity<Map<String, Object>> getTrendDetail(@PathVariable("id") String id) {
        logger.info("Fetching trend detail id={}", id);
        trendsDetailCounter.increment();
        Map<String,Object> response = new HashMap<>();
        try {
            Optional<TrendDetailDto> dtoOpt = aggregationService.buildTrendDetail(id);
            if (dtoOpt.isEmpty()) {
                // fallback sample
                TrendDetailDto sample = aggregationService.sampleTrendDetail();
                response.put("status", "fallback");
                response.put("detail", sample);
                response.put("timestamp", Instant.now().toString());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("detail", dtoOpt.get());
            response.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Trend detail error id={} msg={}", id, e.getMessage());
            // Treat backend data access failures as a graceful fallback response
            response.put("status", "fallback");
            response.put("detail", aggregationService.sampleTrendDetail());
            response.put("message", e.getMessage());
            response.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/drafts/{id}")
    public ResponseEntity<Map<String, Object>> getDraftDetail(@PathVariable("id") String id) {
        logger.info("Fetching draft detail id={}", id);
        draftsDetailCounter.increment();
        Map<String,Object> response = new HashMap<>();
        try {
            Optional<DraftDetailDto> dtoOpt = aggregationService.buildDraftDetail(id);
            if (dtoOpt.isEmpty()) {
                DraftDetailDto sample = aggregationService.sampleDraftDetail();
                response.put("status", "fallback");
                response.put("detail", sample);
                response.put("timestamp", Instant.now().toString());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("detail", dtoOpt.get());
            response.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Draft detail error id={} msg={}", id, e.getMessage());
            // Graceful fallback on data access errors
            response.put("status", "fallback");
            response.put("detail", aggregationService.sampleDraftDetail());
            response.put("message", e.getMessage());
            response.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "viral-service");
        health.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(health);
    }
    
    @PostMapping("/pipeline/test")
    public ResponseEntity<Map<String, Object>> testPipeline(@RequestBody Map<String, Object> request) {
        logger.info("Testing complete ADK agent pipeline");
        
        try {
            // Create a sample competitor post from the request
            CompetitorPost samplePost = new CompetitorPost(
                null, // id will be generated
                (String) request.getOrDefault("platform", "instagram"),
                (String) request.getOrDefault("profile", "@skinvitalityofficial"),
                (String) request.getOrDefault("postUrl", "https://www.instagram.com/p/sample123"),
                (String) request.getOrDefault("caption", "Amazing transformation with our latest SkinTyte treatment! See the incredible results after just one session. Book your consultation today! ✨"),
                Arrays.asList("#skintyte", "#skinlaxity", "#torontoaesthetics"),
                Long.valueOf((Integer) request.getOrDefault("likes", 1250)),
                Long.valueOf((Integer) request.getOrDefault("comments", 89)),
                Long.valueOf((Integer) request.getOrDefault("shares", 23)),
                Long.valueOf((Integer) request.getOrDefault("views", 8500)),
                3.2, // engagementRate
                0.85, // evs
                Instant.now().minusSeconds(7200), // postedAt 2 hours ago
                Instant.now() // scrapedAt now
            );
            
            // Execute the complete pipeline
            Map<String, Object> pipelineResult = viralWorkflowService.executePipeline(samplePost);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Complete ADK agent pipeline executed successfully");
            response.put("pipelineResult", pipelineResult);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Pipeline test failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Pipeline test failed: " + e.getMessage());
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
        }
    }
    
    // sample factory methods removed in favor of DTO fallbacks
}
