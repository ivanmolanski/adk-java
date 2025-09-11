"""Scraping API Endpoints

Provides trigger and status endpoints for competitor content scraping.
"""

from fastapi import APIRouter, HTTPException
from typing import Dict, Any, List
from datetime import datetime
import logging

from app.scraping import ViralContentScraper

logger = logging.getLogger(__name__)

scraping_router = APIRouter()

_LAST_SCRAPE_STATUS: Dict[str, Any] = {
    "status": "idle",
    "last_run": None,
    "items": 0,
    "competitors": 0,
    "error": None
}

DEFAULT_COMPETITORS: List[Dict[str, str]] = [
    {"platform": "instagram", "url": "https://www.instagram.com/_thelookaesthetics"},
    {"platform": "instagram", "url": "https://www.instagram.com/skinvitality"},
    {"platform": "instagram", "url": "https://www.instagram.com/subtle.enhancements"},
    {"platform": "tiktok", "url": "https://tiktok.com/@skinvitality"}
]

@scraping_router.post("/api/scraping/trigger")
async def trigger_scraping(custom: bool = False) -> Dict[str, Any]:
    """Trigger a scraping run for competitor content."""
    global _LAST_SCRAPE_STATUS
    _LAST_SCRAPE_STATUS.update({
        "status": "running",
        "started_at": datetime.utcnow().isoformat(),
        "error": None
    })

    try:
        scraper = ViralContentScraper()
        competitors = DEFAULT_COMPETITORS
        content = scraper.scrape_competitor_content(competitors)

        _LAST_SCRAPE_STATUS.update({
            "status": "completed",
            "last_run": datetime.utcnow().isoformat(),
            "items": len(content),
            "competitors": len(competitors)
        })

        return {
            "status": "success",
            "scraped": len(content),
            "competitors": len(competitors),
            "started_at": _LAST_SCRAPE_STATUS.get("started_at"),
            "completed_at": _LAST_SCRAPE_STATUS.get("last_run")
        }
    except Exception as e:
        logger.error("Scraping error: %s", e)
        _LAST_SCRAPE_STATUS.update({
            "status": "failed",
            "error": str(e),
            "last_run": datetime.utcnow().isoformat()
        })
        raise HTTPException(status_code=500, detail=f"Scraping failed: {e}") from e

@scraping_router.get("/api/scraping/status")
async def scraping_status() -> Dict[str, Any]:
    """Return last scraping run status."""
    return _LAST_SCRAPE_STATUS
