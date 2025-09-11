import os
import pytest
import anyio

from httpx import AsyncClient
from httpx import ASGITransport
from backend.main import create_app

APIFY_TOKEN = os.getenv('APIFY_TOKEN')

@pytest.mark.anyio
@pytest.mark.skipif(not APIFY_TOKEN, reason="APIFY_TOKEN not set for live scrape test")
async def test_apify_scrape_ingest_flow():
    """Live test: run /scrape/apify endpoint and validate ingestion + draft generation.

    This test intentionally hits external Apify actors. It is skipped if APIFY_TOKEN
    is not present in the environment.
    """
    app = create_app()
    # Use ASGITransport for compatibility with httpx versions that don't accept `app=`
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://testserver") as ac:
        resp = await ac.post('/viral-service/api/v1/viral/scrape/apify')
        assert resp.status_code == 200, resp.text
        data = resp.json()
        assert data['status'] in {'ok', 'empty'}
        if data['status'] == 'ok':
            # Basic structural assertions
            assert data['ingested'] >= 0
            assert data['analyses'] == data['drafts']
            # Pipeline should yield at least one analysis/draft if items ingested
            if data['ingested'] > 0:
                assert data['analyses'] > 0

        # Metrics endpoint validation
        mresp = await ac.get('/viral-service/api/v1/metrics')
        assert mresp.status_code == 200
        metrics = mresp.json()['counters']
        # scrape_runs should be >=1 even if empty
        assert metrics['scrape_runs'] >= 1
        if data['status'] == 'ok':
            # items collected should match or exceed ingested count (some posts may be filtered)
            assert metrics['scrape_items_collected'] >= data['ingested']
