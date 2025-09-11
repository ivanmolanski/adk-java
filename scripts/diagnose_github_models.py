#!/usr/bin/env python3
"""Diagnostic script for GitHub Models (GPT-4o) authentication & endpoint/model resolution.

Iterates over candidate endpoints and model ids to discover a working combination
given the current GITHUB_TOKEN in environment/.env.

Outputs a compact matrix of (endpoint, model) -> status, http code, truncated error or sample content.

Usage:
  python scripts/diagnose_github_models.py

NOTE: Does not log or print the token.
"""

from __future__ import annotations

import os
import json
import itertools
from typing import List, Dict
import httpx
from time import perf_counter

try:
    from dotenv import load_dotenv  # type: ignore
    load_dotenv()
except Exception:  # pragma: no cover
    pass

TOKEN = os.getenv("GITHUB_TOKEN")

if not TOKEN:
    raise SystemExit("GITHUB_TOKEN not set in environment")

# Candidate endpoints & model ids (ordered by likelihood)
ENDPOINTS: List[str] = [
    # Current code default
    "https://models.github.ai/inference/chat/completions",
    # Azure-hosted inference domain variants observed in other integrations
    "https://models.inference.ai.azure.com/v1/chat/completions",
    "https://models.inference.ai.azure.com/chat/completions",
]

MODELS: List[str] = [
    "openai/gpt-4o",  # provider-prefixed full model
    "gpt-4o",          # bare id
]

HEADERS_BASE = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json",
    "Accept": "application/json",
    # Version header included for legacy endpoint; harmless elsewhere.
    "X-GitHub-Api-Version": "2022-11-28",
    "User-Agent": "diagnostic-md-aesthetics/1.0",
}

PAYLOAD_TEMPLATE = lambda model: {
    "model": model,
    "messages": [
        {"role": "system", "content": "You are a concise assistant."},
        {"role": "user", "content": "ping"},
    ],
    "temperature": 0.0,
    "max_tokens": 4,
}

results: List[Dict[str, str]] = []

def truncate(text: str, n: int = 140) -> str:
    return text if len(text) <= n else text[: n - 3] + "..."

async def probe():
    async with httpx.AsyncClient(timeout=30) as client:
        for ep, model in itertools.product(ENDPOINTS, MODELS):
            payload = PAYLOAD_TEMPLATE(model)
            start = perf_counter()
            status = "unknown"
            http_code = None
            note = ""
            try:
                resp = await client.post(ep, headers=HEADERS_BASE, json=payload)
                http_code = resp.status_code
                data_text = truncate(resp.text)
                if resp.status_code == 200:
                    try:
                        data = resp.json()
                        sample = data.get("choices", [{}])[0].get("message", {}).get("content")
                        if sample:
                            note = truncate(sample)
                            status = "ok"
                        else:
                            status = "ok-no-content"
                    except Exception:
                        status = "ok-nonjson"
                        note = data_text
                elif resp.status_code == 401:
                    status = "unauthorized"
                    note = data_text
                elif resp.status_code == 403:
                    status = "forbidden"
                    note = data_text
                elif resp.status_code == 404:
                    status = "not-found"
                    note = data_text
                elif resp.status_code == 429:
                    status = "rate-limit"
                    note = data_text
                else:
                    status = "error"
                    note = data_text
            except Exception as exc:  # noqa: BLE001
                status = "exception"
                note = truncate(repr(exc))
            elapsed_ms = int((perf_counter() - start) * 1000)
            results.append({
                "endpoint": ep,
                "model": model,
                "status": status,
                "http": str(http_code),
                "latency_ms": str(elapsed_ms),
                "note": note,
            })

def main():
    import asyncio
    print("🔎 Probing GitHub Models endpoints/models (without exposing token)...")
    asyncio.run(probe())
    # Determine best success (if any) or categorize failures
    any_ok = any(r["status"].startswith("ok") for r in results)
    print()
    print("RESULT MATRIX:")
    for r in results:
        print(f"- endpoint={r['endpoint']} model={r['model']} status={r['status']} http={r['http']} latency={r['latency_ms']}ms note={r['note']}")
    print()
    if any_ok:
        print("✅ At least one combination succeeded. Update settings.github_models_endpoint & model accordingly.")
    else:
        # Group statuses to guide next action
        status_set = {r['status'] for r in results}
        print(f"❌ No successful responses. Status summary: {', '.join(status_set)}")
        if status_set == {"unauthorized"}:
            print("➡ All attempts 401 Unauthorized: Token likely missing 'models' scope or using deprecated endpoint requiring new token generation.")
        elif status_set == {"forbidden"} or "forbidden" in status_set:
            print("➡ 403 responses: Token recognized but lacks necessary scope/feature enablement (request access to GitHub Models preview).")
        elif "not-found" in status_set:
            print("➡ 404 for model: Endpoint reachable but model id variant not recognized; verify published model list.")
        else:
            print("➡ Mixed failures: Check network egress, firewall, or preview program enrollment.")

if __name__ == "__main__":  # pragma: no cover
    main()
