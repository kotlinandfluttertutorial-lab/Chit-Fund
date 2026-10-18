# Kiro Prompt — CHIT-319: Health and Readiness Endpoints

**Ticket:** CHIT-319  
**Epic:** CHIT-317 — Observability  
**Priority:** High  
**Skill:** Use the `fastapi-endpoint` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-005  
**Files:**
- `backend/app/api/v1/endpoints/health.py` (new)
- `backend/app/api/v1/router.py` (register routes)

---

## Objective

Implement `GET /health` (liveness) and `GET /ready` (readiness) endpoints. Both must
be excluded from JWT authentication middleware and from request logging middleware.
`/ready` must return `503` when the database is unreachable.

## Repository inspection

Before writing any code:
1. Read `backend/app/main.py` — understand how routes are registered and how
   authentication middleware is applied. Identify the mechanism to exclude paths.
2. Read `backend/app/api/v1/router.py` — understand the router structure.
3. Read `backend/app/core/middleware.py` — confirm `_SKIP_LOGGING` set exists
   (added in CHIT-318); if not yet merged, add `/health` and `/ready` to it here.
4. Read `backend/app/core/database.py` — understand the async session factory.

## Implementation

### `backend/app/api/v1/endpoints/health.py`

```python
import logging
from datetime import datetime, timezone

from fastapi import APIRouter, status
from fastapi.responses import JSONResponse
from sqlalchemy import text

from app.core.database import async_session_factory

router = APIRouter(tags=["Health"])
logger = logging.getLogger("app.health")


@router.get("/health", include_in_schema=False)
async def liveness():
    """Liveness probe — returns 200 if the process is running."""
    return {"status": "ok", "timestamp": datetime.now(timezone.utc).isoformat()}


@router.get("/ready", include_in_schema=False)
async def readiness():
    """Readiness probe — returns 503 if any dependency is unhealthy."""
    checks: dict[str, str] = {}

    # Database check: run SELECT 1 with a short timeout
    try:
        async with async_session_factory() as session:
            await session.execute(text("SELECT 1"))
        checks["database"] = "ok"
    except Exception as exc:
        logger.warning("Readiness check: database unreachable", extra={"error": str(exc)})
        checks["database"] = "degraded"

    # OpenAI check: env var presence only — no live API call
    from app.core.config import settings
    checks["openai"] = "ok" if settings.OPENAI_API_KEY else "not_configured"

    overall = "ready" if all(v == "ok" for v in checks.values()) else "degraded"
    http_status = status.HTTP_200_OK if overall == "ready" else status.HTTP_503_SERVICE_UNAVAILABLE

    return JSONResponse(
        status_code=http_status,
        content={
            "status": overall,
            "checks": checks,
            "timestamp": datetime.now(timezone.utc).isoformat(),
        },
    )
```

### Exclude from authentication

In `backend/app/main.py` (or wherever JWT authentication is applied as a dependency
or middleware), add `/health` and `/ready` to the list of unauthenticated paths.

If auth is applied via a dependency on the router, mount the health router **without**
that dependency:

```python
# In main.py — mount health routes at the root level, not under the authenticated v1 router
from app.api.v1.endpoints.health import router as health_router
app.include_router(health_router)  # No auth dependency
```

### Exclude from request logging

Confirm that `_SKIP_LOGGING` in `backend/app/core/middleware.py` contains:

```python
_SKIP_LOGGING = {"/health", "/ready", "/metrics"}
```

## Acceptance criteria

- [ ] `GET /health` returns `200` with `{"status": "ok", "timestamp": "..."}` — no
      `Authorization` header required
- [ ] `GET /ready` returns `200` with `{"status": "ready", "checks": {"database": "ok", "openai": "ok"}, ...}` when DB is reachable
- [ ] `GET /ready` returns `503` with `{"status": "degraded", "checks": {"database": "degraded"}, ...}` when DB is unreachable
- [ ] Neither endpoint produces a request log line (excluded from middleware logging)
- [ ] Neither endpoint requires a JWT token
- [ ] `GET /health` responds in under 50 ms
- [ ] `GET /ready` responds in under 200 ms
- [ ] Unit tests: liveness always 200, readiness 200 with live DB, readiness 503 when DB patched to raise

## Definition of done

- `curl http://localhost:8000/health` returns `200` with JSON body
- `curl http://localhost:8000/ready` returns `200` when `docker-compose up -d db` is running
- Tests pass: `pytest tests/test_health.py -v`
- `ruff check` and `mypy` pass on new file
