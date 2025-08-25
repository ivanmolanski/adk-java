package com.mdaesthetics.viral.config;

import com.google.adk.agents.LlmAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for LLM agents that integrates with Secret Manager for production deployment.
 * 
 * This configuration ensures that all ADK LlmAgent instances are properly configured
 * with the Gemini API key retrieved from Google Cloud Secret Manager.
 */
@Configuration
@Profile("production") // Only active in production mode
public class LlmAgentConfig {
    
    @Autowired
    private SecretManagerConfig secretManagerConfig;
    
    /**
     * Creates a configured TrendAnalyzer LlmAgent with the proper API key
     */
    @Bean("trendAnalyzerLlmAgent")
    public LlmAgent createTrendAnalyzerAgent() {
        // Set the API key from Secret Manager
        System.setProperty("GEMINI_API_KEY", secretManagerConfig.getSecret("GEMINI_API_KEY"));
        
        return LlmAgent.builder()
            .name("trend_analyzer")
            .description("Classifies competitor posts and extracts structured viral drivers")
            .model("gemini-2.5-flash")
            .instruction(getTrendAnalyzerInstruction())
            .build();
    }
    
    /**
     * Creates a configured ContentCreator LlmAgent with the proper API key
     */
    @Bean("contentCreatorLlmAgent")
    public LlmAgent createContentCreatorAgent() {
        // Set the API key from Secret Manager
        System.setProperty("GEMINI_API_KEY", secretManagerConfig.getSecret("GEMINI_API_KEY"));
        
        return LlmAgent.builder()
            .name("content_creator")
            .description("Generates superior MD Aesthetics content based on viral analysis")
            .model("gemini-2.5-flash")
            .instruction(getContentCreatorInstruction())
            .build();
    }
    
    /**
     * Creates a configured ProactiveThinker LlmAgent with the proper API key
     */
    @Bean("proactiveThinkerLlmAgent")
    public LlmAgent createProactiveThinkerAgent() {
        // Set the API key from Secret Manager
        System.setProperty("GEMINI_API_KEY", secretManagerConfig.getSecret("GEMINI_API_KEY"));
        
        return LlmAgent.builder()
            .name("proactive_thinker")
            .description("Synthesizes trends and proposes strategic content angles")
            .model("gemini-2.5-flash")
            .instruction(getProactiveThinkerInstruction())
            .build();
    }
    
    /**
     * Get the TrendAnalyzer instruction prompt
     */
    private String getTrendAnalyzerInstruction() {
        return "You are a social media analyst for a high-end medical aesthetics practice. " +
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
    }
    
    /**
     * Get the ContentCreator instruction prompt
     */
    private String getContentCreatorInstruction() {
        return "CRITICAL PROMPT: You are a world-class social media strategist for luxury medical spas. " +
            "Your persona is a blend of clinical authority and elegant branding. Your task is to generate a new post " +
            "that is objectively better than the analyzed viral content. Your post must: " +
            "1. Incorporate a stronger, more direct hook. " +
            "2. Focus on the benefits of MDAesthetics services like SkinTyte, the Duo-C-Lift, or the Vivier body products. " +
            "3. Be highly educational and trustworthy. " +
            "4. Maintain the clean, high-end aesthetic. " +
            "5. Conclude with a clear CTA and a mix of niche (#duoclift) and broad (#torontoaesthetics) hashtags. " +
            "6. NEVER use the word \"Botox\" for pricing; use 'Tox', 'Neuromodulator', or 'Neurotoxin'. " +
            "Output JSON: {\"platform\":\"instagram/tiktok\",\"caption\":\"...\",\"hashtags\":[...],\"suggestedMediaType\":\"video/image\"}";
    }
    
    /**
     * Get the ProactiveThinker instruction prompt
     */
    private String getProactiveThinkerInstruction() {
        return "Analyze the top 5 trending posts from today. Synthesize the underlying themes. " +
            "Are users more interested in prevention or correction? Is 'natural-looking' a recurring phrase? " +
            "Propose three new, innovative content angles for MDAesthetics that anticipates the next trend. " +
            "Output JSON array: [{\"theme\":\"...\",\"insight\":\"...\",\"proposedAngle\":\"...\",\"reasoning\":\"...\"}]";
    }
}