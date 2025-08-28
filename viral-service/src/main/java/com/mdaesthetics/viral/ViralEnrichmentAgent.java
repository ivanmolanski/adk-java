package com.mdaesthetics.viral;

import org.springframework.stereotype.Service;
import com.google.genai.*;
import com.google.genai.types.*;

@Service
public class ViralEnrichmentAgent {

    private final Client genaiClient;

    public ViralEnrichmentAgent() {
        // The Client constructor will automatically pick up GOOGLE_API_KEY from environment
        this.genaiClient = new Client();
    }

    public ViralEnrichmentResult enrichVideo(String videoUrl, String platform, String hashtags, String description) {
        String prompt = String.format(
            "Analyze this viral video from %s. URL: %s. Hashtags: %s. Description: %s. " +
            "Score virality, summarize content, and suggest trending clusters.",
            platform, videoUrl, hashtags, description
        );

        // Use configured model (migrated to OpenRouter model naming)
        GenerateContentResponse response = genaiClient.models.generateContent(
            "openrouter-glm-4.5-air",
            prompt,
            null
        );

        return new ViralEnrichmentResult(response.text());
    }
}