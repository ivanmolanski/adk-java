package com.mdaesthetics.viral.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mdaesthetics.viral.ai.OpenRouterClient;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

@Component
public class ContentCreatorAgent {
    
    private static final Logger log = LoggerFactory.getLogger(ContentCreatorAgent.class);
    private final OpenRouterClient openRouterClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INSTRUCTION = """
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
        
        Output ONLY valid JSON with fields: caption, hashtags, suggestedMediaType, complianceChecked
        """;

    public ContentCreatorAgent(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public Map<String, Object> createContent(Map<String, Object> trendAnalysis, Map<String, Object> originalPost) {
        String category = safeGetString(trendAnalysis, "category");
        String hook = safeGetString(trendAnalysis, "hook");
        String educationalPoint = safeGetString(trendAnalysis, "educationalPoint");
        String originalTag = safeGetString(originalPost, "tag");
        Integer viralityScore = safeGetInteger(trendAnalysis, "viralityScore", 5);

        String prompt = String.format("""
            %s
            
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
            
            Return ONLY a JSON object with: caption, hashtags (array), suggestedMediaType, complianceChecked (boolean).
            """, INSTRUCTION, category, hook, educationalPoint, originalTag, viralityScore);

        try {
            String response = openRouterClient.chat(prompt, "z-ai/glm-4.5-air:free", 2.0);
            
            // Try to parse the LLM response as JSON
            String json = extractJson(response);
            if (json != null) {
                try {
                    Map<String,Object> parsed = objectMapper.readValue(json, new TypeReference<Map<String,Object>>(){});
                    // Basic validation
                    if (parsed.containsKey("caption") && parsed.containsKey("hashtags")) {
                        // Normalize and return
                        Map<String,Object> out = new HashMap<>();
                        out.put("caption", parsed.get("caption"));
                        out.put("hashtags", parsed.get("hashtags"));
                        out.put("suggestedMediaType", parsed.getOrDefault("suggestedMediaType", suggestMediaType(category)));
                        out.put("complianceChecked", parsed.getOrDefault("complianceChecked", true));
                        out.put("brandAlignment", parsed.getOrDefault("brandAlignment", "high"));
                        
                        // Replace any "Botox" occurrences with compliant alternatives
                        String caption = (String) out.get("caption");
                        if (caption != null) {
                            caption = caption.replaceAll("(?i)botox", "Neuromodulator");
                            out.put("caption", caption);
                        }
                        
                        log.info("Content creation successful for category: {}", category);
                        return out;
                    }
                } catch (Exception parseEx) {
                    log.warn("Failed to parse LLM JSON response: {}", parseEx.getMessage());
                }
            }

            // If initial parsing fails, try with stricter JSON-only prompt
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

            String strictResponse = openRouterClient.chat(strictPrompt, "z-ai/glm-4.5-air:free", 1.0);
            String strictJson = extractJson(strictResponse);
            
            if (strictJson != null) {
                try {
                    Map<String,Object> parsed = objectMapper.readValue(strictJson, new TypeReference<Map<String,Object>>(){});
                    if (parsed.containsKey("caption") && parsed.containsKey("hashtags")) {
                        Map<String,Object> out = new HashMap<>();
                        out.put("caption", parsed.get("caption"));
                        out.put("hashtags", parsed.get("hashtags"));
                        out.put("suggestedMediaType", parsed.getOrDefault("suggestedMediaType", suggestMediaType(category)));
                        out.put("complianceChecked", parsed.getOrDefault("complianceChecked", true));
                        out.put("brandAlignment", parsed.getOrDefault("brandAlignment", "high"));
                        out.put("rawResponse", strictResponse);
                        
                        // Replace any "Botox" occurrences with compliant alternatives
                        String caption = (String) out.get("caption");
                        if (caption != null) {
                            caption = caption.replaceAll("(?i)botox", "Neuromodulator");
                            out.put("caption", caption);
                        }
                        
                        log.info("Content creation successful with strict prompt for category: {}", category);
                        return out;
                    }
                } catch (Exception strictEx) {
                    log.warn("Strict JSON parsing also failed: {}", strictEx.getMessage());
                }
            }

            // If still failing, return structured content with raw response
            Map<String,Object> fallback = createStructuredContent(category, originalTag, viralityScore);
            fallback.put("rawResponse", response);
            log.warn("Using fallback structured content for category: {}", category);
            return fallback;
            
        } catch (Exception e) {
            log.error("Content creation failed: {}", e.getMessage(), e);
            return createFallbackContent(originalTag);
        }
    }

    private Map<String, Object> createStructuredContent(String category, String originalTag, Integer viralityScore) {
        String caption = generateCaption(category, originalTag);
        List<String> hashtags = generateHashtags(originalTag);
        String mediaType = suggestMediaType(category);

        HashMap<String, Object> result = new HashMap<>();
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
        if (tag.contains("skintyte")) {
            return List.of(
                "mdaesthetics", "torontoaesthetics", "whitbymedspa",
                "skintyte", "bodycontouring", "skintexture", "firmandsmooth",
                "collagenremodeling", "physicianled",
                "medicalaesthetics", "durhamregion", "whitby", "toronto"
            );
        } else if (tag.contains("ultherapy") || tag.contains("duoclift")) {
            return List.of(
                "mdaesthetics", "torontoaesthetics", "whitbymedspa",
                "ultherapy", "duoclift", "radiesse", "biostimulator",
                "nonsurgicallift", "facialrejuvenation", "collagenstimulation",
                "medicalaesthetics", "physicianled", "durhamregion"
            );
        } else {
            return List.of(
                "mdaesthetics", "torontoaesthetics", "whitbymedspa",
                "physicianled", "medicalaesthetics", "durhamregion"
            );
        }
    }

    private String suggestMediaType(String category) {
        if (category == null) return "photo_carousel";
        return switch (category) {
            case "Process Demystified" -> "video_process";
            case "Science Explained" -> "infographic_animation";
            case "Transformation" -> "before_after_carousel";
            case "Myth Busting" -> "video_education";
            default -> "photo_carousel";
        };
    }

    private Map<String, Object> createFallbackContent(String originalTag) {
        return Map.of(
            "caption", "Discover the science behind beautiful, natural-looking results. Book your consultation with our physician-led team.",
            "hashtags", List.of("mdaesthetics", "torontoaesthetics", "physicianled"),
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