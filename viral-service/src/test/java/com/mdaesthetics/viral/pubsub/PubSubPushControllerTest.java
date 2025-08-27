package com.mdaesthetics.viral.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.model.ContentDraft;
import com.mdaesthetics.viral.model.TrendAnalysis;
import com.mdaesthetics.viral.service.FirestoreAccessService;
import com.mdaesthetics.viral.service.ViralWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PubSubPushControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FirestoreAccessService firestore;

    @MockBean
    ViralWorkflowService workflow;

    private final ObjectMapper mapper = new ObjectMapper();

    private CompetitorPost samplePost;

    @BeforeEach
    void init() {
        samplePost = new CompetitorPost("p1", "instagram", "thelook", "https://ig/post/1", "Caption", List.of("#duoclift"), 10L,2L,0L,50L,0.1,0.2, Instant.now(), Instant.now());
        when(firestore.getCompetitorPost("p1")).thenReturn(Optional.of(samplePost));
        when(workflow.executePipeline(any())).thenReturn(Map.of(
            "trendAnalysisId", "ta-1",
            "contentDraftId", "cd-1",
            "qaPassed", true
        ));
    }

    @Test
    void analyzeNewPostHappyPath() throws Exception {
        String inner = mapper.writeValueAsString(Map.of("postId", "p1", "documentPath", "viral_research/2025-08-27/p1"));
        String b64 = Base64.getEncoder().encodeToString(inner.getBytes(StandardCharsets.UTF_8));
        String payload = mapper.writeValueAsString(Map.of(
            "message", Map.of(
                "data", b64,
                "messageId", "123",
                "attributes", Map.of("origin", "test")
            )
        ));
        mockMvc.perform(post("/pubsub/analyze-new-post").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk());
        Mockito.verify(workflow, Mockito.times(1)).executePipeline(samplePost);
    }
}
