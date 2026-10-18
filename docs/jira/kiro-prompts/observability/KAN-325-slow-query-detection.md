# Kiro Prompt — KAN-325: Slow Query Detection

**Ticket:** KAN-325  
**Epic:** KAN-317 — Observability  
**Priority:** Medium  
**Skill:** Use the `fastapi-endpoint` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-008  
**Prerequisites:** KAN-318 (logging middleware)  
**Files:**
- `backend/app/core/database.py` (add SQLAlchemy event listener)

---

## Objective

Log a `WARNING` for any database query taking longer than 500 ms and an `ERROR` for
any query taking longer than 2,000 ms. Only the normalised query template must be
logged — never bound parameter values, which may contain PII.

## Repository inspection

Before writing any code:
1. Read `backend/app/core/database.py` — understand the async engine creation.
   Identify the SQLAlchemy engine instance variable.
2. Check if SQLAlchemy `event.listen` is already used anywhere.

## Implementation — `backend/app/core/database.py`

Use SQLAlchemy's `before_cursor_execute` and `after_cursor_execute` connection events
to measure query time:

```python
import time
import logging
from sqlalchemy import event
from sqlalchemy.engine import Engine

logger = logging.getLogger("app.db")

SLOW_QUERY_WARNING_MS = 500
SLOW_QUERY_ERROR_MS = 2_000


@event.listens_for(Engine, "before_cursor_execute")
def before_cursor_execute(conn, cursor, statement, parameters, context, executemany):
    conn.info.setdefault("query_start_time", []).append(time.monotonic())


@event.listens_for(Engine, "after_cursor_execute")
def after_cursor_execute(conn, cursor, statement, parameters, context, executemany):
    total_ms = (time.monotonic() - conn.info["query_start_time"].pop()) * 1000

    if total_ms >= SLOW_QUERY_ERROR_MS:
        logger.error(
            "slow_query",
            extra={
                "event": "slow_query",
                "duration_ms": round(total_ms, 2),
                "threshold_ms": SLOW_QUERY_ERROR_MS,
                # Normalise: remove numeric literals and string literals from query
                "query_template": _normalise_query(statement),
                # Never log `parameters` — may contain PII
            },
        )
    elif total_ms >= SLOW_QUERY_WARNING_MS:
        logger.warning(
            "slow_query",
            extra={
                "event": "slow_query",
                "duration_ms": round(total_ms, 2),
                "threshold_ms": SLOW_QUERY_WARNING_MS,
                "query_template": _normalise_query(statement),
            },
        )


def _normalise_query(statement: str) -> str:
    """
    Replace literal values with placeholders to prevent PII leakage.
    Strips string literals ('...'), numeric literals, and UUIDs.
    """
    import re
    # Remove string literals
    s = re.sub(r"'[^']*'", "'?'", statement)
    # Remove UUID literals
    s = re.sub(
        r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
        "?",
        s,
        flags=re.IGNORECASE,
    )
    # Remove numeric literals
    s = re.sub(r"\b\d+\b", "?", s)
    # Collapse whitespace
    return " ".join(s.split())
```

Register this listener in the engine creation block:

```python
# At the bottom of database.py, after engine = create_async_engine(...)
# The @event.listens_for decorator applies globally — no explicit registration needed.
# Confirm the import of `event` from `sqlalchemy` is present.
```

## Acceptance criteria

- [ ] A database query taking > 500 ms produces a `WARNING` log with `event: slow_query`
- [ ] A database query taking > 2,000 ms produces an `ERROR` log with `event: slow_query`
- [ ] `query_template` in the log contains `?` placeholders instead of literal values
- [ ] `parameters` is never present in the log output
- [ ] A query taking < 500 ms produces no slow-query log
- [ ] `_normalise_query("SELECT * FROM members WHERE id = '550e8400-e29b...'")` returns
      `"SELECT * FROM members WHERE id = '?'"`
- [ ] Unit tests: normalise_query function, threshold boundary (499ms/500ms/2001ms)

## Definition of done

- `pytest tests/test_slow_query_detection.py -v` passes
- `ruff check` and `mypy` pass on `backend/app/core/database.py`
