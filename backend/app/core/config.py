"""
Application configuration — reads from environment variables.
Copy .env.example to .env and fill in values before running.
"""

from typing import Optional

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Application
    APP_NAME: str = "ChitSmart API"
    APP_VERSION: str = "0.1.0"
    DEBUG: bool = False

    # Database
    DATABASE_URL: str  # postgresql+asyncpg://user:pass@host/dbname
    DATABASE_POOL_SIZE: int = 10
    DATABASE_MAX_OVERFLOW: int = 20

    # Security
    SECRET_KEY: str  # generate with: openssl rand -hex 32
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24  # 24 hours
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30
    ALGORITHM: str = "HS256"

    # Firebase Admin SDK — path to the service account key JSON file.
    # Never commit the key file itself; backend/firebase-service-account.json is gitignored.
    FIREBASE_SERVICE_ACCOUNT_PATH: str = "firebase-service-account.json"

    # OpenAI
    OPENAI_API_KEY: Optional[str] = None
    OPENAI_MODEL: str = "gpt-4o"
    OPENAI_EMBEDDING_MODEL: str = "text-embedding-3-small"

    # Payment gateway
    PAYMENT_GATEWAY: str = "razorpay"  # razorpay | cashfree
    PAYMENT_KEY_ID: Optional[str] = None
    PAYMENT_KEY_SECRET: Optional[str] = None

    # CORS — comma-separated origins
    ALLOWED_ORIGINS: str = "http://localhost:5173,http://localhost:3000"

    # File storage
    STORAGE_BACKEND: str = "local"  # local | s3
    STORAGE_LOCAL_PATH: str = "./uploads"
    AWS_BUCKET_NAME: Optional[str] = None
    AWS_REGION: Optional[str] = None

    class Config:
        env_file = ".env"
        case_sensitive = True
        extra = "ignore"  # tolerate legacy/unknown keys in .env (e.g. SMS_*, OTP_*)


settings = Settings()
