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
    return new GenAiProperties(key);
  }
}
