# Viral Service Agent Trigger Flow

## Event Source

Firebase Functions write normalized Instagram/TikTok posts into the `viral_research` collection, and publish a JSON message to the `viral-post-created` Pub/Sub topic (configurable).

This document describes how the Java ADK agents (TrendAnalyzerAgent, ContentCreatorAgent, QAAgent) are triggered when new competitor posts are ingested.

### Delivery Modes

1. Push: Google Cloud Pub/Sub can be configured to push to `/viral-service/pubsub/analyze-new-post` (implemented in `PubSubPushController`).

2. Pull: If push is not configured, the internal `PubSubSubscriberService` (enabled by default) pulls from a subscription (env `VIRAL_POST_SUBSCRIPTION`, default `viral-post-created-sub`).

### Pipeline Steps (ViralWorkflowService)

1. Persist (or upsert) CompetitorPost in Firestore.

2. Invoke `TrendAnalyzerAgent` (model: OpenRouter configured model) to produce structured `TrendAnalysis` JSON (category, hook, virality & relevance heuristics, hashtags, etc.).

3. Invoke `ContentCreatorAgent` to produce a draft caption + hashtags aligned with brand pillars and compliance rules.

4. Run `QAAgent` to apply deterministic validation (CTA presence, hashtag bounds, forbidden terms, pillar keyword presence).

5. Persist `TrendAnalysis` and `ContentDraft` documents in Firestore (IDs referenced in logs & pipeline result map).

### Email / Digest

High-quality drafts (QA passed) are candidates for the daily digest. The `EmailDispatcherService` builds HTML for trends and drafts; actual Gmail API integration is a TODO (currently logs if simulate=true).

### Configuration Environment Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| GCP_PROJECT | GCP project ID | contentforge-ai-ygy25 |
| PUBSUB_ANALYZE_TOPIC | Analyze new post topic name | analyze-new-post |
| VIRAL_POST_SUBSCRIPTION | Pull subscription for post-created events | viral-post-created-sub |
| OPENROUTER_API_KEY | OpenRouter GenAI key (required) | (none) |
| DIGEST_RECIPIENTS | Comma separated daily digest recipients | `christine.carrer@hotmail.com,dalkeith@golden.net` |

### Model Enforcement

All agents use the configured OpenRouter model (or fallback policy defined in configuration) to perform inference.

### Logging

Structured logs include tags `[trend]`, `[workflow]`, `[email]` to facilitate filtering. Latency metrics (ms) are emitted for trend analysis and entire workflow.

### Next Steps (Optional Enhancements)

* Add Firestore caching in Java layer (current caching exists in Functions layer for daily digest).

* Implement Gmail API send using service account.

* Add Pub/Sub DLQ subscription for failed messages.

* Add Prometheus endpoint for metrics (success rates, latency percentiles).
