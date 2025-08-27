package com.mdaesthetics.viral.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import com.mdaesthetics.viral.service.FirestoreAccessService;
import com.mdaesthetics.viral.service.ViralAggregationService;
import com.mdaesthetics.viral.service.ViralWorkflowService;
import com.mdaesthetics.viral.model.TrendAnalysis;
import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.PaginatedResult;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ViralAnalysisController.class)
@WithMockUser
@Import(TestConfig.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
public class ViralAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FirestoreAccessService firestoreAccessService;

    @MockBean
    private ViralAggregationService viralAggregationService;

    @MockBean
    private ViralWorkflowService viralWorkflowService;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter trendsListCounter;
    private Counter draftsListCounter;
    private Counter trendsDetailCounter;
    private Counter draftsDetailCounter;

    @BeforeEach
    void setUp() {
        // Get the mocked counters from the registry
        trendsListCounter = meterRegistry.counter("api.trends.list.count");
        draftsListCounter = meterRegistry.counter("api.drafts.list.count");
        trendsDetailCounter = meterRegistry.counter("api.trends.detail.count");
        draftsDetailCounter = meterRegistry.counter("api.drafts.detail.count");

        // Reset all counters before each test
        reset(trendsListCounter, draftsListCounter, trendsDetailCounter, draftsDetailCounter);
    }

    @Test
    void trendsEndpointReturnsSuccessEnvelope() throws Exception {
        // Given
        List<TrendAnalysis> mockTrends = List.of(
            new TrendAnalysis("1", "post1", "Process Demystified", "Amazing transformation", "Book now", "Learn about the process", List.of("#skintyte"), 0.8, 0.9, "raw json", Instant.now())
        );
        PaginatedResult<TrendAnalysis> mockResult = new PaginatedResult<>(mockTrends, "next-cursor", false);

        when(firestoreAccessService.listTrendAnalysesWithCursor(10, "")).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/viral/trends")
                .param("cursor", "")
                .param("limit", "10")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.trends").isArray())
                .andExpect(jsonPath("$.pagination.hasMore").value(false));

        verify(trendsListCounter).increment();
    }

    @Test
    void trendsEndpointReturnsFallbackEnvelopeWhenMissing() throws Exception {
        // Given
        PaginatedResult<TrendAnalysis> emptyResult = new PaginatedResult<>(List.of(), null, false);
        when(firestoreAccessService.listTrendAnalysesWithCursor(10, "")).thenReturn(emptyResult);

        // When & Then
        mockMvc.perform(get("/api/viral/trends")
                .param("cursor", "")
                .param("limit", "10")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.trends").isArray());

        verify(trendsListCounter).increment();
    }

    @Test
    void draftsEndpointReturnsSuccessEnvelope() throws Exception {
        // Given
        List<ContentDraft> mockDrafts = List.of(
            new ContentDraft("1", "analysis1", "SkinTyte", "Amazing results", "Book your consultation", List.of("#skintyte"), "Contact us", true, true, "Compliant", Instant.now())
        );
        PaginatedResult<ContentDraft> mockResult = new PaginatedResult<>(mockDrafts, "next-cursor", false);

        when(firestoreAccessService.listContentDraftsWithCursor(5, "")).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/viral/drafts")
                .param("cursor", "")
                .param("limit", "5")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.drafts").isArray())
                .andExpect(jsonPath("$.pagination.hasMore").value(false));

        verify(draftsListCounter).increment();
    }

    @Test
    void draftsEndpointReturnsFallbackEnvelopeWhenMissing() throws Exception {
        // Given
        PaginatedResult<ContentDraft> emptyResult = new PaginatedResult<>(List.of(), null, false);
        when(firestoreAccessService.listContentDraftsWithCursor(5, "")).thenReturn(emptyResult);

        // When & Then
        mockMvc.perform(get("/api/viral/drafts")
                .param("cursor", "")
                .param("limit", "5")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.drafts").isArray());

        verify(draftsListCounter).increment();
    }

    @Test
    void trendDetailEndpointReturnsSuccessEnvelopeWhenFound() throws Exception {
        // Given
        when(viralAggregationService.buildTrendDetail("1")).thenReturn(Optional.of(mock(com.mdaesthetics.viral.dto.TrendDetailDto.class)));

        // When & Then
        mockMvc.perform(get("/api/viral/trends/1")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(trendsDetailCounter).increment();
    }

    @Test
    void trendDetailEndpointReturnsFallbackEnvelopeWhenMissing() throws Exception {
        // Given
        when(viralAggregationService.buildTrendDetail("1")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/viral/trends/1")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("fallback"));

        verify(trendsDetailCounter).increment();
    }

    @Test
    void draftDetailEndpointReturnsSuccessEnvelopeWhenFound() throws Exception {
        // Given
        when(viralAggregationService.buildDraftDetail("1")).thenReturn(Optional.of(mock(com.mdaesthetics.viral.dto.DraftDetailDto.class)));

        // When & Then
        mockMvc.perform(get("/api/viral/drafts/1")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(draftsDetailCounter).increment();
    }

    @Test
    void draftDetailEndpointReturnsFallbackEnvelopeWhenMissing() throws Exception {
        // Given
        when(viralAggregationService.buildDraftDetail("1")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/viral/drafts/1")
                .with(user("test").password("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("fallback"));

        verify(draftsDetailCounter).increment();
    }
}