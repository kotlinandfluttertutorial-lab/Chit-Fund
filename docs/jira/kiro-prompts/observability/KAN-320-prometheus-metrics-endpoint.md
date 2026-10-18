# Kiro Prompt — KAN-320: Prometheus Metrics Endpoint

**Ticket:** KAN-320  
**Epic:** KAN-317 — Observability  
**Priority:** High  
**Skill:** Use the `fastapi-endpoint` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-006  
**Prerequisites:** KAN-318 (logging middleware — correlation ID on `request.state`)  
**Files:**
- `backend/app/core/metrics.py` (new)
- `backend/app/api/v1/endpoints/health.py` (add `/metrics` route)
- `backend/app/core/middleware.py` (instrument HTTP metrics)
- `backend/requirements.txt` (add `prometheus-client`)

---

## Objective

Expose a Prometheus-compatible `GET /metrics` endpoint (internal only — not behind the
public API gateway). Instrument HTTP requests, key business events, and database pool
stats. All path labels must be normalised to prevent cardinality explosion.

## Repository inspection

Before writing any code:
1. Read `backend/requirements.txt` — check if `prometheus-client` is already present.
2. Read `backend/app/core/middleware.py` — this is where HTTP metrics instrumentation goes.
3. Read `backend/app/main.py` — understand how to mount the `/metrics` route without auth.
4. Read `backend/app/core/database.py` — find the SQLAlchemy engine to read pool stats.

## Implementation

### 1. Metric definitions — `backend/app/core/metrics.py`

```python
from prometheus_client import Counter, Histogram, Gauge, REGISTRY
from prometheus_client.openmetrics.exposition import CONTENT_TYPE_LATEST, generate_latest

# ── HTTP ──────────────────────────────────────────────────────────────────────
http_requests_total = Counter(
    "http_requests_total",
    "Total HTTP requests",
    ["method", "path", "status_code"],
)
http_request_duration_seconds = Histogram(
    "http_request_duration_seconds",
    "HTTP request duration",
    ["method", "path", "status_code"],
    buckets=[0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 2.0, 5.0],
)
http_requests_in_flight = Gauge(
    "http_requests_in_flight",
    "HTTP requests currently being processed",
)

# ── Business ──────────────────────────────────────────────────────────────────
otp_sent_total = Counter("otp_sent_total", "OTPs sent", ["provider", "success"])
otp_verified_total = Counter("otp_verified_total", "OTP verifications", ["success"])
payment_orders_created_total = Counter(
    "payment_orders_created_total", "Payment orders created", ["method"]
)
payment_webhooks_received_total = Counter(
    "payment_webhooks_received_total",
    "Payment webhooks received",
    ["event_type", "valid_signature"],
)
payment_status_transitions_total = Counter(
    "payment_status_transitions_total",
    "Payment status transitions",
    ["from_status", "to_status"],
)
ledger_entries_appended_total = Counter(
    "ledger_entries_appended_total", "Ledger entries appended", ["entry_type"]
)
ai_requests_total = Counter("ai_requests_total", "AI chat requests", ["language"])
ai_tokens_used_total = Counter(
    "ai_tokens_used_total", "AI tokens used", ["model", "direction"]
)
ai_tool_calls_total = Counter(
    "ai_tool_calls_total", "AI tool calls", ["tool_name", "success"]
)
ai_request_duration_seconds = Histogram(
    "ai_request_duration_seconds",
    "AI request duration (first token)",
    ["model"],
    buckets=[0.5, 1.0, 2.0, 3.0, 5.0, 10.0],
)
reconciliation_discrepancies_total = Counter(
    "reconciliation_discrepancies_total",
    "Payment reconciliation discrepancies found",
)

# ── Database pool ─────────────────────────────────────────────────────────────
db_pool_size = Gauge("db_pool_size", "SQLAlchemy pool size")
db_pool_checked_out = Gauge("db_pool_checked_out", "Active DB connections checked out")
db_pool_overflow = Gauge("db_pool_overflow", "DB connections in overflow")

# ── Path normalisation ────────────────────────────────────────────────────────
import re

_UUID_RE = re.compile(
    r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", re.IGNORECASE
)
_ID_SEGMENT_RE = re.compile(r"/\d+")

def normalise_path(path: str) -> str:
    """Replace UUIDs and numeric IDs with placeholders to bound label cardinality."""
    path = _UUID_RE.sub("{id}", path)
    path = _ID_SEGMENT_RE.sub("/{id}", path)
    return path
```

