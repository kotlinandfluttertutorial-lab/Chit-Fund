"""
Basic health-check smoke test — no database required.

Tests real behaviour of the /health endpoint exposed by app/main.py.
"""
from starlette.testclient import TestClient


def test_health_endpoint_returns_200(test_client: TestClient) -> None:
    """GET /health must return HTTP 200."""
    response = test_client.get("/health")
    assert response.status_code == 200


def test_health_endpoint_returns_json(test_client: TestClient) -> None:
    """GET /health body must contain {"status": "ok"}."""
    response = test_client.get("/health")
    data = response.json()
    assert "status" in data
    assert data["status"] == "ok"


def test_health_endpoint_includes_version(test_client: TestClient) -> None:
    """GET /health body must include a non-empty 'version' field."""
    response = test_client.get("/health")
    data = response.json()
    assert "version" in data
    assert isinstance(data["version"], str)
    assert len(data["version"]) > 0


def test_health_content_type_is_json(test_client: TestClient) -> None:
    """GET /health must respond with Content-Type: application/json."""
    response = test_client.get("/health")
    assert "application/json" in response.headers.get("content-type", "")


def test_app_is_fastapi_instance() -> None:
    """The app object must be a FastAPI instance (not a plain Starlette app)."""
    from fastapi import FastAPI

    from app.main import app

    assert isinstance(app, FastAPI)
