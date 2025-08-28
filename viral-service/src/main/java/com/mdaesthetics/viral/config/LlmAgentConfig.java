package com.mdaesthetics.viral.config;

import com.mdaesthetics.viral.ai.OpenRouterClient;
import com.mdaesthetics.viral.agents.TrendAnalyzerAgent;
import com.mdaesthetics.viral.agents.ContentCreatorAgent;
import com.mdaesthetics.viral.agents.ProactiveThinkerAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for LLM agents that integrates with OpenRouter API for production deployment.
 * 
 * This configuration ensures that all agent instances are properly configured
 * with the OpenRouter API key retrieved from Google Cloud Secret Manager.
 */
@Configuration
@Profile("production") // Only active in production mode
public class LlmAgentConfig {
    
    @Autowired
    private SecretManagerConfig secretManagerConfig;
    
    @Autowired
    private OpenRouterClient openRouterClient;
    
    /**
     * Creates a configured TrendAnalyzer agent with the proper API key
     */
    @Bean("trendAnalyzerAgent")
    public TrendAnalyzerAgent createTrendAnalyzerAgent() {
        return new TrendAnalyzerAgent(openRouterClient);
    }
    
    /**
     * Creates a configured ContentCreator agent with the proper API key
     */
    @Bean("contentCreatorAgent")
    public ContentCreatorAgent createContentCreatorAgent() {
        return new ContentCreatorAgent(openRouterClient);
    }
    
    /**
     * Creates a configured ProactiveThinker agent with the proper API key
     */
    @Bean("proactiveThinkerAgent")
    public ProactiveThinkerAgent createProactiveThinkerAgent() {
        return new ProactiveThinkerAgent(openRouterClient);
    }
}