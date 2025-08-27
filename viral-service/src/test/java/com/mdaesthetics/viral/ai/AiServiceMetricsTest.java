package com.mdaesthetics.viral.ai;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies Micrometer metrics instrumentation in AiService without spinning full Spring context.
 */
class AiServiceMetricsTest {

    private MeterRegistry registry;
    private AiService aiService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        GenAiProperties props = new GenAiProperties("dummy-key");
        aiService = new AiService(props, registry);
    }

    @Test
    @DisplayName("LLM success increments success counter and records latency")
    void successIncrementsCounter() {
        double beforeSuccess = registry.counter("llm.call.success").count();
        double beforeError = registry.counter("llm.call.error").count();
        double beforeLatencyCount = registry.timer("llm.call.latency").count();

        String resp = aiService.chat(List.of(), "Test prompt for metrics");
        assertThat(resp).isNotBlank();

    double afterSuccess = registry.find("llm.call.success").counter().count();
    double afterError = registry.find("llm.call.error").counter().count();
    double afterLatency = registry.find("llm.call.latency").timer().count();
    assertThat(afterSuccess).as("success counter increment").isEqualTo(beforeSuccess + 1.0);
    assertThat(afterError).as("error counter unchanged").isEqualTo(beforeError);
    assertThat(afterLatency).as("latency timer increment").isEqualTo(beforeLatencyCount + 1.0);
    }

    @Test
    @DisplayName("Empty prompt returns validation message without invoking LLM (no counters increment)")
    void emptyPromptNoInvocation() {
        double beforeSuccess = registry.counter("llm.call.success").count();
        double beforeError = registry.counter("llm.call.error").count();
        double beforeLatency = registry.timer("llm.call.latency").count();

        String resp = aiService.chat(List.of(), "   ");
        assertThat(resp).isEqualTo("Prompt cannot be empty");

    double afterSuccess = registry.find("llm.call.success").counter().count();
    double afterError = registry.find("llm.call.error").counter().count();
    double afterLatency = registry.find("llm.call.latency").timer().count();
    assertThat(afterSuccess).isEqualTo(beforeSuccess);
    assertThat(afterError).isEqualTo(beforeError);
    assertThat(afterLatency).isEqualTo(beforeLatency);
    }
}
