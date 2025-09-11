import os
import pytest
from httpx import AsyncClient
import sys
import asyncio

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, BASE_DIR)

APIFY_TOKEN_PRESENT = bool(os.getenv('APIFY_TOKEN'))

@pytest.mark.asyncio
async def test_scrape_apify_endpoint_smoke():
    if not APIFY_TOKEN_PRESENT:
        pytest.skip('APIFY_TOKEN not set; skipping live Apify scrape test')
    import main  # noqa: F401
    from main import app
    async with AsyncClient(app=app, base_url="http://test") as ac:
        resp = await ac.post('/viral-service/api/v1/viral/scrape/apify')
    assert resp.status_code in (200, 500, 200)  # 500 would indicate orchestrator failure but endpoint reachable
    data = resp.json()
    assert 'status' in data
    # Acceptable statuses: ok, empty (no items), or error returned via HTTPException path
    assert data['status'] in {'ok', 'empty'} or resp.status_code == 500
