package com.mdaesthetics.viral.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
  private static final Logger log = LoggerFactory.getLogger(AiController.class);
  private final AiService aiService;

  public AiController(AiService aiService) {
    this.aiService = aiService;
  }

  @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest request) {
    long start = System.currentTimeMillis();
    try {
      String prompt = request.prompt();
      if (prompt == null || prompt.trim().isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "ok", false,
            "error", "ValidationError",
            "message", "Prompt must not be empty"
        ));
      }
      String response = aiService.chat(request.history(), request.prompt());
      long latency = System.currentTimeMillis() - start;
      log.debug("/chat latencyMs={} histSize={} promptChars={} respChars={}", latency, request.history() == null ? 0 : request.history().size(), request.prompt() == null ? 0 : request.prompt().length(), response == null ? 0 : response.length());
      return ResponseEntity.ok(Map.of(
          "ok", true,
          "latencyMs", latency,
          "response", response
      ));
    } catch (Exception e) {
      long latency = System.currentTimeMillis() - start;
      log.error("/chat failure latencyMs={} type={} msg={}", latency, e.getClass().getSimpleName(), e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
          "ok", false,
          "error", e.getClass().getSimpleName(),
          "message", e.getMessage(),
          "latencyMs", latency
      ));
    }
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("status", "UP");
  }

  @GetMapping("/test")
  public Map<String, Object> test() {
    boolean keyPresent = aiService.isKeyPresent();
    String model = aiService.getModelName();
    long started = System.currentTimeMillis();
    String probe;
    try {
      probe = aiService.diagnosticProbe();
    } catch (Exception e) {
      log.warn("Diagnostic probe failed", e);
      probe = "ERROR: " + e.getClass().getSimpleName() + ":" + e.getMessage();
    }
    long elapsed = System.currentTimeMillis() - started;
    return Map.of(
        "ok", true,
        "model", model,
        "keyPresent", keyPresent,
        "probe", probe,
        "ms", elapsed,
        "timestamp", System.currentTimeMillis()
    );
  }

  public record ChatRequest(List<String> history, String prompt) {}
}
