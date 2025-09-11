"""AI Client Abstraction.

Production-ready lightweight client for invoking GitHub Models (gpt-4o) and
future providers through a consistent async interface.

Design Goals:
 - Zero heavy vendor SDK dependency (privacy + portability)
 - Simple retries with exponential backoff
 - Strict failure transparency (no silent mock/fallback)
 - Easy to extend for additional models/providers
"""

from __future__ import annotations

import logging
import asyncio
from typing import Dict, Any, List, Optional
import json
import httpx

from app.config.settings import settings

logger = logging.getLogger(__name__)


class AIClient:
    """Unified facade for AI model invocations.

    Usage:
        client = AIClient()
        text = await client.generate(messages=[{"role":"user","content":"Hello"}])
    """

    def __init__(self):
        self.github_token = settings.github_token
        self.openai_api_key = None  # reserved future
        self.default_model = settings.ai_default_model
        self.github_endpoint = settings.github_models_endpoint
        self.github_api_version = settings.github_models_api_version
        self.timeout_seconds = settings.ai_http_timeout
        self.max_retries = settings.ai_http_retries
        self._client: Optional[httpx.AsyncClient] = None

    async def _ensure_client(self):
        if self._client is None:
            self._client = httpx.AsyncClient(timeout=self.timeout_seconds)

    async def generate(self, *, messages: List[Dict[str, str]], model: Optional[str] = None, temperature: float = 0.8, max_tokens: int = 1024) -> str:
        """Generate a completion for provided messages."""
        model = model or self.default_model
        # Only allow the explicitly configured model to be used. No fallbacks.
        if model != self.default_model:
            raise ValueError(f"Unsupported model: {model}. Allowed: {self.default_model}")

        # Normalize model id to provider form (e.g. 'openai/gpt-4o')
        provider_model_id = self._normalize_model_id(self.default_model)
        # Currently we only support GitHub-hosted OpenAI models (openai/*).
        if provider_model_id.endswith("gpt-4o"):
            return await self._invoke_github_gpt_4o(messages, provider_model_id=provider_model_id, temperature=temperature, max_tokens=max_tokens)
        raise ValueError(f"Model mapping for {self.default_model} not implemented")

    def _normalize_model_id(self, model: str) -> str:
        """Return a provider-prefixed model id.

        Accepts inputs like 'gpt-4o' or 'openai/gpt-4o' (or mistaken variants) and
        returns a normalized id such as 'openai/gpt-4o'. This keeps the rest of the
        client focused on provider ids when forming requests.
        """
        if not model:
            raise ValueError("Empty model id")
        m = model.strip().lower()
        # Fix accidental 'open-ai' typo
        m = m.replace('open-ai/', 'openai/')
        # If already provider-prefixed, return as-is
        if '/' in m:
            return m
        # Otherwise assume OpenAI family
        return f"openai/{m}"

    async def _invoke_github_gpt_4o(self, messages: List[Dict[str, str]], *, provider_model_id: str, temperature: float, max_tokens: int) -> str:
        if not self.github_token:
            raise PermissionError("GITHUB_TOKEN not configured; cannot invoke GitHub Models API")
        payload = {
            "model": provider_model_id,
            "messages": messages,
            "temperature": temperature,
            "max_tokens": max_tokens,
        }
        await self._ensure_client()
        backoff = 1.0
        last_error: Optional[Exception] = None
        for attempt in range(1, self.max_retries + 1):
            try:
                assert self._client is not None, "HTTP client not initialized"
                # choose endpoint: org attributed if configured
                endpoint = self.github_endpoint
                if settings.github_models_org:
                    # ensure we use the /orgs/{org}/inference/chat/completions path
                    org = settings.github_models_org
                    endpoint = f"https://models.github.ai/orgs/{org}/inference/chat/completions"

                resp = await self._client.post(
                    endpoint,
                    headers={
                        "Authorization": f"Bearer {self.github_token}",
                        "Content-Type": "application/json",
                        "Accept": "application/vnd.github+json",
                        "X-GitHub-Api-Version": self.github_api_version,
                        "User-Agent": "md-aesthetics-viral-system/1.0",
                    },
                    json=payload,
                )
                if resp.status_code >= 500:
                    raise RuntimeError(f"Server error {resp.status_code}: {resp.text}")
                if resp.status_code == 401:
                    raise PermissionError(f"Unauthorized (401) GitHub Models API: {resp.text}")
                if resp.status_code == 403:
                    raise PermissionError(f"Forbidden (403) likely missing scope: {resp.text}")
                if resp.status_code == 404:
                    # Do not attempt fallback to non-prefixed model ids. Fail fast.
                    raise RuntimeError(f"Model not found (404): {resp.text}")
                if resp.status_code == 429:
                    logger.warning("Rate limited (429) invoking GitHub Models API")
                    raise RuntimeError("Rate limited")
                if resp.status_code >= 400:
                    raise RuntimeError(f"Client error {resp.status_code}: {resp.text}")
                data = resp.json()
                choices = data.get("choices") or []
                if not choices:
                    raise ValueError("No choices in response payload")
                message = choices[0].get("message") or {}
                content = message.get("content")
                if not content:
                    raise ValueError("Missing content in first choice")
                return content
            except (httpx.RequestError, httpx.HTTPStatusError, RuntimeError, ValueError, PermissionError) as e:
                last_error = e
                logger.error(
                    "Attempt %d/%d GitHub gpt-4o failed endpoint=%s model=%s error=%s", attempt, self.max_retries, self.github_endpoint, payload.get('model'), e
                )
                if attempt >= self.max_retries:
                    break
                await asyncio.sleep(backoff)
                backoff *= 2
        logger.error("Exhausted retries for GitHub gpt-4o. Raising last error. last_error=%s", last_error)
        if last_error:
            raise last_error
        raise RuntimeError("GitHub gpt-4o invocation failed for unknown reasons")

    async def close(self):
        if self._client:
            await self._client.aclose()

    async def __aenter__(self):
        await self._ensure_client()
        return self

    async def __aexit__(self, exc_type, exc, tb):
        await self.close()

    @staticmethod
    def extract_json_block(text: str) -> Optional[Any]:
        if not text:
            return None
        start = text.find("{")
        end = text.rfind("}")
        if start == -1 or end == -1 or end <= start:
            return None
        snippet = text[start : end + 1]
        try:
            return json.loads(snippet)
        except json.JSONDecodeError:
            return None
