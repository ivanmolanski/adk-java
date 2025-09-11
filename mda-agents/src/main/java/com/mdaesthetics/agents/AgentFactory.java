package com.mdaesthetics.agents;

import com.google.adk.agents.LlmAgent;

/** Factory for constructing strongly instructed LLM agents for Viral Forge. */
public final class AgentFactory {

  private static final String MODEL = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-2.5-flash");

  private AgentFactory() {}

  public static LlmAgent trendAnalyzer() {
    return LlmAgent.builder()
        .name("TrendAnalyzer")
        .description("Analyzes raw social post data and extracts structured hooks & CTAs")
        .model(MODEL)
        .instruction("You are a social media analyst for a high-end medical aesthetics practice. " +
            "Given JSON for a post (caption, metrics, hashtags), classify it into one of: 'Process Demystified', 'Science Explained', 'Transformation', 'Myth Busting'. " +
            "Extract: 3-second hook, call_to_action, educational_point (if any), pacing_style (fast/medium/slow), and produce JSON strictly with keys: category, hook, call_to_action, educational_point, pacing_style, hashtags[]. If information missing use null.")
        .build();
  }

  public static LlmAgent contentCreator() {
    return LlmAgent.builder()
        .name("ContentCreator")
        .description("Generates superior on-brand MD Aesthetics post drafts")
        .model(MODEL)
        .instruction("Persona: Dr. Copeland's trusted clinical strategist. Tone: physician-led, results-driven, clean, authoritative, educational. " +
            "Input: JSON analysis from TrendAnalyzer + business priorities. Task: Create a NEW post (do not copy). Requirements: 1) Stronger direct hook. 2) Pivot to one core pillar (Duo-C-Lift, Tyte & Tone Body Bundle, Firm + Lift Buttock Package, Skin Boosting with Hyaluronic). 3) Explain clinical WHY (mechanism: collagen stimulation, infrared tightening, Vitamin C resurfacing). 4) Use professional language (firmness, smoothing, increase volume). 5) End with CTA to book consultation or link in bio. 6) Provide 8-14 hashtags: mix of local (#torontoaesthetics, #whitby), service (#skintytetreatment, #ultherapy, #duoclift), branded (#mdaesthetics). 7) NEVER use word 'Botox'—substitute Neuromodulator or Neurotoxin. 8) Return JSON with keys: hook, caption, hashtags[], cta, pillar, compliance_ok (true/false), violations[].")
        .build();
  }

  public static LlmAgent proactiveThinker() {
    return LlmAgent.builder()
        .name("ProactiveThinker")
        .description("Synthesizes trends and proposes forward-looking angles")
        .model(MODEL)
        .instruction("Analyze the top 5 structured analyses for today. Identify emerging themes (e.g., prevention, natural-looking, biostimulator education). Propose exactly 3 innovative content angles (JSON array) each with: title, rationale, suggested_hook, pillar, risk_level (low/med/high). Output JSON only.")
        .build();
  }

  public static LlmAgent qaAgent() {
    return LlmAgent.builder()
        .name("QAAgent")
        .description("Validates compliance and brand alignment")
        .model(MODEL)
        .instruction("You receive JSON for a generated caption. Verify: (a) CTA present, (b) 5-15 hashtags, (c) pillar mentioned, (d) no forbidden words (Botox, pricing like '$' with units), (e) tone clinical & authoritative. Return JSON: compliance_pass (true/false), issues[].")
        .build();
  }

  public static LlmAgent emailSummarizer() {
    return LlmAgent.builder()
        .name("EmailSummarizer")
        .description("Formats daily digest for email dispatch")
        .model(MODEL)
        .instruction("Given arrays of top analyses, generated captions, and proactive ideas, craft a concise executive digest HTML fragment. Sections: 1) Viral Intelligence Snapshot (bullets). 2) New Draft Captions (numbered). 3) Strategic Angles (table). Keep medically professional. Do NOT hallucinate metrics. Return raw HTML only (no markdown).")
        .build();
  }
}
