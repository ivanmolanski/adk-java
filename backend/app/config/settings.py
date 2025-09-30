"""Centralized application settings using Pydantic BaseSettings.

Loads environment variables from the process environment. The project policy
is NOT to auto-create an example file here (user explicitly declined). The
`.env` at repo root is expected to be managed manually and loaded by
`python-dotenv` if desired before app startup, otherwise container/hosting
platform should inject variables.
"""
from __future__ import annotations

from functools import lru_cache
from pydantic import Field, ConfigDict
from pydantic_settings import BaseSettings
from typing import List, Optional
import os
try:
  # Load .env from repo root if present so developers can keep secrets in .env
  # during local development. This is optional and will be skipped if python-dotenv
  # is not installed in the environment.
  from dotenv import load_dotenv  # type: ignore
  load_dotenv()
except Exception:
  # noop: dotenv not available or .env not present
  pass


class Settings(BaseSettings):
  # Core service
  app_name: str = Field(default='MD Aesthetics Viral Content API', validation_alias='APP_NAME')
  environment: str = Field(default='development', validation_alias='SPRING_PROFILES_ACTIVE')
  log_level: str = Field(default='INFO', validation_alias='LOG_LEVEL')
  server_port: int = Field(default=3453, validation_alias='SERVER_PORT')

  # AI / Model
  github_token: Optional[str] = Field(default=None, validation_alias='GITHUB_TOKEN')
  ai_default_model: str = Field(default='gpt-4o', validation_alias='AI_DEFAULT_MODEL')
  github_models_endpoint: str = Field(default='https://models.github.ai/inference/chat/completions', validation_alias='GITHUB_MODELS_ENDPOINT')
  github_models_api_version: str = Field(default='2022-11-28', validation_alias='GITHUB_MODELS_API_VERSION')
  # Optional: if set, requests will be attributed to this organization using
  # the /orgs/{org}/inference/chat/completions endpoint instead of the
  # top-level /inference/chat/completions path.
  github_models_org: Optional[str] = Field(default=None, validation_alias='GITHUB_MODELS_ORG')
  ai_http_timeout: float = Field(default=60.0, validation_alias='AI_HTTP_TIMEOUT')
  ai_http_retries: int = Field(default=3, validation_alias='AI_HTTP_RETRIES')

  # Email / Digest
  email_enabled: bool = Field(default=True, validation_alias='EMAIL_ENABLED')
  daily_digest_enabled: bool = Field(default=True, validation_alias='DAILY_DIGEST_ENABLED')
  digest_recipients: str = Field(default='christine.carrer@hotmail.com,dalkeith@golden.net', validation_alias='DIGEST_RECIPIENTS')
  email_sender: str = Field(default='noreply@mdaesthetics.ca', validation_alias='EMAIL_SENDER')

  # Database / Supabase / Postgres
  postgres_url: Optional[str] = Field(default=None, validation_alias='POSTGRES_URL')
  postgres_pool_url: Optional[str] = Field(default=None, validation_alias='POSTGRES_PRISMA_URL')
  postgres_direct_url: Optional[str] = Field(default=None, validation_alias='POSTGRES_URL_NON_POOLING')
  supabase_url: Optional[str] = Field(default=None, validation_alias='SUPABASE_URL')
  supabase_anon_key: Optional[str] = Field(default=None, validation_alias='SUPABASE_ANON_KEY')
  supabase_service_role_key: Optional[str] = Field(default=None, validation_alias='SUPABASE_SERVICE_ROLE_KEY')
  supabase_jwt_secret: Optional[str] = Field(default=None, validation_alias='SUPABASE_JWT_SECRET')

  # Social Auth
  tiktok_client_id: Optional[str] = Field(default=None, validation_alias='TIKTOK_CLIENT_ID')
  tiktok_client_secret: Optional[str] = Field(default=None, validation_alias='TIKTOK_CLIENT_SECRET')
  instagram_client_id: Optional[str] = Field(default=None, validation_alias='INSTAGRAM_CLIENT_ID')
  instagram_client_secret: Optional[str] = Field(default=None, validation_alias='INSTAGRAM_CLIENT_SECRET')

  # External APIs
  google_cse_key: Optional[str] = Field(default=None, validation_alias='GOOGLE_CSE_KEY')
  google_cse_cx: Optional[str] = Field(default=None, validation_alias='GOOGLE_CSE_CX')
  apify_token: Optional[str] = Field(default=None, validation_alias='APIFY_TOKEN')

  model_config = ConfigDict(case_sensitive=False)

  def recipients_list(self) -> List[str]:
    return [r.strip() for r in self.digest_recipients.split(',') if r.strip()]


@lru_cache()
def get_settings() -> Settings:
  s = Settings()
  # Normalize quoted tokens (user may wrap secrets in quotes in .env)
  if s.github_token and s.github_token.startswith(('"', "'")) and s.github_token.endswith(('"', "'")):
    s.github_token = s.github_token[1:-1]
  return s


settings = get_settings()
