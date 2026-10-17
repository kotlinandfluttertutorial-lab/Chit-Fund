"""
HTTP request/response logger middleware for ChitSmart.

Logs every inbound request and its response, including:
  - HTTP method and URL path
  - Query string (if present)
  - Response status code
  - Wall-clock duration in milliseconds
  - Client IP address
  - Request-ID header (if present) for distributed tracing

Log level is INFO for 2xx/3xx, WARNING for 4xx, ERROR for 5xx.
"""

import logging
import time
import uuid

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import Response

logger = logging.getLogger("chitsmart.http")


class HTTPLoggerMiddleware(BaseHTTPMiddleware):
    """Middleware that emits a structured log line per request."""

    async def dispatch(self, request: Request, call_next) -> Response:
        request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())[:8]
        start = time.perf_counter()

        response = await call_next(request)

        duration_ms = (time.perf_counter() - start) * 1000
        status_code = response.status_code
        method = request.method
        path = request.url.path
        query = f"?{request.url.query}" if request.url.query else ""
        client_ip = request.client.host if request.client else "unknown"

        msg = f"[{request_id}] {method} {path}{query} → {status_code} ({duration_ms:.1f}ms) client={client_ip}"

        if status_code >= 500:
            logger.error(msg)
        elif status_code >= 400:
            logger.warning(msg)
        else:
            logger.info(msg)

        # Echo the request-id back so callers can correlate logs
        response.headers["X-Request-ID"] = request_id
        return response
