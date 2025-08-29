package com.mdaesthetics.viral.ai;

import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.events.Event;
import com.google.adk.sessions.InMemorySessionService;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AiService {
  private static final Logger log = LoggerFactory.getLogger(AiService.class);

  private final GenAiProperties props;
  private final LlmAgent agent;
  private final Timer llmLatencyTimer;
  private final Counter llmSuccessCounter;
  private final Counter llmErrorCounter;
  private final InMemorySessionService sessionService = new InMemorySessionService();
  private final InMemoryArtifactService artifactService = new InMemoryArtifactService();

  public AiService(GenAiProperties props, MeterRegistry meterRegistry) {
    this.props = props;
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      log.warn("OPENROUTER_API_KEY not provided at startup; LLM calls will fallback");
    }
  this.agent = LlmAgent.builder()
    .name("ai_chat")
    .description("General chat endpoint for MDAesthetics Virality system")
    .model("openrouter-glm-4.5-air")
        .instruction("You are a clinically authoritative yet approachable assistant for a physician-led medical aesthetics practice. Keep answers concise, evidence-based, and aligned with professional standards. Avoid definitive medical diagnosis; encourage consultation.")
        .build();
    this.llmLatencyTimer = meterRegistry.timer("llm.call.latency");
    this.llmSuccessCounter = meterRegistry.counter("llm.call.success");
    this.llmErrorCounter = meterRegistry.counter("llm.call.error");
  }

  public String chat(List<String> history, String userPrompt) {
    long start = System.currentTimeMillis();
    String requestId = UUID.randomUUID().toString();
    if (userPrompt == null || userPrompt.isBlank()) {
      return "Prompt cannot be empty";
    }
    log.debug("[chat] id={} hist={} promptChars={}", requestId, history == null ? 0 : history.size(), userPrompt.length());
    StringBuilder conversation = new StringBuilder();
    if (history != null) {
      history.forEach(h -> conversation.append("User: ").append(h).append("\n"));
    }
    conversation.append("User: ").append(userPrompt).append("\nAssistant:");

  try {
      InvocationContext ctx = InvocationContext.create(
          sessionService,
          artifactService,
          "chat-" + System.currentTimeMillis(),
          agent,
          sessionService.createSession("mdaesthetics", "ai-chat").blockingGet(),
          Content.fromParts(Part.fromText(conversation.toString())),
          RunConfig.builder().build()
      );

      StringBuilder response = new StringBuilder();
    Flowable<Event> stream = agent.runAsync(ctx)
      .timeout(25, TimeUnit.SECONDS)
          .doOnError(err -> log.warn("[chat] id={} stream error type={} msg={}", requestId, err.getClass().getSimpleName(), err.getMessage()));

      stream.blockingSubscribe(event -> event.content().ifPresent(c -> c.parts().ifPresent(parts -> parts.forEach(p -> p.text().ifPresent(response::append)))));

      String out = response.toString().trim();
      long latency = System.currentTimeMillis() - start;
      llmLatencyTimer.record(latency, java.util.concurrent.TimeUnit.MILLISECONDS);
      if (out.isBlank()) {
        log.info("[chat] id={} empty-response latencyMs={}", requestId, latency);
        llmSuccessCounter.increment();
        return "(No response generated)";
      }
      log.info("[chat] id={} success chars={} latencyMs={}", requestId, out.length(), latency);
      llmSuccessCounter.increment();
      return out;
    } catch (Throwable t) {
      long latency = System.currentTimeMillis() - start;
      log.error("[chat] id={} failure latencyMs={} type={} msg={}", requestId, latency, t.getClass().getSimpleName(), t.getMessage(), t);
      String category;
      if (t.getMessage() != null && t.getMessage().contains("quota")) {
        category = "QuotaExceeded";
      } else if (t.getMessage() != null && t.getMessage().toLowerCase().contains("unauthorized")) {
        category = "AuthError";
      } else {
        category = "GeneralFailure";
      }
      llmLatencyTimer.record(latency, java.util.concurrent.TimeUnit.MILLISECONDS);
      llmErrorCounter.increment();
      return "Error(" + category + "): " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
    }
  }

  public boolean isKeyPresent() {
    return props.apiKey() != null && !props.apiKey().isBlank();
  }

  public String getModelName() {
    return "openrouter-glm-4.5-air";
  }

  public String diagnosticProbe() {
    // Lightweight probe without consuming large quota; ask for a 1-word reply.
    String reply = chat(List.of(), "Respond with only the word READY.");
    if (reply.length() > 30) {
      return reply.substring(0, 30) + "...";
    }
    return reply;
  }
}

