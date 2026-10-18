"""
Application configuration — reads from environment variables.

Build variants
--------------
ENV=local  (default)
    • DEBUG defaults to True
    • Hot-reload via uvicorn --reload (set in docker-compose.yml)
    • Local PostgreSQL container
    • Local file storage
    • env_file loads .env.local (preferred) or .env (legacy fallback)

ENV=cloud
    • DEBUG defaults to False
    • Gunicorn/uvicorn multi-worker (set in Dockerfile prod stage CMD)
    • External managed PostgreSQL (DATABASE_URL injected by cloud provider)
    • S3-compatible storage
    • All secrets injected via environment — no .env file on disk

Copy the relevant template before running:
  Local:  cp backend/.env.local.example backend/.env.local
  Cloud:  set env vars in your cloud provider / CI secret store
"""

import os
from typing import Literal, Optional

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # ------------------------------------------------------------------ #
    # Build variant — "local" | "cloud"
    # ------------------------------------------------------------------ #
    ENV: Literal["local", "cloud"] = "local"

    # ------------------------------------------------------------------ #
    # Application
    # ------------------------------------------------------------------ #
    APP_NAME: str = "ChitSmart API"
    APP_VERSION: str = "0.1.0"
    DEBUG: bool = False  # auto-flipped per-variant in validator below

    # ------------------------------------------------------------------ #
    # Database
    # ------------------------------------------------------------------ #
    DATABASE_URL: str = "postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev"
    DATABASE_POOL_SIZE: int = 10
    DATABASE_MAX_OVERFLOW: int = 20

    # ------------------------------------------------------------------ #
    # Security
    # ------------------------------------------------------------------ #
    SECRET_KEY: str = "CHANGE_ME_GENERATE_A_REAL_SECRET_KEY"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24  # 24 hours
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30
    ALGORITHM: str = "HS256"

    # ------------------------------------------------------------------ #
    # Firebase Admin SDK
    # ------------------------------------------------------------------ #
    FIREBASE_SERVICE_ACCOUNT_PATH: str = "firebase-service-account.json"
    # Cloud deployments: supply the raw JSON string instead of a file path.
    FIREBASE_SERVICE_ACCOUNT_JSON: Optional[str] = None

    # ------------------------------------------------------------------ #
    # SMS / OTP provider — "firebase" (default), "twilio", "msg91"
    # In DEBUG mode send_sms_otp returns before reaching the provider switch,
    # so this only affects the non-DEBUG warning path.
    # ------------------------------------------------------------------ #
    SMS_PROVIDER: str = "firebase"

    # ------------------------------------------------------------------ #
    # OpenAI
    # ------------------------------------------------------------------ #
    OPENAI_API_KEY: Optional[str] = None
    OPENAI_MODEL: str = "gpt-4o"
    OPENAI_EMBEDDING_MODEL: str = "text-embedding-3-small"

    # ------------------------------------------------------------------ #
    # Payment gateway — no third-party gateway integrated in this release.
    # PAYMENT_KEY_ID / PAYMENT_KEY_SECRET are reserved for a future integration.
    # ------------------------------------------------------------------ #
    PAYMENT_GATEWAY: str = "internal"
    PAYMENT_KEY_ID: Optional[str] = None
    PAYMENT_KEY_SECRET: Optional[str] = None

    # ------------------------------------------------------------------ #
    # CORS — comma-separated origins
    # ------------------------------------------------------------------ #
    ALLOWED_ORIGINS: str = "http://localhost:5173,http://localhost:3000"

    # ------------------------------------------------------------------ #
    # File storage
    # ------------------------------------------------------------------ #
    STORAGE_BACKEND: str = "local"  # local | s3
    STORAGE_LOCAL_PATH: str = "./uploads"
    AWS_BUCKET_NAME: Optional[str] = None
    AWS_REGION: Optional[str] = None
    AWS_ACCESS_KEY_ID: Optional[str] = None
    AWS_SECRET_ACCESS_KEY: Optional[str] = None

    # ------------------------------------------------------------------ #
    # Cloud-only: worker tuning
    # ------------------------------------------------------------------ #
    WEB_CONCURRENCY: int = 2

    # ------------------------------------------------------------------ #
    # pydantic-settings v2 config
    # ------------------------------------------------------------------ #
    model_config = SettingsConfigDict(
        env_file=[".env", ".env.local"],
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore",
    )

    # ------------------------------------------------------------------ #
    # Variant-aware defaults
    # ------------------------------------------------------------------ #
    @model_validator(mode="after")
    def _apply_variant_defaults(self) -> "Settings":
        """Auto-set DEBUG based on ENV when the caller hasn't set it explicitly."""
        env_debug = os.getenv("DEBUG")
        if env_debug is None:
            object.__setattr__(self, "DEBUG", self.ENV == "local")
        return self

    # ------------------------------------------------------------------ #
    # Derived helpers
    # ------------------------------------------------------------------ #
    @property
    def is_local(self) -> bool:
        return self.ENV == "local"

    @property
    def is_cloud(self) -> bool:
        return self.ENV == "cloud"

    @property
    def cors_origins(self) -> list[str]:
        return [o.strip() for o in self.ALLOWED_ORIGINS.split(",") if o.strip()]


settings = Settings()
