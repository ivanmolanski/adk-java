package com.mdaesthetics.viral.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdaesthetics.viral.ai.OpenRouterClient;
import com.mdaesthetics.viral.model.CompetitorPost;
import com.mdaesthetics.viral.model.TrendAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Analyzes a competitor post into structured JSON per business categories.
 */
@Service
public class TrendAnalyzerAgent {
    private static final Logger log = LoggerFactory.getLogger(TrendAnalyzerAgent.class);
    
    private final OpenRouterClient openRouterClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INSTRUCTION = "You are a social media analyst for a high-end medical aesthetics practice. " +
        "Given a JSON object describing a competitor post, classify it and extract structured insights. " +
        "Output ONLY valid JSON with this exact schema: {" +
        "\"category\": one of [\"Process Demystified\",\"Science Explained\",\"Transformation\",\"Myth Busting\"]," +
        "\"hook\": string (best 3-second opening hook)," +
        "\"callToAction\": string or empty if none," +
        "\"educationalPoint\": concise clinical point conveyed," +
        "\"extractedHashtags\": string array of 3-12 most strategic hashtags (lowercase, no duplicates)," +
        "\"viralityScore\": number 0-1 (heuristic)," +
        "\"relevanceScore\": number 0-1 (how aligned to our pillars: Duo-C-Lift, SkinTyte, Radiesse, body firming). } " +
        "Do not include markdown or commentary. If insufficient data, infer conservatively.";

    public TrendAnalyzerAgent(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public TrendAnalysis analyze(CompetitorPost post) {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();
        String inputJson = toInputJson(post);
        
        try {
            String prompt = INSTRUCTION + "\n\nAnalyze this post:\n" + inputJson;
            String response = openRouterClient.chat(prompt, "z-ai/glm-4.5-air:free", 2.0);
            
            TrendAnalysis mapped = mapRaw(response, post.id());
            long latency = System.currentTimeMillis() - start;
            log.info("[trend] id={} postId={} latencyMs={} category={} virality={} relevance={}", 
                requestId, post.id(), latency, mapped.category(), mapped.viralityScore(), mapped.relevanceScore());
            return mapped;
            
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[trend] id={} failure latencyMs={} msg={}", requestId, latency, e.getMessage(), e);
            return new TrendAnalysis(null, post.id(), "Unknown", "", "", "", List.of(), 0.0, 0.0, 
                "ERROR:"+e.getMessage(), Instant.now());
        }
    }

    private String toInputJson(CompetitorPost p) {
        return "{" +
            quote("platform")+":"+quote(p.platform())+"," +
            quote("profile")+":"+quote(p.profile())+"," +
            quote("postUrl")+":"+quote(p.postUrl())+"," +
            quote("caption")+":"+escape(p.caption())+"," +
            quote("hashtags")+":"+toJsonArray(p.hashtags())+"," +
            quote("likes")+":"+(p.likes()==null?0:p.likes())+"," +
            quote("comments")+":"+(p.comments()==null?0:p.comments())+"," +
            quote("shares")+":"+(p.shares()==null?0:p.shares())+"," +
            quote("views")+":"+(p.views()==null?0:p.views())+"}";
    }

    private String quote(String s){return "\""+s+"\"";}
    private String escape(String s){ 
        if(s==null) return ""; 
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," "); 
    }
    private String toJsonArray(List<String> list){ 
        if(list==null||list.isEmpty()) return "[]"; 
        return "["+String.join(",", list.stream().map(v->quote(v)).toList())+"]"; 
    }

    private TrendAnalysis mapRaw(String raw, String competitorPostId) {
        try {
            // Try to parse as JSON first
            JsonNode jsonNode = objectMapper.readTree(raw);
            
            String category = getStringFromJson(jsonNode, "category");
            String hook = getStringFromJson(jsonNode, "hook");
            String cta = getStringFromJson(jsonNode, "callToAction");
            String educational = getStringFromJson(jsonNode, "educationalPoint");
            List<String> hashtags = getArrayFromJson(jsonNode, "extractedHashtags");
            Double virality = getDoubleFromJson(jsonNode, "viralityScore");
            Double relevance = getDoubleFromJson(jsonNode, "relevanceScore");
            
            return new TrendAnalysis(null, competitorPostId, category, hook, cta, educational, 
                hashtags, virality, relevance, raw, Instant.now());
                
        } catch (Exception e) {
            log.warn("Failed to parse JSON response, falling back to regex extraction", e);
            // Fallback to regex extraction
            String category = extractString(raw, "category");
            String hook = extractString(raw, "hook");
            String cta = extractString(raw, "callToAction");
            String educational = extractString(raw, "educationalPoint");
            List<String> hashtags = extractArray(raw, "extractedHashtags");
            Double virality = extractNumber(raw, "viralityScore");
            Double relevance = extractNumber(raw, "relevanceScore");
            return new TrendAnalysis(null, competitorPostId, category, hook, cta, educational, 
                hashtags, virality, relevance, raw, Instant.now());
        }
    }
    
    private String getStringFromJson(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asText("") : "";
    }
    
    private Double getDoubleFromJson(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null ? fieldNode.asDouble(0.0) : 0.0;
    }
    
    private List<String> getArrayFromJson(JsonNode node, String field) {
        JsonNode arrayNode = node.get(field);
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        
        List<String> result = new ArrayList<>();
        for (JsonNode element : arrayNode) {
            result.add(element.asText());
        }
        return result;
    }

    // Fallback regex methods
    private String extractString(String json, String field) {
        String marker = "\""+field+"\""; 
        int idx = json.indexOf(marker); 
        if(idx<0) return ""; 
        int colon = json.indexOf(':', idx); 
        if(colon<0) return ""; 
        int q1 = json.indexOf('"', colon+1); 
        if(q1<0) return ""; 
        int q2 = json.indexOf('"', q1+1); 
        if(q2<0) return ""; 
        return json.substring(q1+1,q2).trim();
    }
    
    private List<String> extractArray(String json, String field) {
        String marker = "\""+field+"\""; 
        int idx = json.indexOf(marker); 
        if(idx<0) return List.of(); 
        int colon = json.indexOf(':', idx); 
        int lb = json.indexOf('[', colon); 
        int rb = json.indexOf(']', lb); 
        if(lb<0||rb<0) return List.of(); 
        String inner = json.substring(lb+1, rb).trim(); 
        if(inner.isEmpty()) return List.of(); 
        String[] parts = inner.split(","); 
        return List.of(parts).stream()
            .map(s->s.replace("\""," ").trim())
            .filter(s->!s.isBlank())
            .toList();
    }
    
    private Double extractNumber(String json, String field) { 
        try { 
            String marker = "\""+field+"\""; 
            int idx = json.indexOf(marker); 
            if(idx<0) return 0.0; 
            int colon = json.indexOf(':', idx); 
            int end = json.indexOf(',', colon+1); 
            if(end<0) end = json.indexOf('}', colon+1); 
            String num = json.substring(colon+1,end).replaceAll("[^0-9.]+"," ").trim(); 
            if(num.isBlank()) return 0.0; 
            return Double.parseDouble(num); 
        } catch(Exception e){ 
            return 0.0; 
        } 
    }
}