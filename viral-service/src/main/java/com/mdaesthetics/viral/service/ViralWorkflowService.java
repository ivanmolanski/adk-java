package com.mdaesthetics.viral.service;

import com.mdaesthetics.viral.agents.ContentCreatorAgent;
import com.mdaesthetics.viral.agents.QAAgent;
import com.mdaesthetics.viral.agents.TrendAnalyzerAgent;
import com.mdaesthetics.viral.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Orchestrates a single post ingestion lifecycle: CompetitorPost -> TrendAnalysis -> ContentDraft -> QA -> persistence.
 * Pub/Sub listener or controller will call executePipeline().
 */
@Service
public class ViralWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(ViralWorkflowService.class);

    private final TrendAnalyzerAgent trendAnalyzer;
    private final ContentCreatorAgent contentCreator;
    private final QAAgent qaAgent;
    private final FirestoreAccessService firestore;

    private final Counter trendCacheHit;
    private final Counter trendCacheMiss;
    private final Counter draftCacheHit;
    private final Counter draftCacheMiss;
    private final Timer workflowTimer;
    private final Counter workflowErrorCounter;

    public ViralWorkflowService(TrendAnalyzerAgent trendAnalyzer, ContentCreatorAgent contentCreator,
                                QAAgent qaAgent, FirestoreAccessService firestore, MeterRegistry meterRegistry) {
        this.trendAnalyzer = trendAnalyzer;
        this.contentCreator = contentCreator;
        this.qaAgent = qaAgent;
        this.firestore = firestore;
        this.trendCacheHit = meterRegistry.counter("trendAnalysis.cache.hit");
        this.trendCacheMiss = meterRegistry.counter("trendAnalysis.cache.miss");
        this.draftCacheHit = meterRegistry.counter("contentDraft.cache.hit");
        this.draftCacheMiss = meterRegistry.counter("contentDraft.cache.miss");
        this.workflowTimer = meterRegistry.timer("workflow.execute.timer");
        this.workflowErrorCounter = meterRegistry.counter("workflow.error.count");
    }

    public Map<String,Object> executePipeline(CompetitorPost post) {
        long start = System.currentTimeMillis();
        Map<String,Object> result = new LinkedHashMap<>();
        try {
            // 1. Save raw post (if not already persisted)
            CompetitorPost savedPost = firestore.saveCompetitorPost(post);
            result.put("competitorPostId", savedPost.id());

            // 1b. Attempt to find existing TrendAnalysis for this competitor post to avoid duplicate LLM cost
            final String savedPostId = savedPost.id();
            TrendAnalysis existingAnalysis = firestore.findLatestTrendAnalysisForCompetitorPost(savedPostId).orElse(null);
            TrendAnalysis analysis;
            if (existingAnalysis != null) {
                trendCacheHit.increment();
                log.info("[workflow] cache hit trendAnalysis competitorPostId={}", savedPost.id());
                analysis = existingAnalysis;
            } else {
                trendCacheMiss.increment();
                // 2. Trend analysis (only if not cached)
                analysis = trendAnalyzer.analyze(savedPost);
                analysis = firestore.saveTrendAnalysis(analysis);
            }
            result.put("trendAnalysisId", analysis.id());

            // 2b. Check for existing content draft tied to this analysis
            final String analysisIdForDraft = analysis.id();
            ContentDraft existingDraft = firestore.findLatestContentDraftForTrendAnalysis(analysisIdForDraft).orElse(null);
            ContentDraft finalDraft;
            if (existingDraft != null) {
                draftCacheHit.increment();
                log.info("[workflow] cache hit contentDraft analysisId={} draftId={}", analysis.id(), existingDraft.id());
                finalDraft = existingDraft;
                result.put("contentDraftId", finalDraft.id());
                result.put("qaPassed", finalDraft.compliancePassed());
                result.put("qaNotes", List.of(finalDraft.complianceNotes()));
                long latency = System.currentTimeMillis() - start;
                workflowTimer.record(latency, java.util.concurrent.TimeUnit.MILLISECONDS);
                log.info("[workflow] reused pipeline postId={} analysisId={} draftId={} latencyMs={}", savedPost.id(), analysis.id(), finalDraft.id(), latency);
                return result;
            } else {
                draftCacheMiss.increment();
            }
            // 3. Content creation (no cached draft found)
            Map<String,Object> analysisMap = Map.of(
                "category", analysis.category(),
                "hook", analysis.hook(),
                "educationalPoint", analysis.educationalPoint(),
                "viralityScore", analysis.viralityScore()==null?0:analysis.viralityScore().intValue()
            );
            Map<String,Object> originalPostMap = Map.of("tag", firstHashtag(analysis.extractedHashtags()));
            Map<String,Object> draftMap = contentCreator.createContent(analysisMap, originalPostMap);

            @SuppressWarnings("unchecked")
            List<String> hashtags = (List<String>) draftMap.getOrDefault("hashtags", List.of());
            ContentDraft draft = new ContentDraft(null, analysis.id(),
                pickFocusService(analysis),
                safeString(draftMap.get("caption")).split("\n")[0], // first line as hook
                safeString(draftMap.get("caption")),
                hashtags,
                deriveCta(safeString(draftMap.get("caption"))),
                true,
                true,
                "", // notes
                Instant.now()
            );

            // 4. QA
            QAAgent.ValidationResult validation = qaAgent.validate(draft);
            finalDraft = new ContentDraft(draft.id(), draft.trendAnalysisId(), draft.focusService(), draft.hook(), draft.body(), draft.hashtags(), draft.callToAction(), true, validation.passed(), String.join("; ", validation.notes()), draft.createdAt());
            ContentDraft savedDraft = firestore.saveContentDraft(finalDraft);
            result.put("contentDraftId", savedDraft.id());
            result.put("qaPassed", validation.passed());
            result.put("qaNotes", validation.notes());

            long latency = System.currentTimeMillis() - start;
            workflowTimer.record(latency, java.util.concurrent.TimeUnit.MILLISECONDS);
            log.info("[workflow] postId={} analysisId={} draftId={} qaPassed={} latencyMs={}", savedPost.id(), analysis.id(), savedDraft.id(), validation.passed(), latency);
            return result;
        } catch (Exception e) {
            log.error("[workflow] failure msg={}", e.getMessage(), e);
            workflowErrorCounter.increment();
            result.put("error", e.getMessage());
            return result;
        }
    }

    private String firstHashtag(List<String> tags) { return (tags==null||tags.isEmpty())? "" : tags.get(0); }
    private String safeString(Object o){ return o==null?"": String.valueOf(o); }
    private String deriveCta(String caption) {
        // Heuristic: if caption already ends with a booking line keep it, else add standardized CTA
        String lower = caption.toLowerCase();
        if (lower.contains("book") || lower.contains("consult")) return extractLineWithKeyword(caption, "book", "consult");
        return "Book a physician consultation to see if you're a candidate – link in bio.";
    }
    private String extractLineWithKeyword(String caption, String... kws) {
        for (String line : caption.split("\n")) {
            String l = line.toLowerCase();
            for (String k : kws) if (l.contains(k)) return line.trim();
        }
        return "";
    }
    private String pickFocusService(TrendAnalysis analysis) {
        String all = (analysis.hook()+" "+analysis.educationalPoint()).toLowerCase();
        if (all.contains("skintyte")) return "SkinTyte";
        if (all.contains("ultherapy") || all.contains("duo")) return "Duo-C-Lift";
        if (all.contains("radiesse")) return "Radiesse";
        if (all.contains("vivier")) return "Vivier";
        return "General Aesthetics";
    }
}
