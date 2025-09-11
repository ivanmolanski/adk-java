#!/usr/bin/env bash
# Simple token validator for GitHub + GitHub Models
# Usage:
#   TOKEN=ghp_xxx ./scripts/validate_github_token.sh
# or
#   ./scripts/validate_github_token.sh  # will read from .env if present

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [[ -z "${1:-}" ]]; then
  if [[ -f "$ENV_FILE" ]]; then
    # shellcheck disable=SC1090
    source "$ENV_FILE"
  fi
  if [[ -z "${GITHUB_TOKEN:-}" ]]; then
    echo "ERROR: No token provided. Pass as first arg or set GITHUB_TOKEN in .env or env." >&2
    echo "Usage: TOKEN=ghp_... $0" >&2
    exit 2
  fi
else
  GITHUB_TOKEN="$1"
fi

MODEL_ENDPOINT="${GITHUB_MODELS_ENDPOINT:-https://models.github.ai/inference/chat/completions}"

echo "Checking GitHub REST /user..."
curl -i -H "Authorization: Bearer $GITHUB_TOKEN" -H "User-Agent: validate-token-script" https://api.github.com/user || true
echo
echo "Checking Models catalog..."
curl -i -H "Authorization: Bearer $GITHUB_TOKEN" -H "Accept: application/vnd.github+json" https://models.github.ai/catalog/models || true
echo
echo "Attempting a small chat POST to Models endpoint ($MODEL_ENDPOINT) with model openai/gpt-4o..."
curl -i -X POST "$MODEL_ENDPOINT" \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/vnd.github+json" \
  -H "User-Agent: validate-token-script" \
  -d '{"model":"openai/gpt-4o","messages":[{"role":"user","content":"Health check"}]}' || true

echo
echo "Done. If you see 401 from /user, the PAT is invalid or not authorized for API access."