### 2. Instrument HTTP middleware — `backend/app/core/middleware.py`

Inside `RequestLoggingMiddleware.dispatch`, wrap the existing logic:

```python
from app.core.metrics import (
    http_requests_total, http_request_duration_seconds,
    http_requests_in_flight, normalise_path,
)

# Before call_next:
http_requests_in_flight.inc()
norm_path = normalise_path(request.url.path)

# After call_next:
http_requests_in_flight.dec()
labels = {"method": request.method, "path": norm_path,
          "status_code": str(response.status_code)}
http_requests_total.labels(**labels).inc()
http_request_duration_seconds.labels(**labels).observe(duration_ms / 1000)
```

### 3. DB pool refresh — `backend/app/core/metrics.py`

```python
def refresh_db_pool_metrics(engine) -> None:
    """Call this periodically or on each /metrics scrape."""
    pool = engine.pool
    db_pool_size.set(pool.size())
    db_pool_checked_out.set(pool.checkedout())
    db_pool_overflow.set(pool.overflow())
```

### 4. `/metrics` endpoint — `backend/app/api/v1/endpoints/health.py`

```python
from prometheus_client import generate_latest, CONTENT_TYPE_LATEST
from starlette.responses import Response
from app.core.metrics import refresh_db_pool_metrics
from app.core.database import engine

@router.get("/metrics", include_in_schema=False)
async def metrics():
    refresh_db_pool_metrics(engine)
    return Response(content=generate_latest(), media_type=CONTENT_TYPE_LATEST)
```

Mount this route **without** authentication middleware (same as `/health` and `/ready`).

Add `/metrics` to `_SKIP_LOGGING` in `middleware.py`.

### 5. `backend/requirements.txt`

```
prometheus-client==0.20.0
```

## How to use metrics in business logic

Import and increment counters at the service layer, not in endpoint handlers:

```python
# In auth_service.py
from app.core.metrics import otp_sent_total
otp_sent_total.labels(provider=settings.SMS_PROVIDER, success="true").inc()

# In payment_service.py
from app.core.metrics import payment_orders_created_total
payment_orders_created_total.labels(method=payment_method).inc()
```

## Acceptance criteria

- [ ] `GET /metrics` returns `200` with `Content-Type: text/plain; version=0.0.4`
- [ ] Response body contains `http_requests_total`, `http_request_duration_seconds`,
      `otp_sent_total`, `payment_orders_created_total`, `ai_requests_total`,
      `db_pool_size`
- [ ] After making 3 API requests, `http_requests_total` counter reflects them
- [ ] Path labels contain `{id}` instead of raw UUIDs or numeric IDs
- [ ] `GET /metrics` does NOT appear in the structured request log (excluded from
      middleware logging)
- [ ] `GET /metrics` does NOT require an `Authorization` header
- [ ] `db_pool_size` gauge is non-zero when the DB is connected
- [ ] Unit tests: `normalise_path` function, counter increment, histogram observe

## Security note

`GET /metrics` exposes internal application state. In production it MUST only be
reachable from the internal network (VPC / private subnet). Do NOT add this path to
the public-facing load balancer or API gateway. This is an infrastructure configuration
concern, not a code concern — document it in a comment in the route handler.

## Definition of done

- `curl http://localhost:8000/metrics` returns Prometheus text format
- All unit tests pass: `pytest tests/test_metrics.py -v`
- `ruff check` and `mypy` pass on `backend/app/core/metrics.py`
