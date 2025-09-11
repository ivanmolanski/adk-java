#!/usr/bin/env python3
"""FastAPI backend entrypoint for MD Aesthetics Viral Content System."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import os
import logging
from typing import Dict, Any

from app.config.settings import settings  # type: ignore

logging.basicConfig(level=getattr(logging, settings.log_level.upper(), logging.INFO))
logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
  """Create and configure the FastAPI application."""
  app_ = FastAPI(
    title="MD Aesthetics Viral Content API",
    description="Python/FastAPI backend for competitive intelligence and content generation",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
  )

  app_.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # TODO: restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
  )

  @app_.get("/viral-service/api/v1/health")
  def health_check() -> Dict[str, Any]:  # noqa: D401
    return {
      "status": "healthy",
      "service": "md-aesthetics-viral-api",
      "version": "2.0.0",
      "backend": "FastAPI/Python",
      "database": "PostgreSQL/Supabase",
    }

  # Simple in-memory metrics store (thread-safe enough for single-process usage)
  from collections import defaultdict
  metrics_store: Dict[str, int] = defaultdict(int)
  # Pre-register known counters for clarity
  for key in [
    "chat_requests",
    "ai_calls",
    "auth_failures",
    "upstream_failures",
    "rate_limit_events",
    "ingested_posts",
    "analyses_created",
    "drafts_created",
    "digest_emails_sent",
    # Scraping metrics
    "scrape_runs",
    "scrape_failures",
    "scrape_items_collected"
  ]:
    metrics_store[key] = 0
  app_.state.metrics = metrics_store  # expose for other routers

  @app_.get("/viral-service/api/v1/metrics")
  def metrics() -> Dict[str, Any]:
    """Return internal counters for observability."""
    return {"counters": dict(metrics_store)}

  @app_.get("/viral-service/api/v1/ai/health")
  async def ai_health() -> Dict[str, Any]:  # pragma: no cover - network call
    """Lightweight runtime probe for GitHub Models API.

    Executes a minimal prompt against the configured model. This intentionally
    does NOT introduce any fallback; failures are surfaced directly so ops can
    alert on upstream outages or credential issues.
    """
    from time import perf_counter
    from app.services.ai_client import AIClient  # local import to avoid startup cost if unused

    client = AIClient()
    start = perf_counter()
    status = "ok"
    error_type: str | None = None
    error_message: str | None = None
    try:
      content = await client.generate(messages=[{"role": "user", "content": "ping"}], max_tokens=4, temperature=0.0)
    except PermissionError as exc:  # auth / 401
      status = "auth_error"
      error_type = exc.__class__.__name__
      error_message = str(exc)
      content = None
    except Exception as exc:  # noqa: BLE001
      status = "error"
      error_type = exc.__class__.__name__
      error_message = str(exc)
      content = None
    finally:
      await client.close()
    latency_ms = round((perf_counter() - start) * 1000, 2)
    return {
      "status": status,
      "latency_ms": latency_ms,
      "model": getattr(client, "default_model", None),
      "error_type": error_type,
      "error_message": error_message,
      "sample": content,
    }

  @app_.get("/viral-service/api/v1/ai/scopes")
  async def ai_scopes() -> Dict[str, Any]:  # pragma: no cover - network call
    """Return raw authorization / scope diagnostic information.

    Performs a lightweight HEAD (or minimal POST fallback) request against the
    configured GitHub Models endpoint to surface headers such as
    X-OAuth-Scopes and X-Accepted-OAuth-Scopes. This helps differentiate
    between credential *presence* and *permission* issues without consuming
    model quota.
    """
    import httpx
    token = settings.github_token
    if not token:
      return {"status": "missing_token"}
    # Prefer a HEAD request; if unsupported, fall back to POST with max-tokens=1
    headers = {
      "Authorization": f"Bearer {token}",
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": settings.github_models_api_version,
      "User-Agent": "md-aesthetics-viral-system/1.0",
      "Content-Type": "application/json",
    }
    async with httpx.AsyncClient(timeout=10) as hc:
      try:
        resp = await hc.request("HEAD", settings.github_models_endpoint, headers=headers)
        if resp.status_code == 405:  # Method not allowed, do minimal POST
          resp = await hc.post(settings.github_models_endpoint, headers=headers, json={"model": "openai/gpt-4o", "messages": [{"role": "user", "content": "."}], "max_tokens": 1, "temperature": 0})
      except Exception as exc:  # noqa: BLE001
        return {"status": "network_error", "error": str(exc)}
    scope_headers = {k: v for k, v in resp.headers.items() if k.lower() in {"x-oauth-scopes", "x-accepted-oauth-scopes"}}
    status: str
    if resp.status_code == 401:
      status = "unauthorized"
    elif resp.status_code == 403:
      status = "forbidden"
    elif resp.status_code == 404:
      status = "not_found"
    elif resp.status_code >= 500:
      status = "upstream_error"
    else:
      status = "ok" if 200 <= resp.status_code < 300 else f"http_{resp.status_code}"
    return {
      "status": status,
      "http_status": resp.status_code,
      "scopes": scope_headers,
      "rate_limit_remaining": resp.headers.get("x-ratelimit-remaining"),
      "rate_limit": resp.headers.get("x-ratelimit-limit"),
    }

  # Import routers independently so one failing import (e.g. optional scraping deps) doesn't block others.
  from typing import Sequence

  def _safe_include(import_path: str, attr: str, *, prefix: str | None = None, tags: Sequence[str] | None = None):  # pragma: no cover - small helper
    try:
      module = __import__(import_path, fromlist=[attr])
      router_obj = getattr(module, attr)
      include_tags = list(tags) if tags else None
      if prefix:
        app_.include_router(router_obj, prefix=prefix, tags=include_tags)  # type: ignore[arg-type]
      else:
        app_.include_router(router_obj, tags=include_tags)  # type: ignore[arg-type]
      logger.info("Registered router %s from %s", attr, import_path)
    except ImportError as imp:  # noqa: PERF203
      logger.warning("Skipping router %s (%s): %s", attr, import_path, imp)
    except Exception as exc:  # noqa: BLE001
      logger.error("Unexpected error registering router %s (%s): %s", attr, import_path, exc)

  _safe_include("app.api.chat", "chat_router", prefix="/viral-service/api/v1", tags=["chat"])
  _safe_include("app.api.viral", "router", prefix="/viral-service/api/v1/viral", tags=["viral"])
  _safe_include("app.api.viral_research", "research_router", prefix="/viral-service/api/v1/viral", tags=["viral-research"])
  _safe_include("app.api.agents", "router", prefix="/viral-service/api/v1/agents", tags=["agents"])
  _safe_include("app.api.viral_alias", "alias_router", tags=["viral-alias"])
  _safe_include("app.api.scraping_api", "scraping_router", tags=["scraping"])
  _safe_include("app.api.trends", "trends_router", prefix="/viral-service/api/v1", tags=["trends"])
  _safe_include("app.api.ingest", "ingest_router", prefix="/viral-service/api/v1", tags=["ingest"])

  return app_


app = create_app()

try:  # Scheduler setup
  from app.scheduler import init_scheduler

  @app.on_event("startup")
  async def _start_scheduler():  # pragma: no cover
    init_scheduler(app)
except (ImportError, RuntimeError) as sched_exc:  # pragma: no cover
  logger.warning("Scheduler initialization skipped: %s", sched_exc)


if __name__ == "__main__":  # pragma: no cover
  port = int(os.getenv("PORT", str(settings.server_port)))
  host = os.getenv("HOST", "0.0.0.0")
  logger.info("Starting MD Aesthetics Viral Content API on %s:%s", host, port)
  uvicorn.run("main:app", host=host, port=port, reload=False, log_level="info")