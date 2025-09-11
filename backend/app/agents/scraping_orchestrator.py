"""ScrapingOrchestrator Agent

Uses Apify actors to gather recent Instagram & TikTok posts for the
configured competitor profiles and hashtags then normalizes and returns
them for downstream pipeline ingestion.

Environment Variables:
  APIFY_TOKEN              - (required) Apify API token
  APIFY_INSTAGRAM_ACTOR    - (default apify/instagram-posts-scraper)
  APIFY_TIKTOK_ACTOR       - (default apify/tiktok-scraper)
  APIFY_MAX_ITEMS          - (default 50) per platform

This module does not persist; callers should forward results to the
pipeline.process_posts() function.
"""
from __future__ import annotations

from typing import List, Dict, Any, Optional
import os
import asyncio
import logging
import httpx
from datetime import datetime, timezone

logger = logging.getLogger(__name__)


DEFAULT_IG_PROFILES = [
    "_thelookaesthetics",
    "subtle.enhancements",
    "skinvitality",
]

DEFAULT_HASHTAGS = [
    "torontoaesthetics",
    "whitbyaesthetics",
    "durhamregion",
    "torontomedspa",
    "whitbymedspa",
    "skintyte",
    "ultherapy",
    "radiesse",
    "duoclift",
]


class ScrapingOrchestrator:
    def __init__(self,
                 apify_token: Optional[str] = None,
                 instagram_actor: Optional[str] = None,
                 tiktok_actor: Optional[str] = None,
                 max_items: Optional[int] = None) -> None:
        self.apify_token = apify_token or os.getenv("APIFY_TOKEN")
        self.instagram_actor = instagram_actor or os.getenv("APIFY_INSTAGRAM_ACTOR", "apify/instagram-posts-scraper")
        self.tiktok_actor = tiktok_actor or os.getenv("APIFY_TIKTOK_ACTOR", "apify/tiktok-scraper")
        self.max_items = int(max_items or os.getenv("APIFY_MAX_ITEMS", "50"))
        if not self.apify_token:
            raise RuntimeError("APIFY_TOKEN not configured")

    async def _start_actor(self, client: httpx.AsyncClient, actor_id: str, payload: Dict[str, Any]) -> str:
        url = f"https://api.apify.com/v2/acts/{actor_id.replace('/', '~')}/runs?token={self.apify_token}"
        r = await client.post(url, json=payload, timeout=60)
        r.raise_for_status()
        data = r.json()
        run_id = data.get("data", {}).get("id")
        if not run_id:
            raise RuntimeError(f"Failed to start actor {actor_id}: {data}")
        return run_id

    async def _wait_for_run(self, client: httpx.AsyncClient, run_id: str) -> Dict[str, Any]:
        status_url = f"https://api.apify.com/v2/actor-runs/{run_id}?token={self.apify_token}"
        for _ in range(60):  # up to ~5 min (60 * 5s)
            r = await client.get(status_url, timeout=30)
            r.raise_for_status()
            data = r.json().get("data", {})
            status = data.get("status")
            if status in {"SUCCEEDED", "FAILED", "TIMED_OUT", "ABORTED"}:
                return data
            await asyncio.sleep(5)
        raise TimeoutError(f"Actor run {run_id} did not finish in time")

    async def _fetch_items(self, client: httpx.AsyncClient, dataset_id: str) -> List[Dict[str, Any]]:
        items_url = f"https://api.apify.com/v2/datasets/{dataset_id}/items?token={self.apify_token}&format=json"
        r = await client.get(items_url, timeout=60)
        r.raise_for_status()
        return r.json()

    def _normalize_instagram(self, item: Dict[str, Any]) -> Dict[str, Any]:
        # Actor specific structure may vary; attempt broad field mapping.
        caption = item.get("caption") or item.get("text") or ""
        hashtags = list({h for h in (item.get("hashtags") or [])})
        return {
            "id": item.get("id") or item.get("shortCode") or item.get("url"),
            "platform": "instagram",
            "profile": item.get("ownerUsername") or item.get("username"),
            "caption": caption,
            "hashtags": hashtags,
            "engagement_rate": 0.0,  # Can compute later if likes/comments available
            "likes": item.get("likesCount") or 0,
            "comments": item.get("commentsCount") or 0,
            "shares": 0,
            "views": item.get("videoViewCount") or 0,
            "post_url": item.get("url") or item.get("link"),
            "scraped_at": datetime.now(timezone.utc).isoformat(),
        }

    def _normalize_tiktok(self, item: Dict[str, Any]) -> Dict[str, Any]:
        desc = item.get("description") or item.get("text") or ""
        hashtags = list({h.get('name') if isinstance(h, dict) else h for h in (item.get("hashtags") or []) if h})
        return {
            "id": item.get("id") or item.get("videoId") or item.get("url"),
            "platform": "tiktok",
            "profile": item.get("authorUsername") or item.get("author") or item.get("username"),
            "caption": desc,
            "hashtags": hashtags,
            "engagement_rate": 0.0,
            "likes": item.get("diggCount") or item.get("likes") or 0,
            "comments": item.get("commentCount") or 0,
            "shares": item.get("shareCount") or 0,
            "views": item.get("playCount") or item.get("views") or 0,
            "post_url": item.get("url") or item.get("shareUrl"),
            "scraped_at": datetime.now(timezone.utc).isoformat(),
        }

    async def scrape(self,
                     instagram_profiles: Optional[List[str]] = None,
                     hashtags: Optional[List[str]] = None,
                     include_tiktok: bool = False) -> List[Dict[str, Any]]:
        instagram_profiles = instagram_profiles or DEFAULT_IG_PROFILES
        hashtags = hashtags or DEFAULT_HASHTAGS
        results: List[Dict[str, Any]] = []
        async with httpx.AsyncClient() as client:
            # Instagram
            ig_payload = {
                "usernames": instagram_profiles,
                "hashtags": hashtags,
                "resultsLimit": self.max_items,
            }
            try:
                ig_run = await self._start_actor(client, self.instagram_actor, ig_payload)
                ig_data = await self._wait_for_run(client, ig_run)
                if ig_data.get("status") == "SUCCEEDED":
                    dataset_id = ig_data.get("defaultDatasetId")
                    if dataset_id:
                        ig_items = await self._fetch_items(client, dataset_id)
                        for it in ig_items[: self.max_items]:
                            try:
                                results.append(self._normalize_instagram(it))
                            except Exception as norm_err:  # noqa: BLE001
                                logger.debug("Skipping IG item normalization error: %s", norm_err)
                else:
                    logger.warning("Instagram actor run ended with status=%s", ig_data.get("status"))
            except Exception as e:  # noqa: BLE001
                logger.error("Instagram scraping failed: %s", e)

            if include_tiktok:
                tk_payload = {
                    "profiles": instagram_profiles,  # reuse list; adjust for specific tiktok usernames if available
                    "resultsLimit": self.max_items,
                }
                try:
                    tk_run = await self._start_actor(client, self.tiktok_actor, tk_payload)
                    tk_data = await self._wait_for_run(client, tk_run)
                    if tk_data.get("status") == "SUCCEEDED":
                        dataset_id = tk_data.get("defaultDatasetId")
                        if dataset_id:
                            tk_items = await self._fetch_items(client, dataset_id)
                            for it in tk_items[: self.max_items]:
                                try:
                                    results.append(self._normalize_tiktok(it))
                                except Exception as norm_err:  # noqa: BLE001
                                    logger.debug("Skipping TikTok item normalization error: %s", norm_err)
                    else:
                        logger.warning("TikTok actor run ended with status=%s", tk_data.get("status"))
                except Exception as e:  # noqa: BLE001
                    logger.error("TikTok scraping failed: %s", e)

        logger.info("ScrapingOrchestrator collected %d items", len(results))
        return results
