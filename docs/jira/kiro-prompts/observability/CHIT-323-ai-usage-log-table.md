# Kiro Prompt — CHIT-323: AI Usage Log Table and Tracking

**Ticket:** CHIT-323  
**Epic:** CHIT-317 — Observability  
**Priority:** Medium  
**Skill:** Use the `database-migration` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-009  
**Prerequisites:** CHIT-318 (logging), CHIT-320 (metrics — `ai_tokens_used_total`, `ai_request_duration_seconds`)  
**Files:**
- `backend/migrations/versions/002_add_ai_usage_log.py` (new Alembic migration)
- `backend/app/models/ai_usage_log.py` (new model)
- `backend/app/services/ai_service.py` (instrument after every OpenAI call)
- `backend/requirements.txt` (no new deps needed)

---

## Objective

Create the `ai_usage_log` table and instrument the AI chat service to write a row for
every OpenAI API call. This powers the Admin → Reports → AI Usage report and enables
the operations team to monitor monthly AI spend.

## Repository inspection

Before writing any code:
1. Read `backend/app/models/` — understand the base model classes (`UUIDMixin`,
   `TimestampMixin`) and naming conventions.
2. Read `backend/migrations/versions/001_initial_schema.py` — understand how
   migrations are structured in this project.
3. Read `backend/app/services/ai_service.py` — find exactly where the OpenAI API call
   is made and where the response is received.
4. Read `backend/app/core/config.py` — find where OpenAI model names are configured.

## Model — `backend/app/models/ai_usage_log.py`

```python
from sqlalchemy import Column, String, Integer, Float, Boolean, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from app.models.base import Base, UUIDMixin, TimestampMixin

class AIUsageLog(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "ai_usage_log"

    # Who made the request
    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=True)
    admin_id = Column(UUID(as_uuid=True), ForeignKey("admin_users.id"), nullable=True)

    # OpenAI call details
    model = Column(String(64), nullable=False)          # e.g. "gpt-4o"
    prompt_tokens = Column(Integer, nullable=False)
    completion_tokens = Column(Integer, nullable=False)
    total_tokens = Column(Integer, nullable=False)
    cost_usd_approx = Column(Float, nullable=False)     # calculated client-side

    # Latency
    duration_ms = Column(Integer, nullable=False)        # first-token latency
    total_duration_ms = Column(Integer, nullable=False)  # full response

    # Tool use
    tool_called = Column(String(64), nullable=True)      # name of tool if called, else NULL
    finish_reason = Column(String(32), nullable=False)   # "stop", "tool_calls", "length"

    # Request context
    correlation_id = Column(String(64), nullable=True)
    language_detected = Column(String(16), nullable=True)
    message_length = Column(Integer, nullable=False)
```

## Alembic migration — `backend/migrations/versions/002_add_ai_usage_log.py`

Follow the pattern from `001_initial_schema.py`. Use `op.create_table` with all columns.
Add an index on `(member_id, created_at)` and `(created_at,)` for the reports query.

## OpenAI pricing constants — `backend/app/core/config.py`

```python
# Cost per 1K tokens (USD) — update when OpenAI pricing changes
OPENAI_COST_PER_1K_PROMPT_TOKENS: float = 0.005      # gpt-4o input
OPENAI_COST_PER_1K_COMPLETION_TOKENS: float = 0.015  # gpt-4o output
```

## Instrumentation — `backend/app/services/ai_service.py`

After receiving the OpenAI response, before returning to the caller:

```python
import time
import logging
from app.models.ai_usage_log import AIUsageLog
from app.core.config import settings
from app.core.metrics import ai_tokens_used_total, ai_request_duration_seconds

logger = logging.getLogger("app.services.ai")

# --- inside the AI chat function, after await openai_client.chat.completions.create() ---

usage = response.usage
prompt_tokens = usage.prompt_tokens
completion_tokens = usage.completion_tokens
total_tokens = usage.total_tokens
cost = (
    (prompt_tokens / 1000) * settings.OPENAI_COST_PER_1K_PROMPT_TOKENS
    + (completion_tokens / 1000) * settings.OPENAI_COST_PER_1K_COMPLETION_TOKENS
)

# Log to stdout
logger.info("ai_response_completed", extra={
    "event": "ai_response_completed",
    "member_id": str(member_id),
    "model": settings.OPENAI_MODEL,
    "prompt_tokens": prompt_tokens,
    "completion_tokens": completion_tokens,
    "total_tokens": total_tokens,
    "cost_usd_approx": round(cost, 6),
    "duration_ms": duration_ms,
    "finish_reason": response.choices[0].finish_reason,
    "correlation_id": correlation_id,
})

# Alert if cost exceeds threshold
if cost > 0.10:
    logger.error("ai_request_cost_exceeded", extra={
        "event": "ai_request_cost_exceeded",
        "cost_usd": round(cost, 6),
        "total_tokens": total_tokens,
        "member_id": str(member_id),
        "correlation_id": correlation_id,
    })

# Increment Prometheus counters
ai_tokens_used_total.labels(model=settings.OPENAI_MODEL, direction="prompt").inc(prompt_tokens)
ai_tokens_used_total.labels(model=settings.OPENAI_MODEL, direction="completion").inc(completion_tokens)
ai_request_duration_seconds.labels(model=settings.OPENAI_MODEL).observe(duration_ms / 1000)

# Write to DB (fire-and-forget — use asyncio.create_task or background task)
await session.execute(
    AIUsageLog.__table__.insert().values(
        member_id=member_id,
        model=settings.OPENAI_MODEL,
        prompt_tokens=prompt_tokens,
        completion_tokens=completion_tokens,
        total_tokens=total_tokens,
        cost_usd_approx=round(cost, 6),
        duration_ms=duration_ms,
        total_duration_ms=total_duration_ms,
        tool_called=tool_called,
        finish_reason=response.choices[0].finish_reason,
        correlation_id=correlation_id,
        language_detected=detected_language,
        message_length=len(message),
    )
)
```

## Acceptance criteria

- [ ] Alembic migration `002_add_ai_usage_log` applies cleanly: `alembic upgrade head`
- [ ] `ai_usage_log` table exists with all columns after migration
- [ ] Every AI chat request writes one row to `ai_usage_log`
- [ ] `cost_usd_approx` is calculated from `prompt_tokens` and `completion_tokens`
- [ ] A log entry at `ERROR` level is emitted when `cost_usd_approx > 0.10`
- [ ] `ai_tokens_used_total` counter increments after every AI call
- [ ] Migration rollback (`alembic downgrade -1`) drops the table cleanly
- [ ] Unit tests: cost calculation, DB row insert, cost threshold alert

## Definition of done

- `alembic upgrade head` and `alembic downgrade -1` both succeed
- `pytest tests/test_ai_usage_log.py -v` passes
- `ruff check` and `mypy` pass on new files
