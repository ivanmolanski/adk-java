# MD Aesthetics Viral Content System

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Python](https://img.shields.io/badge/Python-3.12-blue.svg)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.116-green.svg)](https://fastapi.tiangolo.com)
[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org)

> An intelligent competitive analysis and content generation system for MD Aesthetics, powered by a lean Python/FastAPI backend with strict direct GitHub Models integration (no Gemini / Firebase / Google ADK runtime dependencies).

The MD Aesthetics Viral Content System is a comprehensive solution for monitoring competitor social media content, analyzing viral trends, and generating compliant, on-brand content for MD Aesthetics' social media channels.

--------------------------------------------------------------------------------

## ✨ Key Features

- **🔍 Competitive Intelligence**: Automated monitoring of competitor Instagram and TikTok accounts with engagement analysis
- **🤖 AI-Powered Agents**: Pydantic-based agents for trend analysis, content creation, and compliance checking
- **📊 Trend Analysis**: Identifies viral hooks, CTAs, content categories, and engagement drivers
- **✍️ Content Generation**: Creates MD Aesthetics-branded content with automatic compliance checking
- **📧 Automated Reporting**: Daily digest emails with trending content and generated drafts
- **🌐 Modern Architecture**: Python/FastAPI backend with PostgreSQL/Supabase database

## 🏗️ Architecture

### Backend (Python/FastAPI)

- **FastAPI**: High-performance async web framework
- **Pydantic**: Data validation and agent state management
- **PostgreSQL/Supabase**: Production-ready database
- **SQLAlchemy**: Async ORM for database operations

### AI Agents (Custom Lightweight Implementations)

- **TrendAnalyzer**: Extracts hooks, CTAs, categories and engagement heuristics
- **ContentCreator**: Generates MD Aesthetics-branded content (clinical, authoritative, compliant)
- **ComplianceAgent**: Terminology replacement + risk pattern scanning
- **EmailDispatcher**: Summarizes daily insights + generated drafts

### Frontend (Next.js)

- **Next.js 14**: React framework with TypeScript
- **Tailwind CSS**: Utility-first styling
- **Real-time Dashboard**: Live competitor monitoring and content generation

### Backend Operational Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| /viral-service/api/v1/health | GET | Health probe |
| /viral-service/api/v1/chat | POST | Conversational AI manager (with optional inline research) |
| /viral-service/api/v1/metrics | GET | In-memory counters (chat_requests, ai_calls) |

Chat request body example:
```json
{
  "messages": [{"role": "user", "content": "Your message"}],
  "invoke_research": false,
  "limit_learning": 10
}
```

Chat response body fields: `reply`, `model_used`, `research` (optional), `used_learning_items`, `timestamp`.

### AI Client Behavior (Strict Mode)

The system invokes GitHub Models directly (`https://models.github.ai/inference/chat/completions`) using `openai/gpt-4o` (full model). Behavior characteristics:

| Condition | Result |
|-----------|-------|
| Missing `GITHUB_TOKEN` | `/chat` returns 401 Unauthorized (no mock data) |
| Transient upstream/network errors (after retries) | `/chat` returns 502 Bad Gateway |
| Successful invocation | Returns model-generated reply |

Design principle: **No silent fallback**. Operational issues are surfaced immediately to facilitate rapid remediation.

### Metrics
Counters are stored in-process by default (ephemeral). Consider exporting to Prometheus / OpenTelemetry for production monitoring.

| Counter | Meaning |
|---------|---------|
| `chat_requests` | Total chat endpoint invocations (success or failure) |
| `ai_calls` | Successful upstream GitHub model completions |
| `ingested_posts` | Number of scraped posts successfully upserted |
| `analyses_created` | Number of TrendAnalyzer analyses persisted |
| `drafts_created` | Number of ContentCreator drafts persisted |
| `scrape_runs` | Number of scrape attempts triggered by scheduler or API |
| `scrape_items_collected` | Number of normalized items collected in last scrape |
| `scrape_failures` | Number of scrape errors encountered |
| `digest_runs` | Number of digest job invocations |
| `digest_emails_sent` | Number of digest emails successfully handed off to provider |

If `chat_requests` grows while `ai_calls` remains flat, investigate authentication (`GITHUB_TOKEN`) or upstream service health. If `scrape_runs` increments but `scrape_items_collected` is zero, verify the `APIFY_TOKEN` and actor IDs.


### Scheduler
The scheduler is gated by the `ENABLE_SCHEDULER` env var (set to `1` / `true` to enable). Default scheduled jobs are UTC-based:

- Scrape job: 13:00 UTC (collect competitor posts via Apify actors)
- Digest job: 13:30 UTC (compile and send daily email digest)

Only one running replica should enable the scheduler to avoid duplicated work. Times may be made configurable via `SCRAPE_CRON` and `DIGEST_CRON` environment variables in future revisions.

## Backend Compliance Layer

Compliance filter applied to every chat response:

- Replaces the term "Botox" (any casing) with "Neuromodulator".
- Flags (but does not remove) pricing references (currency symbol or explicit currency words).
- Easily extensible for more medical advertising guardrails.

Planned enhancements:

1. Return structured `compliance` metadata in ChatResponse (currently internal only).
2. External policy configuration file with hot reload.
3. Add LLM-assisted higher-order risk classification (claims, guarantees).
4. Optional moderation via provider safety endpoints.

## 🚀 Quick Start

### Prerequisites

- Python 3.12+
- Node.js 18+
- PostgreSQL database (or Supabase account)

### 1. Setup Development Environment

```bash
# Clone the repository
git clone https://github.com/ivanmolanski/adk-java.git
cd adk-java

# Setup Python backend
./setup-dev.sh

# Edit environment variables
cp .env.template .env
# Edit .env with your API keys and database credentials
```

### 2. Configure Environment Variables

Use `.env.template` as a template (already added). Copy and fill:

```bash
cp .env.template .env
```

Key variables (sanitized):

```bash
# Core
SPRING_PROFILES_ACTIVE=development
SERVER_PORT=3453
LOG_LEVEL=INFO

# GitHub AI Models (Direct GitHub Models API) - REQUIRED
# Provide a GitHub PAT with the `models` scope. The system enforces a strict no-fallback policy:
# - Model used: `openai/gpt-4o` (no "mini" or alternate providers)
# - If `GITHUB_TOKEN` is missing or invalid, requests will fail so issues are visible and remediable.
GITHUB_TOKEN=ghp_your_token_here   # Without this the /chat endpoint will return 401
AI_DEFAULT_MODEL=gpt-4o
GITHUB_MODELS_ENDPOINT=https://models.github.ai/inference/chat/completions
RUN_LIVE_AI=0                    # Set to 1 to enable live GitHub model calls in tests / runs

# Supabase / Postgres
POSTGRES_URL=postgres://user:password@host:6543/postgres?sslmode=require
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=replace-me
SUPABASE_SERVICE_ROLE_KEY=replace-me
SUPABASE_JWT_SECRET=replace-me

# Search & Scraping
GOOGLE_CSE_KEY=replace-me
GOOGLE_CSE_CX=replace-me
APIFY_TOKEN=replace-me

# Email Digest
EMAIL_ENABLED=true
DAILY_DIGEST_ENABLED=true
DIGEST_RECIPIENTS=team@example.com
EMAIL_SENDER=noreply@example.com

# Learning Store (optional override)
LEARNING_STORE_PATH=data/learning_store.jsonl
```

Sensitive production secrets must NEVER be committed; use deployment platform secret managers.

### 3. Start the Application

```bash
# Terminal 1: Start Python backend
./start-backend.sh

# Terminal 2: Start Next.js frontend  
./start-frontend.sh
```

### 4. Access the Application

- **Backend API**: <http://localhost:3453>
- **API Documentation**: <http://localhost:3453/docs>
- **Frontend Dashboard**: <http://localhost:3000>

## 📚 API Endpoints

### Health & Status

```text
GET  /api/v1/health                    # System health check
```

### Viral Content Analysis

```text
GET  /api/v1/viral/posts               # Get competitor posts
POST /api/v1/viral/analyze             # Analyze posts for trends
POST /api/v1/viral/generate            # Generate MD Aesthetics content
POST /api/v1/viral/analyze-and-generate # Complete pipeline
```

### Agent Management

```text
GET  /api/v1/agents/                   # List available agents
GET  /api/v1/agents/{agent_type}       # Get agent details
POST /api/v1/agents/pipeline/*         # Execute agent workflows
POST /api/v1/agents/{agent_type}/execute # Execute specific agent
```

## 🤖 Pydantic Agents

### TrendAnalyzer

Analyzes viral social media posts to extract:

- **Hooks**: Opening 3-second attention grabbers
- **CTAs**: Call-to-action patterns and effectiveness
- **Categories**: Content type classification (Process Demystified, Science Explained, etc.)
- **Scores**: Relevance and virality scoring for MD Aesthetics

### ContentCreator

Generates MD Aesthetics-branded content with:

- **Brand Compliance**: Automatic checking against forbidden terms (e.g., "Botox" → "Tox")
- **Service Focus**: Content tailored to specific treatments (Duo-C-Lift, SkinTyte, etc.)
- **Platform Optimization**: Instagram/TikTok specific formatting
- **Educational Value**: Clinical authority and trustworthy information

### ComplianceAgent

Validates content for:

- **Brand Guidelines**: Tone, voice, and messaging consistency
- **Medical Regulations**: Compliance with aesthetic medicine advertising rules
- **Forbidden Terms**: Automatic detection and replacement
- **Quality Assurance**: Professional standards verification

## 🎯 Competitor Monitoring

The system automatically monitors these competitor profiles:

- `_thelookaesthetics` (Instagram)
- `subtle.enhancements` (Instagram)
- `skinvitalityofficial` (Instagram/TikTok)

### Content Categories Analyzed

1. **Process Demystified**: Treatment demonstrations and procedures
2. **Science Explained**: Educational content about technologies and ingredients
3. **Transformation**: Before/after results and success stories
4. **Expert Myth-Busting**: Professional authority and trust-building content

## 📧 Automated Reporting

Daily digest emails include:

- Top 5 viral posts from competitors
- Trend analysis and engagement insights
- Generated MD Aesthetics content drafts
- Compliance-checked captions and hashtags
- Posting recommendations and timing tips

## 🛠️ Development

### Project Structure

```text
├── backend/                 # Python FastAPI backend
│   ├── main.py             # FastAPI application entry point
│   └── app/
│       ├── api/            # API route handlers
│       ├── agents/         # Pydantic-based AI agents
│       └── models/         # Database and data models
├── app/                    # Next.js frontend
│   ├── components/         # React components
│   ├── hooks/              # Custom React hooks
│   └── lib/                # Utility functions
├── src/google/adk/         # Python ADK framework (legacy)
├── requirements.txt        # Python dependencies
└── package.json           # Node.js dependencies
```

### Adding New Agents

1. Create agent class in `backend/app/agents/`
2. Inherit from Pydantic `BaseModel`
3. Define agent methods and validation
4. Add to agent registry in `app/api/agents.py`

### Database Schema

- **competitor_posts**: Scraped social media content
- **trend_analyses**: Agent analysis results
- **content_drafts**: Generated MD Aesthetics content
- **agent_runs**: Execution logs and metrics

## � GitHub Models Token & Scopes

You MUST create a Personal Access Token (classic) with the `models` scope enabled.

1. Go to GitHub → Settings → Developer Settings → Personal access tokens → **Fine-grained tokens** (or classic if `models` not exposed yet in fine-grained UI).
2. Generate a token and ensure the **`models`** scope is selected (GitHub Docs Quickstart Step 2).
3. Store it as `GITHUB_TOKEN` in `.env` (never commit real tokens).

Test it manually (replace `YOUR_GITHUB_PAT`):

```bash
curl -s -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer YOUR_GITHUB_PAT" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "Content-Type: application/json" \
  https://models.github.ai/inference/chat/completions \
  -d '{"model":"openai/gpt-4o","messages":[{"role":"user","content":"ping"}]}' | jq .choices[0].message.content
```

If you see `"The capital of France is **Paris**."`-style content, your token works.

### Troubleshooting Auth

| Symptom | Likely Cause | Action |
|---------|--------------|--------|
| 401 Unauthorized | Missing or invalid token / no `models` scope | Regenerate PAT with `models` scope |
| 403 Forbidden | Token present but insufficient repository/org permissions (rare) | Confirm scope + not expired |
| 404 Model not found | Wrong `model` field or temporary catalog change | Verify model name `openai/gpt-4o` in marketplace |
| 5xx Upstream error | GitHub service issue | Retry later / monitor status |

Use internal diagnostics:

```text
GET /viral-service/api/v1/ai/health   # Live minimal completion attempt
GET /viral-service/api/v1/ai/scopes   # Scope + status classification
```

Both endpoints intentionally perform no fallback so misconfiguration is immediately visible.

## 🔄 JS vs Python Invocation Parity

| Concern | JS Fetch | Python (httpx) |
|---------|----------|----------------|
| Endpoint | `https://models.github.ai/inference/chat/completions` | same |
| Headers | Authorization, Accept, X-GitHub-Api-Version, Content-Type | identical |
| Body keys | model, messages, temperature, max_tokens (optional) | same |
| Model | `openai/gpt-4o` | `openai/gpt-4o` |
| Error surfacing | fetch non-2xx → handled by caller | raises PermissionError/RuntimeError |

No Gemini / Vertex / Firebase paths exist in this client; architecture is intentionally lean.

## 🤝 Contributing

We welcome contributions from the community! Whether it's bug reports, feature
requests, documentation improvements, or code contributions, please see our
[**Contributing Guidelines**](./CONTRIBUTING.md) to get started.

## 📄 License

This project is licensed under the Apache 2.0 License - see the
[LICENSE](LICENSE) file for details.

--------------------------------------------------------------------------------

*Operational Principle: Fail Fast, Surface Everything.*
