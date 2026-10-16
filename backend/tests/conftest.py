"""
Pytest configuration for ChitSmart backend test suite.

The TestClient from starlette wraps the FastAPI app synchronously so tests
run without a real database or event loop by default. The health endpoint and
app structure tests do not touch the database at all.
"""
import os

import pytest
from starlette.testclient import TestClient

# Provide the minimum required settings before the app module is imported.
# These override any .env file values during the test session.
os.environ.setdefault(
    "DATABASE_URL",
    "postgresql+asyncpg://test:test@localhost:5432/test_not_used",
)
os.environ.setdefault("SECRET_KEY", "test-secret-key-not-for-production")
os.environ.setdefault("DEBUG", "true")

from app.main import app  # noqa: E402 — import after env vars are set


@pytest.fixture(scope="session")
def test_client() -> TestClient:
    """
    Shared synchronous TestClient for the session.

    Health and other lightweight endpoints do not touch the database,
    so no database fixture is required for those tests.
    """
    with TestClient(app, raise_server_exceptions=True) as client:
        yield client
