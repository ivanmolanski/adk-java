package com.mdaesthetics.viral.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdaesthetics.viral.ai.OpenRouterClient;
import com.mdaesthetics.viral.model.TrendAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Synthesizes underlying themes across top TrendAnalysis results and proposes forward-looking angles.
 */
@Component
public class ProactiveThinkerAgent {
    private static final Logger log = LoggerFactory.getLogger(ProactiveThinkerAgent.class);
    
    private final OpenRouterClient openRouterClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INSTRUCTION = "You are a strategic medical aesthetics marketing strategist. " +
        "Given a JSON array of recent structured trend analyses, identify macro themes (prevention vs correction, natural look framing, collagen focus, combination therapy interest). " +
        "Then propose EXACTLY three innovative, forward-looking content angles that anticipate the next wave of patient interest for a physician-led clinic. " +
        "Output ONLY JSON: {\"themes\":[\"string\",\"string\",\"string\"], \"angles\":[{\"title\":\"string\", \"rationale\":\"string\", \"suggestedHook\":\"string\", \"pillar\":\"string\"}]}";

    public ProactiveThinkerAgent(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public Map<String,Object> synthesize(List<TrendAnalysis> topAnalyses) {
        String jsonArray = toJsonArray(topAnalyses);
        
        try {
            String prompt = INSTRUCTION + "\n\nAnalyze these trend analyses:\n" + jsonArray;
            String response = openRouterClient.chat(prompt, "z-ai/glm-4.5-air:free", 2.0);
            
            // Try to parse as structured JSON
            String json = extractJson(response);
            if (json != null) {
                try {
                    Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
                    if (parsed.containsKey("themes") && parsed.containsKey("angles")) {
                        log.info("Proactive synthesis successful, found {} themes and {} angles", 
                            ((List<?>) parsed.get("themes")).size(),
                            ((List<?>) parsed.get("angles")).size());
                        return parsed;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse proactive thinking JSON: {}", e.getMessage());
                }
            }
            
            // Return raw response if parsing fails
            return Map.of("raw", response);
            
        } catch (Exception e) {
            log.error("[proactive] failure msg={}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    private String toJsonArray(List<TrendAnalysis> list) {
        return "[" + list.stream().map(this::toJson).collect(Collectors.joining(",")) + "]";
    }
    
    private String q(String s){ 
        return "\"" + (s==null?"":s.replace("\\","\\\\").replace("\"","\\\"")) + "\""; 
    }
    
    private String toJson(TrendAnalysis ta) {
        return "{" +
            q("category")+":"+q(ta.category())+","+
            q("hook")+":"+q(ta.hook())+","+
            q("callToAction")+":"+q(ta.callToAction())+","+
            q("educationalPoint")+":"+q(ta.educationalPoint())+","+
            q("viralityScore")+":"+(ta.viralityScore()==null?0:ta.viralityScore())+","+
            q("relevanceScore")+":"+(ta.relevanceScore()==null?0:ta.relevanceScore())+"}";
    }
    
    private String extractJson(String text) {
        if (text == null) return null;
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end+1);
        }
        return null;
    }
}
