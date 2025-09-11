import os
import sys
import pytest
from fastapi.testclient import TestClient

BASE = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, BASE)
APP_DIR = os.path.join(BASE, 'app')
sys.path.insert(0, APP_DIR)
from main import app  # type: ignore

client = TestClient(app)

@pytest.mark.skipif(not os.getenv("GITHUB_TOKEN"), reason="GITHUB_TOKEN not set for live test")
def test_ai_health_live():
    """Live integration test against GitHub Models API.

    This does NOT mock the AI client. It will perform a real network call.
    Acceptable statuses: ok (success) or auth_error (bad token/scopes).
    Any other status is flagged for investigation.
    """
    resp = client.get('/viral-service/api/v1/ai/health')
    assert resp.status_code == 200
    data = resp.json()
    assert data['status'] in {"ok", "auth_error"}, f"Unexpected status: {data}"
    if data['status'] == 'auth_error':
        # Provide guidance inline for faster iteration
        assert 'Unauthorized' in (data.get('error_message') or '') or 'Forbidden' in (data.get('error_message') or ''), \
            f"Auth error did not contain expected marker: {data}"
