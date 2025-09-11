# Agent Harmony & System Architecture

This document provides an authoritative overview of the MD Aesthetics Viral Intelligence & Content Generation system. It describes agents, data flows, control flows, persistence layers, metrics, scheduling, and extensibility contracts.

---
## 1. High-Level Flow (Daily Autonomous Cycle)

```text
[Scheduler] --> _run_scrape() --> ScrapingOrchestrator (Apify actors)
        |           |--> normalized post items (list[dict])
        |           v
        |      process_posts(session, posts, refine=True)
        |           |-- Upsert posts (ViralPostRepository)
        |           |-- TrendAnalyzer.analyze_batch()
        |           |-- ContentCreator.generate_batch(refine?)
        |           |-- Persist analyses & drafts
        |           |-- LearningStore.append() (analysis + draft composites)
        |           v
        |      Metrics: scrape_runs, scrape_items_collected, ingested_posts, analyses_created, drafts_created
        v
  _run_digest() --> send_daily_digest(session) --> EmailDispatcher (collect latest analyses + drafts)
                        |--> email sent -> metrics: digest_runs, digest_emails_sent
```

Ad‑hoc / On‑Demand paths (user initiated via API or UI) reuse the same `process_posts` and agent classes for consistency.

---
## 2. Core Agents

| Agent | Type | Responsibilities | Invocation Points |
|-------|------|------------------|-------------------|
| ScrapingOrchestrator | Operational | Calls Apify actors for Instagram / TikTok data; normalizes output | Scheduler scrape job; `/viral/scrape/apify` endpoint |
| TrendAnalyzer | Heuristic (local) | Extracts hooks, CTA, category, relevance & virality scores, engagement factors, themes, compliance notes | `process_posts()` step 2 |
| ContentCreator | Generative + heuristic | Produces branded captions, hashtags, service mapping, visual suggestions; optional refinement with GitHub `openai/gpt-4o` | `process_posts()` step 3 |
| Compliance (internal methods) | Embedded | Terminology & risk adjustments (e.g., Botox → Neuromodulator) | Inside ContentCreator refinement & sanitation phase |
| EmailDispatcher | Operational | Builds HTML digest (top analyses + drafts) and sends via SendGrid/SMTP | Manual endpoint or scheduled digest job |
| (Future) ProactiveThinker | Generative | Cross-analysis of top daily themes for forward-looking angles | To be added after stable baseline |
| (Future) QA-Agent | Rule-based | Final validation on CTA presence, hashtag counts, service mention coverage | After ContentCreator in pipeline |

---
## 3. Pipeline Orchestration (`process_posts`)

Sequential contract:

1. Normalize & upsert posts (idempotent) – duplicates collapsed by `id`.
2. Batch analyze with `TrendAnalyzer`.
3. Generate drafts (optionally `refine=True` enabling second-pass LLM polish).
4. Persist analyses and drafts with repositories.
5. Emit learning records to `LearningStore` (JSONL) for retrieval / future retrieval augmented generation.

Return object keys:

```json
{
  "ingested": 0,
  "analyses": 0,
  "drafts": 0,
  "analysis_ids": [],
  "draft_ids": []
}
```

Idempotency: If the same post id appears again it is updated; analyses & drafts always new (versioned by creation timestamp).

---

## 4. Data Model Snapshots (Conceptual)

```text
ViralPost(id, platform, profile, caption, hashtags[], likes, comments, shares, views, engagement_rate, post_url, scraped_at)
TrendAnalysis(id, post_id(FK), hook, cta, content_category, relevance_score, virality_score, summary, key_themes[], engagement_factors[], compliance_notes, analyzed_at)
ContentDraft(id, analysis_id(FK), platform, caption, hashtags[], suggested_media_type, target_service, compliance_checked, brand_alignment_score, estimated_engagement, suggested_visuals[], posting_tips[], created_at)
LearningStore (append-only JSONL blending select fields from TrendAnalysis + ContentDraft)
```

---

## 5. Metrics (Producer Mapping)

| Metric | Producer | Increment Condition |
|--------|----------|--------------------|
| chat_requests | Chat endpoint | Each incoming chat POST |
| ai_calls | AI client | Each successful upstream model completion |
| auth_failures | AI client | 401/403 from upstream |
| upstream_failures | AI client | Non-auth upstream errors after retries |
| rate_limit_events | AI client | 429 encountered |
| ingested_posts | Viral API / pipeline | After successful post upsert batch |
| analyses_created | pipeline | After `TrendAnalyzer` persistence |
| drafts_created | pipeline | After `ContentCreator` persistence |
| scrape_runs | scheduler / scrape endpoint | After scrape attempt (success path) |
| scrape_items_collected | scheduler / scrape endpoint | Count of normalized items appended |
| scrape_failures | scheduler / scrape endpoint | Exception path |
| digest_runs | scheduler digest | Successful invocation of digest job |
| digest_emails_sent | EmailDispatcher | Email successfully handed off to provider |

