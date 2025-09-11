# MD Aesthetics Viral Forge Java Agents

Implements the Java-side multi-agent pipeline using Google ADK Java (v0.2.0) and Gemini.

## Agents

1. TrendAnalyzer – classifies and extracts structured features from raw post JSON.
2. ContentCreator – produces superior on-brand caption + hashtags.
3. QAAgent – validates compliance & brand guardrails.
4. ProactiveThinker – synthesizes emerging themes, proposes 3 angles.
5. EmailSummarizer – formats HTML digest for daily email.

## Build

```
mvn -f mda-agents/pom.xml test
```

## Environment Variables

Required:
- GOOGLE_API_KEY – Gemini access.
- FIRESTORE_PROJECT_ID – Firestore project (optional if application default credentials already specify).

Optional:
- PRIORITY_CYCLE – Business promo tag (default: august-promo)
- GEMINI_MODEL – Override default model (default gemini-2.5-flash)

## Usage

```
java -jar target/mda-agents-1.0.0-SNAPSHOT.jar collection/docId
```

This will fetch Firestore doc, run analysis->draft->QA pipeline, and log JSON output.

## Next Steps

- Add REST facade (Cloud Run) subscribing to Pub/Sub `analyze-new-post` topic.
- Wire EmailSummarizer + dispatcher (Gmail API service account).
- Persist pipeline outputs back into Firestore under `/agent_outputs/{date}/{postId}`.
