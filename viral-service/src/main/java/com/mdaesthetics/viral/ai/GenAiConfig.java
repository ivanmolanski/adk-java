package com.mdaesthetics.viral.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiConfig {

  @Value("${openrouter.api.key:#{null}}")
  private String configuredKey;

  @Bean
  public GenAiProperties genAiProperties() {
    String key = configuredKey;
    if (key == null || key.isBlank()) {
      key = System.getenv("OPENROUTER_API_KEY");
    }
    if (key == null || key.isBlank()) {
      key = System.getenv("OPENROUTER_API_KEY_PROD");
    }
    return new GenAiProperties(key);
  }
  
  @Bean
  public OpenRouterClient openRouterClient(@Value("${openrouter.api.key:#{null}}") String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      apiKey = System.getenv("OPENROUTER_API_KEY");
    }
    if (apiKey == null || apiKey.isBlank()) {
      apiKey = System.getenv("OPENROUTER_API_KEY_PROD");
    }
    return new OpenRouterClient(apiKey);
  }
}
