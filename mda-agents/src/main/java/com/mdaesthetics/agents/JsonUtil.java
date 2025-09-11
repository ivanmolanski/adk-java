package com.mdaesthetics.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/** Lightweight JSON helpers (avoid external utility libs). */
public final class JsonUtil {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private JsonUtil() {}

  public static String toJson(Object o) {
    try {
      return MAPPER.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String mergeAnalyses(String analysisJson, Map<String, Object> businessContext) {
    Map<String, Object> root = new HashMap<>();
    root.put("analysis", analysisJson);
    root.put("business_context", businessContext);
    return toJson(root);
  }

  public static String wrapPipeline(String analysis, String draft, String qa) {
    Map<String, Object> root = new HashMap<>();
    root.put("analysis", analysis);
    root.put("draft", draft);
    root.put("qa", qa);
    return toJson(root);
  }

  public static String composeEmailInput(String analysesJson, String draftsJson, String ideasJson) {
    Map<String, Object> root = new HashMap<>();
    root.put("analyses", analysesJson);
    root.put("drafts", draftsJson);
    root.put("ideas", ideasJson);
    return toJson(root);
  }
}
