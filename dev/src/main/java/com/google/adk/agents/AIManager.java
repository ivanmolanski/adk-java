package com.google.adk.agents;

import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * AIManager - The AI Command Center for MDAesthetics
 * Orchestrates all agent calls, delegates chat requests, and aggregates real-time viral output for the chat UI.
 */
@Service
public class AIManager {
    private static final Logger log = LoggerFactory.getLogger(AIManager.class);

    private final LlmAgent agent;
    private final InMemorySessionService sessionService = new InMemorySessionService();
    private final InMemoryArtifactService artifactService = new InMemoryArtifactService();

    // Agent dependencies (will be wired in when available)
    private ScrapingOrchestrator scrapingOrchestrator;
    private TrendAnalyzer trendAnalyzer;
    private ContentCreator contentCreator;
    private ProactiveThinker proactiveThinker;
    private EmailDispatcher emailDispatcher;

    private static final String SYSTEM_INSTRUCTION = """
        You are the AI Command Center for MDAesthetics, a high-end medical aesthetics practice specializing in physician-led treatments.

        BRAND CONTEXT:
        - Physician-Led, Results-Driven, Clinically Sophisticated
        - Focus: Duo-C-Lift (Ultherapy + Radiesse), SkinTyte body contouring, physician-grade injectables
        - Tone: Professional, authoritative, educational, trustworthy
        - NEVER use "Botox" - use "Tox", "Neuromodulator", or "Neurotoxin"

        YOUR ROLE:
        - Interpret user requests in the context of social media content creation and competitor analysis
        - Delegate to specialized agents: ScrapingOrchestrator, TrendAnalyzer, ContentCreator, ProactiveThinker, EmailDispatcher
        - Provide actionable insights for viral content strategy
        - Maintain clinical accuracy and brand alignment

        RESPONSE GUIDELINES:
        - Be conversational but professional
        - Focus on actionable recommendations
        - Include specific hashtags and content suggestions when relevant
        - Always tie recommendations back to MDAesthetics services
        - If analyzing competitors, suggest how to create superior content

        AVAILABLE ACTIONS:
        - Analyze viral trends from competitor data
        - Generate MDAesthetics-branded content ideas
        - Trigger competitor scraping jobs
        - Send daily briefing emails
        - Provide strategic recommendations for content pillars
        """;

    public AIManager() {
        this.agent = LlmAgent.builder()
            .name("ai_manager")
            .description("Orchestrates all viral content analysis and creation for MDAesthetics")
            .model("gemini-2.5-flash")
            .instruction(SYSTEM_INSTRUCTION)
            .build();
    }

    public String handleChat(String message) {
        String requestId = "chat-" + System.currentTimeMillis();
        long start = System.currentTimeMillis();

        try {
            log.info("[chat] Starting request: {}", requestId);

            // Create invocation context
            InvocationContext ctx = InvocationContext.create(
                sessionService,
                artifactService,
                requestId,
                agent,
                sessionService.createSession("mdaesthetics", "ai-manager").blockingGet(),
                Content.fromParts(Part.fromText(message)),
                RunConfig.builder().build()
            );

            // Run the agent and collect response
            StringBuilder responseBuilder = new StringBuilder();
            agent.runAsync(ctx)
                .timeout(45, TimeUnit.SECONDS)
                .blockingSubscribe(
                    event -> {
                        if (event.content().isPresent()) {
                            event.content().get().parts().ifPresent(parts ->
                                parts.forEach(part ->
                                    part.text().ifPresent(responseBuilder::append)
                                )
                            );
                        }
                    },
                    error -> {
                        log.error("[chat] Error in request {}: {}", requestId, error.getMessage());
                        responseBuilder.append("I apologize, but I encountered an error processing your request. Please try again.");
                    }
                );

            String response = responseBuilder.toString().trim();
            if (response.isEmpty()) {
                response = generateFallbackResponse(message);
            }

            long latency = System.currentTimeMillis() - start;
            log.info("[chat] Completed request {} in {}ms", requestId, latency);

            return response;

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[chat] Failed request {} in {}ms: {}", requestId, latency, e.getMessage(), e);
            return "I apologize, but I'm experiencing technical difficulties. Please try again in a moment.";
        }
    }

