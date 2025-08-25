package com.mdaesthetics.viral.agents;

import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.InMemorySessionService;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.mdaesthetics.viral.model.TrendAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Synthesizes underlying themes across top TrendAnalysis results and proposes forward-looking angles.
 */
@Component
public class ProactiveThinkerAgent {
    private static final Logger log = LoggerFactory.getLogger(ProactiveThinkerAgent.class);

    private final LlmAgent agent;
    private final InMemorySessionService sessionService = new InMemorySessionService();
    private final InMemoryArtifactService artifactService = new InMemoryArtifactService();

    private static final String INSTRUCTION = "You are a strategic medical aesthetics marketing strategist. " +
        "Given a JSON array of recent structured trend analyses, identify macro themes (prevention vs correction, natural look framing, collagen focus, combination therapy interest). " +
        "Then propose EXACTLY three innovative, forward-looking content angles that anticipate the next wave of patient interest for a physician-led clinic. " +
        "Output JSON: {themes:[string...], angles:[{title:string, rationale:string, suggestedHook:string, pillar:string}]} only.";

    public ProactiveThinkerAgent() {
        this.agent = LlmAgent.builder()
            .name("proactive_thinker")
            .model("gemini-2.5-flash")
            .description("Derives themes and proposes proactive content angles")
            .instruction(INSTRUCTION)
            .build();
    }

    public Map<String,Object> synthesize(List<TrendAnalysis> topAnalyses) {
        String jsonArray = toJsonArray(topAnalyses); // simplified inline JSON
        StringBuilder sb = new StringBuilder();
        try {
            InvocationContext ctx = InvocationContext.create(
                sessionService,
                artifactService,
                "proactive-" + System.currentTimeMillis(),
                agent,
                sessionService.createSession("mdaesthetics", "proactive-thinker").blockingGet(),
                Content.fromParts(Part.fromText(jsonArray)),
                RunConfig.builder().build()
            );
            agent.runAsync(ctx)
                .timeout(40, TimeUnit.SECONDS)
                .blockingSubscribe(ev -> ev.content().ifPresent(c -> c.parts().ifPresent(parts -> parts.forEach(p -> p.text().ifPresent(sb::append)))));

            String raw = sb.toString();
            return Map.of("raw", raw); // parsing deferred; frontend/next step can parse JSON strictly
        } catch (Exception e) {
            log.error("[proactive] failure msg={} ", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    private String toJsonArray(List<TrendAnalysis> list) {
        return "[" + list.stream().map(this::toJson).collect(Collectors.joining(",")) + "]";
    }
    private String q(String s){ return "\"" + (s==null?"":s.replace("\\","\\\\").replace("\"","\\\"") ) + "\""; }
    private String toJson(TrendAnalysis ta) {
        return "{" +
            q("category")+":"+q(ta.category())+","+
            q("hook")+":"+q(ta.hook())+","+
            q("callToAction")+":"+q(ta.callToAction())+","+
            q("educationalPoint")+":"+q(ta.educationalPoint())+","+
            q("viralityScore")+":"+(ta.viralityScore()==null?0:ta.viralityScore())+","+
            q("relevanceScore")+":"+(ta.relevanceScore()==null?0:ta.relevanceScore())+"}";
    }
}
