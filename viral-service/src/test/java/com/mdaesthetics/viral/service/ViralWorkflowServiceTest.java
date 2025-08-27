package com.mdaesthetics.viral.service;

import com.mdaesthetics.viral.agents.ContentCreatorAgent;
import com.mdaesthetics.viral.agents.QAAgent;
import com.mdaesthetics.viral.agents.TrendAnalyzerAgent;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.TrendAnalysis;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration-style unit test for ViralWorkflowService focusing on cache reuse behavior & metrics.
 * We mock FirestoreAccessService and the agents to avoid real external calls while exercising orchestration logic.
 */
public class ViralWorkflowServiceTest {

    private TrendAnalyzerAgent trendAnalyzer;
    private ContentCreatorAgent contentCreator;
    private QAAgent qaAgent;
    private FirestoreAccessService firestore;
    private MeterRegistry meterRegistry;
    private ViralWorkflowService service;

    @BeforeEach
    void setup() {
        trendAnalyzer = mock(TrendAnalyzerAgent.class);
        contentCreator = mock(ContentCreatorAgent.class);
        qaAgent = mock(QAAgent.class);
        firestore = mock(FirestoreAccessService.class);
        meterRegistry = new SimpleMeterRegistry();
        service = new ViralWorkflowService(trendAnalyzer, contentCreator, qaAgent, firestore, meterRegistry);
    }

    private CompetitorPost samplePost() {
        return new CompetitorPost(null, "instagram", "thelookaesthetics", "https://ig/post/1", "Caption", List.of("#duoclift"), 10L, 2L, 0L, 50L, 0.1, 0.2, Instant.now(), Instant.now());
    }

    @Test
    void firstRunCreatesAnalysisAndDraft_secondRunReusesBoth_andMetricsReflectCacheHits() {
        CompetitorPost input = samplePost();
        CompetitorPost saved = input.withId("post-1");

        // TrendAnalysis & Draft to be returned on first run
        TrendAnalysis ta = new TrendAnalysis("ta-1", saved.id(), "Process Demystified", "Hook", "CTA", "Education", List.of("#duoclift"), 0.9, 0.95, "{}", Instant.now());
        ContentDraft draft = new ContentDraft("draft-1", ta.id(), "Duo-C-Lift", "Hook", "Hook\nBody", List.of("#duoclift", "#torontoaesthetics"), "Book now", true, true, "", Instant.now());

        // QA result (passed)
        QAAgent.ValidationResult validation = new QAAgent.ValidationResult(true, List.of());

        // First run expectations: no existing analysis or draft
        when(firestore.saveCompetitorPost(any())).thenReturn(saved);
        when(firestore.findLatestTrendAnalysisForCompetitorPost(saved.id())).thenReturn(Optional.empty());
        when(trendAnalyzer.analyze(saved)).thenReturn(ta);
        when(firestore.saveTrendAnalysis(ta)).thenReturn(ta);
        when(firestore.findLatestContentDraftForTrendAnalysis(ta.id())).thenReturn(Optional.empty());
        when(contentCreator.createContent(anyMap(), anyMap())).thenReturn(Map.of(
            "caption", draft.body(),
            "hashtags", draft.hashtags()
        ));
        when(qaAgent.validate(any())).thenReturn(validation);
        when(firestore.saveContentDraft(any())).thenReturn(draft);

        Map<String,Object> first = service.executePipeline(input);
        assertEquals("draft-1", first.get("contentDraftId"));
        assertEquals(0.0, meterRegistry.counter("trendAnalysis.cache.hit").count());
        assertEquals(1.0, meterRegistry.counter("trendAnalysis.cache.miss").count());
        assertEquals(0.0, meterRegistry.counter("contentDraft.cache.hit").count());
        assertEquals(1.0, meterRegistry.counter("contentDraft.cache.miss").count());

    // Second run expectations: existing analysis & draft reused => hits increment
    reset(firestore); // reset interactions; keep meterRegistry state
    when(firestore.saveCompetitorPost(any())).thenReturn(saved); // re-stub after reset
    when(firestore.findLatestTrendAnalysisForCompetitorPost(saved.id())).thenReturn(Optional.of(ta));
    when(firestore.findLatestContentDraftForTrendAnalysis(ta.id())).thenReturn(Optional.of(draft));

        Map<String,Object> second = service.executePipeline(input);
        assertEquals("draft-1", second.get("contentDraftId"));
        assertEquals(1.0, meterRegistry.counter("trendAnalysis.cache.hit").count());
        assertEquals(1.0, meterRegistry.counter("trendAnalysis.cache.miss").count());
        assertEquals(1.0, meterRegistry.counter("contentDraft.cache.hit").count());
        assertEquals(1.0, meterRegistry.counter("contentDraft.cache.miss").count());

        // Ensure analyzer & contentCreator not invoked second time
        verify(trendAnalyzer, times(1)).analyze(saved);
        verify(contentCreator, times(1)).createContent(anyMap(), anyMap());

        // Ensure draft not saved during second run (only initial run). Because we reset the mock, we only see second run interactions here: no saveContentDraft
        verify(firestore, never()).saveContentDraft(any());
    }
}
