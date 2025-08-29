package com.mdaesthetics.viral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.mdaesthetics.viral.openrouter.OpenRouterAdapter;

@Service
public class ViralEnrichmentAgent {

    private static final Logger log = LoggerFactory.getLogger(ViralEnrichmentAgent.class);

    private final OpenRouterAdapter openRouterAdapter;

    @Autowired
    public ViralEnrichmentAgent(OpenRouterAdapter openRouterAdapter) {
        this.openRouterAdapter = openRouterAdapter;
        log.info("ViralEnrichmentAgent initialized (OpenRouter adapter will be used if OPENROUTER_API_KEY is configured)");
    }

    public ViralEnrichmentResult enrichVideo(String videoUrl, String platform, String hashtags, String description) {
        String prompt = String.format(
            "Analyze this viral video from %s. URL: %s. Hashtags: %s. Description: %s. " +
            "Score virality, summarize content, and suggest trending clusters.",
            platform, videoUrl, hashtags, description
        );

        String key = System.getenv("OPENROUTER_API_KEY");
        if (key == null || key.isBlank()) {
            log.warn("OPENROUTER_API_KEY not provided; LLM enrichment will fallback to stub");
            return new ViralEnrichmentResult("(stub) enrichment unavailable - no LLM key configured.");
        }

        try {
            String resp = openRouterAdapter.generateText("openrouter-glm-4.5-air", prompt);
            if (resp != null && !resp.isBlank()) {
                // Trim and return the first meaningful chunk
                String out = resp.trim();
                if (out.length() > 0) return new ViralEnrichmentResult(out);
            }
        } catch (Exception e) {
            log.error("Error calling OpenRouter for enrichment, falling back to stub", e);
        }

        return new ViralEnrichmentResult("(stub) enrichment failed or returned empty");
    }
}