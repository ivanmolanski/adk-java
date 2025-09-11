import os
import pytest
import sys
import asyncio
from httpx import AsyncClient

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, BASE_DIR)

@pytest.mark.asyncio
async def test_scheduler_scrape_digest_harness(monkeypatch):
    # Skip live scrape if APIFY token missing
    if not os.getenv('APIFY_TOKEN'):
        pytest.skip('APIFY_TOKEN not set; skipping scheduler scrape harness test')
    import main
    from app.scheduler import _run_scrape, _run_digest  # type: ignore
    # Run scrape (will ingest and possibly create drafts)
    await _run_scrape(main.app)  # type: ignore
    # Run digest (requires DB session)
    await _run_digest(main.app)  # type: ignore
    # Fetch metrics to ensure counters updated (best-effort)
    async with AsyncClient(app=main.app, base_url='http://test') as ac:
        m_resp = await ac.get('/viral-service/api/v1/metrics')
    assert m_resp.status_code == 200
    counters = m_resp.json()['counters']
    # If scrape ran, scrape_runs should be >=1
    if os.getenv('APIFY_TOKEN'):
        assert counters.get('scrape_runs', 0) >= 1
