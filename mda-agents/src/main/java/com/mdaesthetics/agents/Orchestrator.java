package com.mdaesthetics.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

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

  public String processSinglePost(String docPath, Map<String, Object> businessContext)
      throws ExecutionException, InterruptedException {
    Map<String, Object> post = firestore.getDocument(docPath);
    String rawJson = JsonUtil.toJson(post);
    log.info("Processing post doc={} size={}B", docPath, rawJson.length());
    
    // Step 1: Trend Analysis
    log.info("Running TrendAnalyzer on post data...");
    String analysis = trendAnalyzer.run(rawJson).getOutput();
    log.info("TrendAnalyzer completed: {}B", analysis.length());
    
    // Step 2: Content Creation
    String enrichedInput = JsonUtil.mergeAnalyses(analysis, businessContext);
    log.info("Running ContentCreator with business context...");
    String draft = contentCreator.run(enrichedInput).getOutput();
    log.info("ContentCreator completed: {}B", draft.length());
    
    // Step 3: QA Validation
    log.info("Running QA validation on draft...");
    String qa = qaAgent.run(draft).getOutput();
    log.info("QA validation completed: {}B", qa.length());
    
    return JsonUtil.wrapPipeline(analysis, draft, qa);
  }

  public String summarizeDaily(String analysesJson, String draftsJson, String ideasJson) {
    log.info("Running EmailSummarizer for daily digest...");
    String emailInput = JsonUtil.composeEmailInput(analysesJson, draftsJson, ideasJson);
    String summary = emailSummarizer.run(emailInput).getOutput();
    log.info("EmailSummarizer completed: {}B", summary.length());
    return summary;
  }

  public String proactiveIdeas(String topAnalysesJson) {
    log.info("Running ProactiveThinker for trend synthesis...");
    String ideas = proactiveThinker.run(topAnalysesJson).getOutput();
    log.info("ProactiveThinker completed: {}B", ideas.length());
    return ideas;
  }
}