---

## 6. Scheduling

`scheduler.py` conditions:

- Enabled only if `ENABLE_SCHEDULER` env (truthy in {'1','true','yes','on'}).
- Two cron jobs (UTC):
  - 13:00 scrape (IG only by default; TikTok optional future flag).
  - 13:30 digest email.
- Resilience: broad exception capture with logging + failure metric increments (ensures loop continuity).

Scaling Guidance: Run **one** scheduler replica; all others disable `ENABLE_SCHEDULER`.

---

## 7. Refinement Path (ContentCreator)

If `refine=True` (set in scheduled pipeline & Apify endpoint) each generated caption may undergo:

1. Base heuristic assembly (hook inclusion, service mapping, hashtag generation).
2. Optional LLM refinement with GitHub `openai/gpt-4o` (strict no-fallback). Adds polish & brand tone alignment.
3. Compliance sanitation (terminology replacements, CTA assertion, forbidden term normalization).

Failures in the refinement call fall back to the pre-refinement draft (never empty) while surfacing logs + incrementing `upstream_failures` if applicable.

---

## 8. Error Handling Strategy

Tiered approach:

- **Pipeline**: Skip malformed posts individually (warn) but continue batch.
- **Scraping**: Platform-specific failures logged; partial results still returned.
- **Scheduler**: Broad `Exception` captured intentionally to avoid job abortion storm; metrics track failure count.
- **AI Client**: Distinguishes auth vs transient vs rate limit; no silent fallback, raises outward.

---

## 9. Security & Secrets

Single authoritative `.env` (no committed secrets). Deploy platforms should externalize secrets via their secret managers. Sensitive keys that MUST NOT be committed (examples only):

- `GITHUB_TOKEN`
- `SENDGRID_API_KEY`
- `APIFY_TOKEN`
- Any database credentials / Supabase service keys

If remnants of `.env.example` with live secrets exist they must be removed and replaced with a sanitized `.env.template`.

---

## 10. Extensibility Points

| Extension | How |
|-----------|-----|
| Add new platform (e.g., YouTube Shorts) | Implement normalizer + Apify actor call in `ScrapingOrchestrator` |
| Add QA gating | Insert QA agent step after draft generation before persistence |
| Trend synthesis (ProactiveThinker) | New agent reading top N analyses for the day & producing future angles; persist output as advisory records |
| Export metrics | Wrap in Prometheus collector or OTEL exporter replacing in-memory dict |
| Vector retrieval for chat | Index LearningStore composites into a vector DB and augment chat context |

---

## 11. Known Gaps / Backlog

1. No pagination or query filters on drafts/analyses endpoints (add for UI scaling).
2. Lack of per-service performance analytics (could aggregate virality by service target).
3. No hash-based duplicate caption suppression yet (potential future feature).
4. Scheduler currently hard-coded times; could derive from env (e.g., `SCRAPE_CRON`, `DIGEST_CRON`).
5. TikTok path disabled in scheduler (`include_tiktok=False`)—add flag once stable.

---

## 12. Operational Runbook Highlights

| Symptom | Check | Likely Fix |
|---------|-------|------------|
| `ai_calls` flat, `chat_requests` rising | `/ai/scopes` endpoint | Replace/repair `GITHUB_TOKEN` |
| Zero `scrape_items_collected` multiple days | Apify dashboard run logs | Adjust profiles/hashtags; verify `APIFY_TOKEN` |
| High `scrape_failures` | Scheduler logs | Network / actor ID mismatch / quota exhaustion |
| Missing digest email | `digest_runs` metric + email logs | Verify email creds / SMTP or SendGrid key |
| Drafts have unreplaced forbidden terms | ContentCreator logs | Inspect refinement failure path or update compliance rules |

---

## 13. Glossary

| Term | Definition |
|------|------------|
| Engagement Velocity (future) | (likes + comments) / hours since posted to prioritize fresh virality |
| LearningStore | Append-only JSONL acting as lightweight long-term memory for retrieval and analysis |
| Refinement | Second-pass LLM polish over heuristic draft |
| Compliance Sanitization | Deterministic text transformations to enforce brand & regulatory language |

---

## 14. Dependency Boundaries

- No Gemini / Vertex / Firebase runtime dependencies in backend path; only direct GitHub Models API for LLM calls.
- Scraping strictly via Apify actors (no embedded fragile HTML reverse engineering here).
- Email via SendGrid first; SMTP fallback if configured.

---

## 15. Change Control

Any modification to pipeline semantics (ordering, required fields) must update:

1. This document (Agent Harmony)
2. `README.md` architecture section
3. Tests covering `process_posts` invariants

---

*Document version: 1.0 – Generated automatically to reflect current implementation state.*
