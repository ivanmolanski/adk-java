from pydantic_settings import BaseSettings
from functools import lru_cache
import os

class Settings(BaseSettings):
    """Application settings with Pydantic validation"""
    
    # Application
    app_name: str = "MD Aesthetics Viral Forge"
    environment: str = "development"
    debug: bool = True
    server_port: int = 3453
    
    # Database - PostgreSQL from env
    postgres_user: str = "postgres"
    postgres_password: str = "lyZEl6YFdixSZtdQ"
    postgres_database: str = "postgres"
    postgres_host: str = "db.fjszwtosndwcozodmxox.supabase.co"
    postgres_port: int = 6543
    
    @property
    def database_url(self) -> str:
        return f"postgresql+asyncpg://{self.postgres_user}:{self.postgres_password}@{self.postgres_host}:{self.postgres_port}/{self.postgres_database}?sslmode=require"
    
    # API Configuration
    openrouter_api_key: str = ""
    
    # Social Media APIs
    apify_token: str = ""
    google_cse_key: str = ""
    google_cse_cx: str = ""
    
    # OAuth2 Configuration
    tiktok_client_id: str = ""
    tiktok_client_secret: str = ""
    instagram_client_id: str = ""
    instagram_client_secret: str = ""
    oauth2_redirect_uri_base: str = "http://localhost:8080/viral-service"
    
    # Email Configuration
    email_enabled: bool = True
    daily_digest_enabled: bool = True
    digest_recipients: str = "christine.carrer@hotmail.com,dalkeith@golden.net"
    email_sender: str = "noreply@mdaesthetics.ca"
    
    # Frontend URLs
    next_public_viral_service_url: str = "http://localhost:3453/viral-service"
    
    class Config:
        env_file = ".env"
        case_sensitive = False
        extra = "ignore"  # Ignore extra environment variables

@lru_cache()
def get_settings() -> Settings:
    """Get cached settings instance"""
    return Settings()