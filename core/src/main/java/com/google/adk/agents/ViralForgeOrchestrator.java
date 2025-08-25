package com.google.adk.agents;

import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;

public class ViralForgeOrchestrator extends BaseAgent {
    private final SequentialAgent orchestrator;

    public ViralForgeOrchestrator() {
        super(
            "viral_forge_orchestrator",
            "Orchestrates the full viral content workflow for MDAesthetics.",
            null,
            null,
            null
        );
        this.orchestrator = SequentialAgent.builder()
            .name("viral_forge_orchestrator_sequence")
            .description("Runs all sub-agents for the viral content workflow.")
            .subAgents(
                new TrendAnalyzerAgent(),
                new ContentCreatorAgent(),
                new ComplianceAgent(),
                new EmailDispatcher()
            )
            .build();
    }

    @Override
    protected Flowable<Event> runAsyncImpl(InvocationContext invocationContext) {
        return orchestrator.runAsync(invocationContext);
    }

    @Override
    protected Flowable<Event> runLiveImpl(InvocationContext invocationContext) {
        return orchestrator.runLive(invocationContext);
    }
}