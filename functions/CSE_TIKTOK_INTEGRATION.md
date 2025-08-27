# Google CSE & TikTok Integration

This document describes the implementation details of the unified Google Custom Search (CSE) augmentation and TikTok ingestion pipeline within Firebase Functions.

## Overview
The ingestion & enrichment stack aggregates Instagram and TikTok competitor posts, augments them with strategic Google CSE open‑web context, then enriches each post for downstream Java ADK agents.

Pipeline stages:
1. Instagram Scrape (Apify actor `apify/instagram-scraper`) – 08:00 America/Toronto
2. TikTok Scrape (Apify actor `clockworks/tiktok-scraper`) – 08:10 America/Toronto
3. Unified Orchestration (CSE augmentation + per‑post enrichment) – 08:30 America/Toronto
4. Firestore trigger `processViralPost` captures each new post for analysis queueing.

## Firestore Collections
- `viral_research` : Raw normalized posts (instagram|tiktok) + later merged `cseContext` array
- `csePosts` : Unique open‑web search results (deduplicated per link) across all strategic terms
- `analysis_requests` : Pending analysis entries (populated by `processViralPost` trigger)
- `orchestration_runs` : Metrics & telemetry for each enrichment run

## Environment / Secrets
Environment vars (Functions runtime):
- `GOOGLE_CSE_KEY` (secret) – Custom Search API key
- `GOOGLE_CSE_CX` (secret) – Search Engine ID
- `APIFY_TOKEN` (secret) – Apify API token
- (Optional) `GOOGLE_CSE_TERMS` – Comma separated override for default strategic terms

Configured via Firebase Functions v2 `defineSecret()` in `src/index.ts`.

## Scheduling
See `index.ts` schedules:
```
Instagram: 0 8 * * *
TikTok:    10 8 * * *
Orchestr.: 30 8 * * *
```
Time zone: America/Toronto

## Google CSE Augmentation (`googleCSEAgent.js`)
Features:
- Multi-term search (default 16 strategic terms) – override with `GOOGLE_CSE_TERMS`
- Exponential backoff (500ms * 2^attempt capped 5s) & retry (max 3) per term
- De-duplication by URL across all terms (in-run Set)
- Deterministic document IDs: base64(url) sanitized
- Stored documents include: term, title, link, snippet, displayLink, optional image, publishedAt, scrapedAt
- Placeholder crossRef structure: `crossRef.relatedPostIds` reserved for future linking

### Post-Level Enrichment
`enrichPostWithCse(post, limit)`:
- Tokenizes caption + hashtags (length > 3) -> token set
- Fetches latest 50 `csePosts`
- Scores each result (# of token occurrences in title+snippet)
- Attaches top `limit` (default 5) as `post.cseContext`
- Persisted back via `mergeCseContextIntoViralResearch(docId, cseContext)` during orchestration when original Firestore doc id available.

### Legacy vs New Enrichment
`MetadataEnrichmentAgent.js` performs a single-query CSE call per post (first 50 chars of caption). It currently co-exists adding a `cseMentions` array. Future consolidation can merge the two approaches (multi-term global vs per-post query) once evaluation completed.

## TikTok Integration (`dailyTiktokScraper`)
Actor: `clockworks/tiktok-scraper`
Input fields used:
- `profiles`, `hashtags`, `resultsPerPage`, sections, download toggles.
Normalization mapping:
- likes = diggCount|hearts
- comments = commentCount
- shares = shareCount
- views = playCount|plays
- hashtags = array -> `#name`
- timestamp = epoch seconds -> ISO
- engagementRate = (likes + comments + shares) / followerCount * 100
Firestore doc id format: `${date}_tiktok_${postId}` (merge to avoid duplication across runs)

## Orchestration (`runOrchestration`)
Injects workflow metrics:
- `targetsDiscovered` (placeholder – from discovery agent)
- `platformCounts` aggregated from provided posts array (instagram / tiktok / unknown)
- `cse` summary: { total, terms, saved }
- `enrichedPosts` count of posts processed
- `durationMs`, `status`

Persists metrics to `orchestration_runs` (both scheduled & HTTP invocations) including full `metrics` object.

## Data Flow Diagram (Conceptual)
```
[Apify Instagram] -->
                    \
                     +--> [viral_research] --(trigger)--> [analysis_requests]
                    /
[Apify TikTok] ---->

[viral_research] --(scheduled runOrchestration)--> [runCseAugmentation => csePosts]
                                               \-> [enrichPostWithCse => merge cseContext]

[orchestration_runs] <= metrics
```

## Operational Notes
- CSE failures (missing env vars) gracefully skip augmentation with warning log.
- Per-term search errors after retries log error but do not fail entire orchestration.
- Enrichment errors on individual posts are non-fatal; pipeline continues.
- Platform counts provide visibility in `orchestration_runs` entries for monitoring ingestion balance.

## Extensibility Roadmap
- Populate `crossRef.relatedPostIds` by scanning enriched posts referencing each CSE link.
- Consolidate legacy metadata enrichment into a single scoring & context builder.
- Add Pub/Sub push to Java ADK service for deeper multi-agent processing.
- Introduce rate limiting & caching for high-cost CSE terms if quotas approach limits.

## Testing Guidance
Manual harness: `functions/src/testOrchestration.js` can be executed locally with mock posts.
Recommended additional tests:
- Mock `searchGoogleCSE` to assert dedup logic.
- Tokenization scoring unit test (deterministic input -> expected ranking).
- Platform counts correctness with mixed platform array.

## Required Secrets Recap
| Name | Purpose |
|------|---------|
| APIFY_TOKEN | Auth for Apify actors (Instagram & TikTok) |
| GOOGLE_CSE_KEY | Google Custom Search API key |
| GOOGLE_CSE_CX | Search Engine ID |
| (Planned) GMAIL_SERVICE_ACCOUNT | Outbound daily brief emails |

Ensure these are added via: `firebase functions:secrets:set <NAME>` and referenced in `index.ts`.

---
Last updated: (auto-generated) pending further feature integration.
