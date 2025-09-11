package com.mdaesthetics.agents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** Simple CLI bootstrap for manual orchestration or future REST wiring. */
public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) throws Exception {
    if (System.getenv("GOOGLE_API_KEY") == null) {
      throw new IllegalStateException("Missing GOOGLE_API_KEY environment variable for Gemini model access");
    }
    FirestoreService firestore = new FirestoreService();
    Orchestrator orchestrator = new Orchestrator(firestore);
    if (args.length == 0) {
      log.info("No document path provided. Exiting after environment validation.");
      return;
    }
    String docPath = args[0];
    Map<String, Object> business = new HashMap<>();
    business.put("priority_cycle", System.getenv().getOrDefault("PRIORITY_CYCLE", "august-promo"));
    business.put("pillars", new String[]{"Duo-C-Lift", "Tyte & Tone Body Bundle", "Firm + Lift Buttock Package", "Skin Boosting with Hyaluronic"});
    String pipelineJson = orchestrator.processSinglePost(docPath, business);
    log.info("Pipeline result: {}", pipelineJson);
  }
}
