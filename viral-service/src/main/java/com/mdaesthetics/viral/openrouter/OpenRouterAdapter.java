package com.mdaesthetics.viral.openrouter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenRouterAdapter {
  private static final Logger log = LoggerFactory.getLogger(OpenRouterAdapter.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  public String generateText(String modelName, String prompt) throws Exception {
    String key = System.getenv("OPENROUTER_API_KEY");
    if (key == null || key.isBlank()) {
      throw new IllegalStateException("OPENROUTER_API_KEY not configured");
    }

    String endpoint = "https://api.openrouter.ai/v1/outputs"; // generic outputs endpoint

    // Build minimal request body for text generation
    Map<String,Object> body = Map.of(
      "model", modelName,
      "input", prompt
    );

    byte[] payload = mapper.writeValueAsBytes(body);

    URL url = new URL(endpoint);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    conn.setRequestProperty("Authorization", "Bearer " + key);
    conn.setConnectTimeout(15000);
    conn.setReadTimeout(30000);

    try (OutputStream os = conn.getOutputStream()) {
      os.write(payload);
      os.flush();
    }

    int code = conn.getResponseCode();
    InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int r;
    while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
    String respText = baos.toString(StandardCharsets.UTF_8);
    if (code < 200 || code >= 300) {
      log.warn("OpenRouter API returned non-2xx: {} -> {}", code, respText);
      throw new RuntimeException("OpenRouter API error: " + code + " " + respText);
    }

    // Try to parse response and extract text. The exact schema may vary; attempt best-effort
    try {
      Map<String, Object> resp = mapper.readValue(respText, Map.class);
      // try common paths: 'output' or 'results'...
      if (resp.containsKey("output")) {
        Object out = resp.get("output");
        if (out instanceof String) return (String) out;
        if (out instanceof Map) {
          Object txt = ((Map) out).get("content");
          if (txt instanceof String) return (String) txt;
        }
      }

      if (resp.containsKey("results")) {
        Object results = resp.get("results");
        if (results instanceof Iterable) {
          for (Object r0 : (Iterable<?>) results) {
            if (r0 instanceof Map) {
              Map<String,Object> rmap = (Map<String,Object>) r0;
              Object out = rmap.get("output");
              if (out instanceof String) return (String) out;
              if (out instanceof Map) {
                Map<String,Object> outMap = (Map<String,Object>) out;
                Object content = outMap.get("content");
                if (content instanceof String) return (String) content;
              }
            }
          }
        }
      }

    } catch (Exception e) {
      log.debug("Failed to parse OpenRouter response JSON, returning raw text", e);
    }

    return respText;
  }
}
