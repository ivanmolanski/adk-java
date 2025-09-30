package com.mdaesthetics.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/** Coordinates sequential execution of analysis -> content -> QA -> email formatting. */
public class Orchestrator {
  private static final Logger log = LoggerFactory.getLogger(Orchestrator.class);
  private final LlmAgent trendAnalyzer = AgentFactory.trendAnalyzer();
  private final LlmAgent contentCreator = AgentFactory.contentCreator();
  private final LlmAgent qaAgent = AgentFactory.qaAgent();
  private final LlmAgent emailSummarizer = AgentFactory.emailSummarizer();
  private final LlmAgent proactiveThinker = AgentFactory.proactiveThinker();
  private final FirestoreService firestore;

  public Orchestrator(FirestoreService firestore) {
    this.firestore = firestore;
  }

  /**
   * Execute an agent and extract text output from events.
   */
  private String runAgent(LlmAgent agent, String input) {
    Runner runner = new InMemoryRunner(agent, agent.name());
    Session session = runner.sessionService().createSession(agent.name(), "system").blockingGet();
    
    Content messageContent = Content.fromParts(Part.fromText(input));
    List<Event> events = runner
        .runAsync(session, messageContent, RunConfig.builder().build())
        .blockingStream()
        .toList();
    
    // Extract text content from events
    return events.stream()
        .filter(event -> event.content().isPresent())
        .map(event -> event.content().get().text())
        .collect(Collectors.joining());
  }

  public String processSinglePost(String docPath, Map<String, Object> businessContext)
      throws ExecutionException, InterruptedException {
    Map<String, Object> post = firestore.getDocument(docPath);
    String rawJson = JsonUtil.toJson(post);
    log.info("Processing post doc={} size={}B", docPath, rawJson.length());
    
    // Step 1: Trend Analysis
    log.info("Running TrendAnalyzer on post data...");
    String analysis = runAgent(trendAnalyzer, rawJson);
    log.info("TrendAnalyzer completed: {}B", analysis.length());
    
    // Step 2: Content Creation
    String enrichedInput = JsonUtil.mergeAnalyses(analysis, businessContext);
    log.info("Running ContentCreator with business context...");
    String draft = runAgent(contentCreator, enrichedInput);
    log.info("ContentCreator completed: {}B", draft.length());
    
    // Step 3: QA Validation
    log.info("Running QA validation on draft...");
    String qa = runAgent(qaAgent, draft);
    log.info("QA validation completed: {}B", qa.length());
    
    return JsonUtil.wrapPipeline(analysis, draft, qa);
  }

  public String summarizeDaily(String analysesJson, String draftsJson, String ideasJson) {
    log.info("Running EmailSummarizer for daily digest...");
    String emailInput = JsonUtil.composeEmailInput(analysesJson, draftsJson, ideasJson);
    String summary = runAgent(emailSummarizer, emailInput);
    log.info("EmailSummarizer completed: {}B", summary.length());
    return summary;
  }

  public String proactiveIdeas(String topAnalysesJson) {
    log.info("Running ProactiveThinker for trend synthesis...");
    String ideas = runAgent(proactiveThinker, topAnalysesJson);
    log.info("ProactiveThinker completed: {}B", ideas.length());
    return ideas;
  }
}
