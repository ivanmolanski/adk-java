import os
import sys
import asyncio
import pytest

# Adjust sys.path to import backend app modules
BASE = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, BASE)
APP_DIR = os.path.join(BASE, 'app')
sys.path.insert(0, APP_DIR)

# Guard: only run when explicitly enabled to avoid accidental quota usage
RUN_LIVE = os.getenv("RUN_LIVE_AI", "0").lower() in {"1", "true", "yes", "on"}
TOKEN_PRESENT = bool(os.getenv("GITHUB_TOKEN"))
print(f"[debug] RUN_LIVE_AI env raw={os.getenv('RUN_LIVE_AI')} parsed={RUN_LIVE} token_present={TOKEN_PRESENT}")


@pytest.mark.asyncio
async def test_github_models_direct():
    """Direct network integration test with AIClient.generate().

    Acceptable outcomes:
      - Success: returns non-empty content (status implicitly OK)
      - PermissionError: token invalid or missing models scope (reported but not failure)
    Any other exception causes test failure.
    """
    from app.services.ai_client import AIClient
    if not TOKEN_PRESENT:
        pytest.skip("GITHUB_TOKEN not present")
    if not RUN_LIVE:
        pytest.skip("RUN_LIVE_AI flag not enabled")
    client = AIClient()
    try:
        content = await client.generate(messages=[{"role": "user", "content": "Respond ONLY with OK"}], max_tokens=4, temperature=0)
        print(f"[live_ai_test] model response raw={content!r}")
        assert isinstance(content, str) and 'OK' in content.upper()
    except PermissionError as exc:
        # Treat auth issues as an informative skip-style assertion rather than hard failure
        pytest.skip(f"PermissionError invoking GitHub Models API: {exc}")
    finally:
        await client.close()
