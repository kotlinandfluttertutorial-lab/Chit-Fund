# Kiro Prompt — CHIT-318: Structured Logging Middleware

**Ticket:** CHIT-318  
**Epic:** CHIT-317 — Observability  
**Priority:** High  
**Skill:** Use the `fastapi-endpoint` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-001, REQ-OBS-002  
**Files:**
- `backend/app/core/logging.py` (new)
- `backend/app/core/middleware.py` (new or extend)
- `backend/app/main.py` (register middleware)

---

## Objective

Implement a FastAPI middleware that emits one structured JSON log line to stdout for
every HTTP request, plus a shared logging configuration that all backend modules use.
This is the foundation all other observability tickets depend on — implement it first.

## Repository inspection

Before writing any code:
1. Read `backend/app/main.py` — understand the current middleware stack and lifespan.
2. Read `backend/app/core/` — check for any existing logging or config module.
3. Read `backend/requirements.txt` — confirm `python-json-logger` or equivalent is
   listed; add it if absent.

## Implementation

### 1. Logging configuration — `backend/app/core/logging.py`

Configure Python's `logging` module to emit JSON to stdout using `python-json-logger`:

```python
import logging
import sys
from pythonjsonlogger import jsonlogger

def configure_logging(log_level: str = "INFO") -> None:
    handler = logging.StreamHandler(sys.stdout)
    formatter = jsonlogger.JsonFormatter(
        fmt="%(asctime)s %(levelname)s %(name)s %(message)s",
        datefmt="%Y-%m-%dT%H:%M:%S",
        rename_fields={"asctime": "timestamp", "levelname": "level", "name": "module"},
    )
    handler.setFormatter(formatter)
    root = logging.getLogger()
    root.handlers = [handler]
    root.setLevel(getattr(logging, log_level.upper(), logging.INFO))
```

Call `configure_logging(settings.LOG_LEVEL)` in the FastAPI lifespan startup hook.

Add `LOG_LEVEL` to `backend/app/core/config.py` (default `"INFO"`).

### 2. Redaction utility — `backend/app/core/logging.py`

```python
import re

_MOBILE_RE = re.compile(r"\+91\d{6}(\d{4})")

def redact_mobile(value: str) -> str:
    """Return '****<last4>' for any E.164 Indian mobile number."""
    return _MOBILE_RE.sub(r"****\1", value)

def safe_log_fields(**kwargs) -> dict:
    """
    Pass keyword args to log; automatically redacts known sensitive keys.
    Sensitive keys: mobile_number, otp, token, access_token, refresh_token,
    account_number, secret, password, api_key.
    """
    REDACTED = {"otp", "token", "access_token", "refresh_token",
                "account_number", "secret", "password", "api_key"}
    result = {}
    for k, v in kwargs.items():
        if k in REDACTED:
            result[k] = "***REDACTED***"
        elif k == "mobile_number" and isinstance(v, str):
            result[k] = redact_mobile(v)
        else:
            result[k] = v
    return result
```

### 3. Request logging middleware — `backend/app/core/middleware.py`

```python
import time
import uuid
import logging
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response

logger = logging.getLogger("app.middleware")

# Paths excluded from request logging (health probes)
_SKIP_LOGGING = {"/health", "/ready", "/metrics"}

class RequestLoggingMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next) -> Response:
        # Generate or inherit correlation ID
        correlation_id = request.headers.get("X-Correlation-ID") or str(uuid.uuid4())

        # Store on request state for use by endpoint handlers
        request.state.correlation_id = correlation_id

        if request.url.path in _SKIP_LOGGING:
            response = await call_next(request)
            response.headers["X-Correlation-ID"] = correlation_id
            return response

        start = time.monotonic()
        response = await call_next(request)
        duration_ms = round((time.monotonic() - start) * 1000, 2)

        logger.info(
            "request completed",
            extra={
                "correlation_id": correlation_id,
                "method": request.method,
                "path": request.url.path,
                "status_code": response.status_code,
                "duration_ms": duration_ms,
                "user_id": getattr(request.state, "user_id", None),
                "role": getattr(request.state, "role", None),
            },
        )

        response.headers["X-Correlation-ID"] = correlation_id
        return response
```

### 4. Register in `backend/app/main.py`

```python
from app.core.middleware import RequestLoggingMiddleware
from app.core.logging import configure_logging

@asynccontextmanager
async def lifespan(app: FastAPI):
    configure_logging(settings.LOG_LEVEL)
    yield

app.add_middleware(RequestLoggingMiddleware)
```

Middleware order matters — add `RequestLoggingMiddleware` **before** authentication
middleware so every request (including 401s) is logged.

### 5. Add `python-json-logger` to `backend/requirements.txt`

```
python-json-logger==2.0.7
```

## Acceptance criteria

- [ ] Every HTTP response includes the `X-Correlation-ID` header
- [ ] A JSON log line is emitted to stdout for every request not in `_SKIP_LOGGING`
- [ ] Log line contains: `timestamp`, `level`, `module`, `correlation_id`, `method`,
      `path`, `status_code`, `duration_ms`
- [ ] `GET /health` and `GET /ready` do NOT produce request log lines
- [ ] `python -c "import json; json.loads(line)"` succeeds on every emitted log line
- [ ] No mobile number, token, OTP, or account number appears in any log line
- [ ] `safe_log_fields` redacts all keys in the REDACTED set
- [ ] `redact_mobile("+919876543210")` returns `"****3210"`
- [ ] Unit tests: middleware log output, redaction utility, correlation ID generation
      and propagation

## Security checklist

- [ ] Request body is never read or logged by this middleware
- [ ] Response body is never read or logged by this middleware
- [ ] `_SKIP_LOGGING` excludes `/health`, `/ready`, `/metrics`

## Definition of done

- Middleware is registered and active in `main.py`
- All unit tests pass: `pytest tests/test_logging_middleware.py -v`
- `ruff check backend/app/core/logging.py backend/app/core/middleware.py` passes
- `mypy backend/app/core/logging.py backend/app/core/middleware.py --ignore-missing-imports` passes
- `docs/deployment/check-before-push.md` checklist passes
