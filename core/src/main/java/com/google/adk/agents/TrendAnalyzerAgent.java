package com.google.adk.agents;

import com.google.adk.events.Event;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Flowable;
import java.util.Collections;

public class TrendAnalyzerAgent extends LlmAgent {
    private static final String SYSTEM_INSTRUCTION = """
        You are a social media analyst for a high-end medical practice. Analyze the provided post data and:
        1. Identify the content category: 'Process Demystified', 'Science Explained', 'Transformation', or 'Myth Busting'
        2. Extract the 3-second hook
        3. Identify the call-to-action
        4. Note the core educational point
        5. Calculate engagement score (likes + comments)/hours_since_posted
        Return as structured JSON with these fields:
        {category, hook, cta, educational_point, engagement_score}
        """;

    public TrendAnalyzerAgent() {
        super(
            LlmAgent.builder()
                .name("trend_analyzer")
                .description("Analyzes social media trends for MDAesthetics.")
                .model("gemini-2.5-flash")
                .instruction(SYSTEM_INSTRUCTION)
                .subAgents(Collections.emptyList())
                .disallowTransferToParent(false)
                .disallowTransferToPeers(false)
        );
    }

    @Override
    protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
        return runTrendAnalysis(invocationContext);
    }

    @Override
    protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
        return runTrendAnalysis(invocationContext);
    }

    private Flowable<Event> runTrendAnalysis(InvocationContext invocationContext) {
        // Placeholder for actual LLM invocation logic.
        Content content = Content.builder().build();
        Event event = Event.builder()
            .id(Event.generateEventId())
            .author(name())
            .content(content)
            .invocationId(invocationContext.invocationId())
            .branch(invocationContext.branch())
            .build();
        return Flowable.just(event);
    }
}