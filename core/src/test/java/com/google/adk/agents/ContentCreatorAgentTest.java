package com.google.adk.agents;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.adk.sessions.Session;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContentCreatorAgentTest {

    private InvocationContext createTestInvocationContext(Content content) {
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

    private String getOutputText(Event event) {
        return event.content().map(Content::text).orElse("");
    }

    @Test
    void testContentCreationWithBrandRules() throws Exception {
        ContentCreatorAgent agent = new ContentCreatorAgent();
        String input = "Create a post about our Botox treatments";
        Content content = Content.fromParts(Part.fromText(input));
        InvocationContext context = createTestInvocationContext(content);
        Event event = agent.runLiveImpl(context).blockingFirst();
        assertNotNull(event);
        String output = getOutputText(event);
        // Verify compliance rules
        assertFalse(output.contains("Botox"));
        assertTrue(output.contains("Tox") || output.contains("Neuromodulator"));
        // Verify required hashtags
        assertTrue(output.contains("#torontoaesthetics"));
        assertTrue(output.contains("#mdaesthetics"));
        // Verify JSON structure
        assertTrue(output.contains("\"platform\""));
        assertTrue(output.contains("\"caption\""));
        assertTrue(output.contains("\"hashtags\""));
    }

    @Test
    void testServiceFocus() throws Exception {
        ContentCreatorAgent agent = new ContentCreatorAgent();
        String input = "Create a promotional post";
        Content content = Content.fromParts(Part.fromText(input));
        InvocationContext context = createTestInvocationContext(content);
        Event event = agent.runLiveImpl(context).blockingFirst();
        String output = getOutputText(event);
        // Verify focus on core services
        assertTrue(
            output.contains("Duo-C-Lift") ||
            output.contains("SkinTyte") ||
            output.contains("Radiesse") ||
            output.contains("Vivier")
        );
    }

    @Test
    void testClinicalTone() throws Exception {
        ContentCreatorAgent agent = new ContentCreatorAgent();
        String input = "Explain a treatment";
        Content content = Content.fromParts(Part.fromText(input));
        InvocationContext context = createTestInvocationContext(content);
        Event event = agent.runLiveImpl(context).blockingFirst();
        String output = getOutputText(event);
        // Verify professional tone
        assertFalse(output.contains("awesome"));
        assertFalse(output.contains("amazing"));
        assertTrue(output.contains("clinical") || output.contains("results"));
    }
}