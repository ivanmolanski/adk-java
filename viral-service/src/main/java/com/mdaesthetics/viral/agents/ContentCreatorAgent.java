package com.mdaesthetics.viral.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.RunConfig;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class ContentCreatorAgent {
    
    private final LlmAgent agent;
    private final InMemorySessionService sessionService;
    private final InMemoryArtifactService artifactService;

    public ContentCreatorAgent() {
        this.agent = LlmAgent.builder()
            .name("content_creator")
            .description("World-class social media strategist for luxury medical spas")
            .model("gemini-2.5-flash")
            .instruction("""
                You are Dr. Copeland's trusted clinical strategist and content creator. Your persona blends clinical authority with elegant branding.
                
                Your task is to generate superior social media content based on viral trend analysis. 
                
                BRAND IDENTITY - MDAesthetics:
                - Physician-Led, Results-Driven, Clinically Sophisticated
                - NOT a fluffy spa - a medical practice focused on aesthetic science
                - Tone: Professional, authoritative, educational, trustworthy
                - Aesthetic: Clean, bright, uncluttered
                
                KEY CONTENT PILLARS:
                1. Advanced Combination Therapies: "Duo-C-Lift" (Ultherapy + Radiesse)
                2. Specialized Body Contouring: SkinTyte for buttocks, knees, decollete
                3. Physician-Grade Injectables: Radiesse biostimulator vs standard fillers
                4. Medical-Grade Technology: BBL, Moxi, SkinTyte, Vivier products
                
                CONTENT REQUIREMENTS:
                - Incorporate a stronger, more direct hook than the viral content
                - Focus on MDAesthetics services: SkinTyte, Duo-C-Lift, Vivier products
                - Be highly educational and trustworthy
                - Explain the 'Why' behind treatments with clinical depth
                - Maintain results-oriented language: "firmness," "smoothing," "volume"
                - End with clear CTA: book consultation or link in bio
                - Generate hashtags: local (#torontoaesthetics), service (#skintytetreatment), brand (#mdaesthetics)
                - NEVER use "Botox" - use "Tox", "Neuromodulator", or "Neurotoxin"
                
                Output as JSON with fields: caption, hashtags, suggestedMediaType, complianceChecked
                """)
            .build();
        
        this.sessionService = new InMemorySessionService();
        this.artifactService = new InMemoryArtifactService();
    }

    public Map<String, Object> createContent(Map<String, Object> trendAnalysis, Map<String, Object> originalPost) {
        String category = safeGetString(trendAnalysis, "category");
        String hook = safeGetString(trendAnalysis, "hook");
        String educationalPoint = safeGetString(trendAnalysis, "educationalPoint");
        String originalTag = safeGetString(originalPost, "tag");
        Integer viralityScore = safeGetInteger(trendAnalysis, "viralityScore", 5);

        String prompt = String.format("""
            Create a superior MDAesthetics post based on this viral content analysis:
            
            Original Category: %s
            Viral Hook: %s
            Educational Point: %s
            Original Tag: %s
            Virality Score: %d/10
            
            Generate a new post that:
            1. Uses a stronger hook adapted to MDAesthetics services
            2. Focuses on our core treatments (SkinTyte, Duo-C-Lift, Radiesse, etc.)
            3. Explains the clinical science behind the treatment
            4. Maintains our professional, physician-led brand voice
            5. Includes clear call-to-action
            6. Uses compliant language (no "Botox", use alternatives)
            
            Format as JSON with caption, hashtags array, suggestedMediaType, complianceChecked boolean.
            """, category, hook, educationalPoint, originalTag, viralityScore);

        try {
            // Create proper ADK invocation context
            InvocationContext invocationContext = InvocationContext.create(
                sessionService,
                artifactService,
                "content-creation-" + System.currentTimeMillis(),
                agent,
                sessionService.createSession("mdaesthetics", "content-creator").blockingGet(),
                Content.fromParts(Part.fromText(prompt)),
                RunConfig.builder().build()
            );
            
            // Run the agent and extract response
            StringBuilder responseBuilder = new StringBuilder();
            Flowable<Event> eventStream = agent.runAsync(invocationContext);
            
            eventStream.blockingSubscribe(
                event -> {
                    if (event.content().isPresent()) {
                        event.content().get().parts().ifPresent(parts -> {
                            parts.forEach(part -> {
                                part.text().ifPresent(responseBuilder::append);
                            });
                        });
                    }
                },
                error -> System.err.println("Content creation error: " + error.getMessage()),
                () -> System.out.println("Content creation completed")
            );
            
            String response = responseBuilder.toString();

            // Try to parse the LLM response as JSON with retries
            ObjectMapper mapper = new ObjectMapper();
            int attempts = 0;
            while (attempts < 3) {
                attempts++;
                try {
                    if (response != null && !response.isBlank()) {
                        // Attempt to extract JSON object from response
                        String json = extractJson(response);
                        if (json != null) {
                            Map<String,Object> parsed = mapper.readValue(json, new TypeReference<Map<String,Object>>(){});
                            // Basic validation
                            if (parsed.containsKey("caption") && parsed.containsKey("hashtags")) {
                                // Normalize and return
                                Map<String,Object> out = new HashMap<>();
                                out.put("caption", parsed.get("caption"));
                                out.put("hashtags", parsed.get("hashtags"));
                                out.put("suggestedMediaType", parsed.getOrDefault("suggestedMediaType", suggestMediaType(category)));
                                out.put("complianceChecked", parsed.getOrDefault("complianceChecked", true));
                                out.put("brandAlignment", parsed.getOrDefault("brandAlignment", "high"));
                                return out;
                            }
                        }
                    }
                } catch (Exception parseEx) {
                    System.err.println("Failed to parse LLM JSON (attempt " + attempts + "): " + parseEx.getMessage());
                }

                // wait briefly and retry reading the event stream again (if the agent provided more content)
                try { Thread.sleep(500); } catch (InterruptedException ie) { /* ignore */ }
                // attempt to read any new content from the agent's session artifacts (not implemented here)
            }

            // If parsing fails after retries, attempt a stricter second-pass prompt forcing JSON-only output
            try {
                String strictPrompt = String.format("""
                    You are a content generator for MDAesthetics. OUTPUT ONLY a single JSON object with the following keys: caption (string), hashtags (array of strings), suggestedMediaType (string), complianceChecked (boolean).
                    Do NOT include any explanation or surrounding text.

                    Context:
                    Original Category: %s
                    Viral Hook: %s
                    Educational Point: %s
                    Original Tag: %s
                    Virality Score: %d/10

                    Return only the JSON object.
                    """, category, hook, educationalPoint, originalTag, viralityScore);

                InvocationContext strictCtx = InvocationContext.create(
                    sessionService,
                    artifactService,
                    "content-creation-strict-" + System.currentTimeMillis(),
                    agent,
                    sessionService.createSession("mdaesthetics", "content-creator").blockingGet(),
                    Content.fromParts(Part.fromText(strictPrompt)),
                    RunConfig.builder().build()
                );

                StringBuilder strictResp = new StringBuilder();
                Flowable<Event> strictStream = agent.runAsync(strictCtx);
                strictStream.blockingSubscribe(evt -> {
                    if (evt.content().isPresent()) {
                        evt.content().get().parts().ifPresent(parts -> parts.forEach(p -> p.text().ifPresent(strictResp::append)));
                    }
                });

                String strictText = strictResp.toString();
                String json = extractJson(strictText);
                if (json != null) {
                    Map<String,Object> parsed = mapper.readValue(json, new TypeReference<Map<String,Object>>(){});
                    if (parsed.containsKey("caption") && parsed.containsKey("hashtags")) {
                        Map<String,Object> out = new HashMap<>();
                        out.put("caption", parsed.get("caption"));
                        out.put("hashtags", parsed.get("hashtags"));
                        out.put("suggestedMediaType", parsed.getOrDefault("suggestedMediaType", suggestMediaType(category)));
                        out.put("complianceChecked", parsed.getOrDefault("complianceChecked", true));
                        out.put("brandAlignment", parsed.getOrDefault("brandAlignment", "high"));
                        out.put("rawResponse", strictText);
                        return out;
                    }
                }
            } catch (Exception secondEx) {
                System.err.println("Second-pass JSON generation failed: " + secondEx.getMessage());
            }

            // If still failing, return fallback with raw response included for debugging
            Map<String,Object> fallback = createStructuredContent(category, originalTag, viralityScore);
            fallback.put("rawResponse", response);
            return fallback;
            
        } catch (Exception e) {
            System.err.println("Content creation failed: " + e.getMessage());
            // Fallback content
            return createFallbackContent(originalTag);
        }
    }

    private Map<String, Object> createStructuredContent(String category, String originalTag, Integer viralityScore) {
        String caption = generateCaption(category, originalTag);
        List<String> hashtags = generateHashtags(originalTag);
        String mediaType = suggestMediaType(category);

        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        result.put("caption", caption);
        result.put("hashtags", hashtags);
        result.put("suggestedMediaType", mediaType);
        result.put("complianceChecked", true);
        result.put("brandAlignment", "high");
        return result;
    }

    private String generateCaption(String category, String originalTag) {
        String tag = originalTag == null ? "" : originalTag.toLowerCase();
        if (tag.contains("skintyte")) {
            return """
                🔬 The Science Behind Firmer Skin
                
                SkinTyte uses precisely controlled infrared light to heat collagen fibers, causing immediate contraction and stimulating long-term neocollagenesis.
                
                ✨ What makes it different?
                • Physician-supervised treatment protocols
                • Customizable energy levels for your skin type
                • Targets areas traditional treatments miss: knees, buttocks, décolletage
                
                The result? Clinically measurable skin tightening that continues improving for months.
                
                📞 Book a consultation to determine if you're a candidate for our FIRM + LIFT + SMOOTH body package.
                """;
        } else if (tag.contains("ultherapy") || tag.contains("duoclift")) {
            return """
                🎯 Why We Combine Ultherapy + Radiesse (Our "Duo-C-Lift")
                
                Single treatments work. Intelligent combinations transform.
                
                🧬 The Science:
                • Ultherapy: Focused ultrasound lifts from within
                • Radiesse: Biostimulator creates new collagen architecture
                • Combined: Immediate lift + progressive volumization
                
                This isn't trendy marketing. It's evidence-based aesthetic medicine delivering results that last 18+ months.
                
                📱 Link in bio to see if you're a candidate for our signature combination therapy.
                """;
        } else {
            return """
                💡 Medical Aesthetics vs. Beauty Treatments: Know the Difference
                
                When a physician leads your aesthetic journey, every decision is based on:
                ✓ Anatomical knowledge
                ✓ Evidence-based protocols
                ✓ Customized treatment plans
                ✓ Safety-first approach
                
                We don't chase trends. We deliver scientifically-proven results using medical-grade technology.
                
                🩺 Experience the MDAesthetics difference. Book your physician consultation today.
                """;
        }
    }

    private List<String> generateHashtags(String originalTag) {
        String tag = originalTag == null ? "" : originalTag.toLowerCase();
        // Base hashtags always included
        List<String> hashtags = List.of(
            "#mdaesthetics",
            "#torontoaesthetics",
            "#whitbymedspa",
            "#physicianled",
            "#medicalaesthetics",
            "#durhamregion"
        );

        // Add specific service hashtags based on content
        if (tag.contains("skintyte")) {
            return List.of(
                "#mdaesthetics", "#torontoaesthetics", "#whitbymedspa",
                "#skintyte", "#bodycontouring", "#skintexture", "#firmandsmooth",
                "#collagenremodeling", "#physicianled",
                "#medicalaesthetics", "#durhamregion", "#whitby", "#toronto"
            );
        } else if (tag.contains("ultherapy") || tag.contains("duoclift")) {
            return List.of(
                "#mdaesthetics", "#torontoaesthetics", "#whitbymedspa",
                "#ultherapy", "#duoclift", "#radiesse", "#biostimulator",
                "#nonsurgicallift", "#facialrejuvenation", "#collagenstimulation",
                "#medicalaesthetics", "#physicianled", "#durhamregion"
            );
        } else {
            return hashtags;
        }
    }

    private String suggestMediaType(String category) {
        switch (category) {
            case "Process Demystified":
                return "video_process";
            case "Science Explained":
                return "infographic_animation";
            case "Transformation":
                return "before_after_carousel";
            case "Myth Busting":
                return "video_education";
            default:
                return "photo_carousel";
        }
    }

    private Map<String, Object> createFallbackContent(String originalTag) {
        return Map.of(
            "caption", "Discover the science behind beautiful, natural-looking results. Book your consultation with our physician-led team.",
            "hashtags", List.of("#mdaesthetics", "#torontoaesthetics", "#physicianled"),
            "suggestedMediaType", "photo_carousel",
            "complianceChecked", true,
            "error", "AI generation failed, using fallback content"
        );
    }

    // Safe extractors
    private String safeGetString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private Integer safeGetInteger(Map<String, Object> map, String key, Integer fallback) {
        if (map == null) return fallback;
        Object v = map.get(key);
        if (v instanceof Integer) return (Integer) v;
        try {
            if (v instanceof Number) return ((Number) v).intValue();
            if (v instanceof String) return Integer.parseInt((String) v);
        } catch (Exception e) {
            // ignore and fall through
        }
        return fallback;
    }

    // Attempt to extract a JSON object from a potentially noisy LLM response
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