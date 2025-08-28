package com.mdaesthetics.viral.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenRouter API client for making chat completion requests.
 * Replaces the Google Gemini API with OpenRouter's API endpoint.
 */
@Component
public class OpenRouterClient {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenRouterClient.class);
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "z-ai/glm-4.5-air:free";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    @Value("${app.site.url:https://mdaesthetics.ca}")
    private String siteUrl;
    
    @Value("${app.site.name:MD Aesthetics Viral Forge}")
    private String siteName;
    
    public OpenRouterClient(@Value("${openrouter.api.key:#{null}}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    /**
     * Send a chat completion request to OpenRouter API
     */
    public OpenRouterResponse chatCompletion(OpenRouterRequest request) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenRouter API key not configured");
        }
        
        String jsonBody = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(OPENROUTER_BASE_URL))
            .header("Authorization", "Bearer " + apiKey)
            .header("HTTP-Referer", siteUrl)
            .header("X-Title", siteName)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
            
        logger.debug("Sending OpenRouter request: model={}, messages={}", 
            request.getModel(), request.getMessages().size());
            
        HttpResponse<String> response = httpClient.send(httpRequest, 
            HttpResponse.BodyHandlers.ofString());
            
        if (response.statusCode() != 200) {
            logger.error("OpenRouter API error: status={}, body={}", 
                response.statusCode(), response.body());
            throw new RuntimeException("OpenRouter API error: " + response.statusCode() + " - " + response.body());
        }
        
        return objectMapper.readValue(response.body(), OpenRouterResponse.class);
    }
    
    /**
     * Simple chat method with just a prompt
     */
    public String chat(String prompt) {
        return chat(prompt, DEFAULT_MODEL, 2.0);
    }
    
    /**
     * Chat with custom model and temperature
     */
    public String chat(String prompt, String model, double temperature) {
        try {
            OpenRouterRequest request = OpenRouterRequest.builder()
                .model(model)
                .temperature(temperature)
                .messages(List.of(
                    new OpenRouterMessage("user", prompt)
                ))
                .build();
                
            OpenRouterResponse response = chatCompletion(request);
            
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                OpenRouterChoice choice = response.getChoices().get(0);
                if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                    return choice.getMessage().getContent();
                }
            }
            
            logger.warn("OpenRouter returned no content in response");
            return "No response generated";
            
        } catch (Exception e) {
            logger.error("OpenRouter chat failed: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
    
    // Data classes for OpenRouter API
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OpenRouterRequest {
        private String model;
        private List<OpenRouterMessage> messages;
        private Double temperature;
        private Integer maxTokens;
        private Boolean stream;
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters and setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<OpenRouterMessage> getMessages() { return messages; }
        public void setMessages(List<OpenRouterMessage> messages) { this.messages = messages; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
        public Boolean getStream() { return stream; }
        public void setStream(Boolean stream) { this.stream = stream; }
        
        public static class Builder {
            private final OpenRouterRequest request = new OpenRouterRequest();
            
            public Builder model(String model) { request.setModel(model); return this; }
            public Builder messages(List<OpenRouterMessage> messages) { request.setMessages(messages); return this; }
            public Builder temperature(double temperature) { request.setTemperature(temperature); return this; }
            public Builder maxTokens(int maxTokens) { request.setMaxTokens(maxTokens); return this; }
            public Builder stream(boolean stream) { request.setStream(stream); return this; }
            public OpenRouterRequest build() { return request; }
        }
    }
    
    public static class OpenRouterMessage {
        private String role;
        private String content;
        
        public OpenRouterMessage() {}
        
        public OpenRouterMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class OpenRouterResponse {
        private String id;
        private String object;
        private long created;
        private String model;
        private List<OpenRouterChoice> choices;
        private Usage usage;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }
        public long getCreated() { return created; }
        public void setCreated(long created) { this.created = created; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public List<OpenRouterChoice> getChoices() { return choices; }
        public void setChoices(List<OpenRouterChoice> choices) { this.choices = choices; }
        public Usage getUsage() { return usage; }
        public void setUsage(Usage usage) { this.usage = usage; }
    }
    
    public static class OpenRouterChoice {
        private int index;
        private OpenRouterMessage message;
        private String finishReason;
        
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public OpenRouterMessage getMessage() { return message; }
        public void setMessage(OpenRouterMessage message) { this.message = message; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }
    
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }
}