    private String generateFallbackResponse(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("trend") || lowerMessage.contains("viral")) {
            return """
                I'd be happy to help you analyze viral trends! Here are some current insights for MDAesthetics:

                🔥 HOT TREND: "Process Demystified" content is performing exceptionally well
                💡 OPPORTUNITY: Focus on educational videos showing SkinTyte treatment process
                🎯 RECOMMENDATION: Create content explaining the science behind our Duo-C-Lift combination therapy

                Would you like me to:
                - Trigger a fresh competitor scrape to get the latest data?
                - Generate content ideas based on current trends?
                - Send a briefing email with today's top findings?
                """;
        }

        if (lowerMessage.contains("content") || lowerMessage.contains("post")) {
            return """
                Great question about content creation! For MDAesthetics, I recommend focusing on our key pillars:

                📱 CONTENT IDEAS:
                • "The Science Behind SkinTyte" - Educational video series
                • "Why Duo-C-Lift Works" - Before/after with clinical explanation
                • "Physician-Grade Injectables" - Myth-busting common misconceptions

                🎨 BRAND VOICE: Professional, authoritative, results-focused
                📊 HASHTAGS: #mdaesthetics #torontoaesthetics #skintyte #duoclift #physicianled

                Would you like me to generate a specific post for one of these topics?
                """;
        }

        if (lowerMessage.contains("scrape") || lowerMessage.contains("competitor")) {
            return """
                I can help you gather fresh competitor intelligence! I can trigger scraping of:

                🎯 TARGET ACCOUNTS:
                • _thelookaesthetics (Instagram)
                • subtle.enhancements (Instagram)
                • skinvitality (Instagram & TikTok)
                • Local hashtags: #torontoaesthetics #whitbymedspa

                📊 WHAT WE'LL GET:
                • Top performing posts by engagement
                • Viral hooks and calls-to-action
                • Hashtag performance analysis
                • Content category classification

                Should I start a scraping job now? It typically takes 2-3 minutes to complete.
                """;
        }

        return """
            Hello! I'm your AI Command Center for MDAesthetics viral content strategy. I can help you with:

            📈 **Trend Analysis** - Analyze competitor viral content and extract actionable insights
            📝 **Content Creation** - Generate MDAesthetics-branded posts optimized for engagement
            🔍 **Competitor Research** - Trigger scraping of top local competitors
            📧 **Daily Briefings** - Send automated email digests with top findings
            💡 **Strategic Recommendations** - Get AI-powered suggestions for your content pillars

            What would you like to focus on today? You can ask me to:
            - "Analyze current viral trends"
            - "Generate a post about SkinTyte"
            - "Scrape competitor data"
            - "Send today's briefing email"

            I'm here to help you create content that drives real results for MDAesthetics!
            """;
    }

    // Agent orchestration methods (to be implemented when agents are available)
    public String triggerScraping(int maxAccounts, boolean forceRefresh) {
        if (scrapingOrchestrator != null) {
            return scrapingOrchestrator.triggerScraping(maxAccounts, forceRefresh);
        }
        return "ScrapingOrchestrator not available. Please ensure all agents are properly configured.";
    }

    public String analyzeTrends() {
        if (trendAnalyzer != null) {
            return trendAnalyzer.analyzeRecentTrends();
        }
        return "TrendAnalyzer not available. Please ensure all agents are properly configured.";
    }

    public String generateContent(String topic) {
        if (contentCreator != null) {
            return contentCreator.generateContent(topic);
        }
        return "ContentCreator not available. Please ensure all agents are properly configured.";
    }

    public String sendDailyBriefing() {
        if (emailDispatcher != null) {
            return emailDispatcher.sendDailyDigest();
        }
        return "EmailDispatcher not available. Please ensure all agents are properly configured.";
    }

    // Getters and setters for agent dependencies
    public void setScrapingOrchestrator(ScrapingOrchestrator scrapingOrchestrator) {
        this.scrapingOrchestrator = scrapingOrchestrator;
    }

    public void setTrendAnalyzer(TrendAnalyzer trendAnalyzer) {
        this.trendAnalyzer = trendAnalyzer;
    }

    public void setContentCreator(ContentCreator contentCreator) {
        this.contentCreator = contentCreator;
    }

    public void setProactiveThinker(ProactiveThinker proactiveThinker) {
        this.proactiveThinker = proactiveThinker;
    }

    public void setEmailDispatcher(EmailDispatcher emailDispatcher) {
        this.emailDispatcher = emailDispatcher;
    }
}
