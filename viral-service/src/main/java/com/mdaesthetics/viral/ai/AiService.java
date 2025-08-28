package com.mdaesthetics.viral.ai;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {
  private static final Logger log = LoggerFactory.getLogger(AiService.class);

  private final GenAiProperties props;
  private final OpenRouterClient openRouterClient;
  private final Timer llmLatencyTimer;
  private final Counter llmSuccessCounter;
  private final Counter llmErrorCounter;

  public AiService(GenAiProperties props, OpenRouterClient openRouterClient, MeterRegistry meterRegistry) {
    this.props = props;
    this.openRouterClient = openRouterClient;
    if (props.apiKey() == null || props.apiKey().isBlank()) {
      log.warn("OPENROUTER_API_KEY not provided at startup; LLM calls will fail");
    }
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
    conversation.append("You are a clinically authoritative yet approachable assistant for a physician-led medical aesthetics practice. ");
    conversation.append("Keep answers concise, evidence-based, and aligned with professional standards. ");
    conversation.append("Avoid definitive medical diagnosis; encourage consultation.\n\n");
    
    if (history != null) {
      history.forEach(h -> conversation.append("User: ").append(h).append("\n"));
    }
    conversation.append("User: ").append(userPrompt).append("\nAssistant:");

    try {
      String response = openRouterClient.chat(conversation.toString(), "z-ai/glm-4.5-air:free", 2.0);
      
      long latency = System.currentTimeMillis() - start;
      llmLatencyTimer.record(latency, TimeUnit.MILLISECONDS);
      
      if (response.isBlank()) {
        log.info("[chat] id={} empty-response latencyMs={}", requestId, latency);
        llmSuccessCounter.increment();
        return "(No response generated)";
      }
      
      log.info("[chat] id={} success chars={} latencyMs={}", requestId, response.length(), latency);
      llmSuccessCounter.increment();
      return response;
      
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
      
      llmLatencyTimer.record(latency, TimeUnit.MILLISECONDS);
      llmErrorCounter.increment();
      return "Error(" + category + "): " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
    }
  }

  public boolean isKeyPresent() {
    return openRouterClient.isConfigured();
  }

  public String getModelName() {
    return "z-ai/glm-4.5-air:free";
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

