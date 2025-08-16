package com.google.adk.agents;

import com.google.adk.models.SocialMediaPost;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.adk.sessions.Session;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.artifacts.BaseArtifactService;
import com.google.adk.agents.InvocationContext;
import com.google.adk.models.LlmResponse;
import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class ViralForgeWorkflowTest {

    private InvocationContext createTestInvocationContext(Content content) {
        // Use nulls for services and session for test simplicity
        return InvocationContext.create(
            null, // BaseSessionService
            null, // BaseArtifactService
            "test-invocation-id",
            null, // BaseAgent
            Session.builder("test-session").appName("test-app").userId("test-user").build(),
            content,
            null // RunConfig
        );
    }

    private String getEventText(Event event) {
        return event.content().map(Content::text).orElse("");
    }

    @Test
    public void testFullWorkflow() throws Exception {
        // Setup test post as input text
        String inputText = "Check out our new SkinTyte treatment for firmer skin! #skintyte #torontoaesthetics";
        Content inputContent = Content.fromParts(Part.fromText(inputText));
        InvocationContext context = createTestInvocationContext(inputContent);

        // Initialize agents
        TrendAnalyzerAgent trendAnalyzer = new TrendAnalyzerAgent();
        ContentCreatorAgent contentCreator = new ContentCreatorAgent();
        ComplianceAgent complianceAgent = new ComplianceAgent();

        // Trend analysis
        Event trendEvent = trendAnalyzer.runLiveImpl(context).blockingFirst();
        assertNotNull(trendEvent);
        String trendText = getEventText(trendEvent);
        // Simulate extracting a SocialMediaPost from trend analysis (mocked)
        SocialMediaPost analyzed = new SocialMediaPost();
        analyzed.setCaption(trendText.isEmpty() ? inputText : trendText);
        analyzed.setHashtags(List.of("#skintyte", "#torontoaesthetics", "#mdaesthetics", "#duoclift", "#skintytetreatment"));
        analyzed.setPlatform("instagram");

        // Content creation
        Content contentInput = Content.fromParts(Part.fromText(analyzed.getCaption()));
        InvocationContext contentContext = createTestInvocationContext(contentInput);
        Event contentEvent = contentCreator.runLiveImpl(contentContext).blockingFirst();
        assertNotNull(contentEvent);
        String createdText = getEventText(contentEvent);
        SocialMediaPost created = new SocialMediaPost();
        created.setCaption(createdText.isEmpty() ? analyzed.getCaption() : createdText);
        created.setHashtags(analyzed.getHashtags());
        created.setPlatform(analyzed.getPlatform());

        assertTrue(created.getCaption().contains("SkinTyte"));
        assertFalse(created.getCaption().contains("Botox"));

    // Compliance check (now expects Content as input)
    Content complianceInput = Content.fromParts(Part.fromText(created.getCaption()));
    InvocationContext complianceContext = createTestInvocationContext(complianceInput);
    Event complianceEvent = complianceAgent.runLiveImpl(complianceContext).blockingFirst();
    assertNotNull(complianceEvent);
    // Simulate compliance checked by checking output event is not error
    assertFalse(complianceEvent.content().map(Content::text).orElse("").isEmpty());
    // Note: Email dispatcher would be mocked in a real test
    }

    @Test
    public void testComplianceFailure() {
        ComplianceAgent complianceAgent = new ComplianceAgent();
        String nonCompliantCaption = "Get Botox today!";
        Content complianceInput = Content.fromParts(Part.fromText(nonCompliantCaption));
        InvocationContext complianceContext = createTestInvocationContext(complianceInput);
        try {
            complianceAgent.runLiveImpl(complianceContext).blockingFirst();
            fail("Expected ComplianceException");
        } catch (Exception e) {
            boolean isCompliance = e instanceof ComplianceAgent.ComplianceException;
            boolean isCauseCompliance = e.getCause() instanceof ComplianceAgent.ComplianceException;
            assertTrue(isCompliance || isCauseCompliance, "Exception or its cause should be ComplianceException");
        }
    }
}