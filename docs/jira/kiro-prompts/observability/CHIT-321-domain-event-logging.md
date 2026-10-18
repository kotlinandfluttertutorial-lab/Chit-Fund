# Kiro Prompt — CHIT-321: Domain Event Logging

**Ticket:** CHIT-321  
**Epic:** CHIT-317 — Observability  
**Priority:** High  
**Skill:** Use the `fastapi-endpoint` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-003  
**Prerequisites:** CHIT-318 (logging middleware and `safe_log_fields`)  
**Files:**
- `backend/app/services/auth_service.py`
- `backend/app/services/payment_service.py`
- `backend/app/services/ai_service.py`
- `backend/app/services/admin_service.py` (or equivalent admin action handlers)
- `backend/app/core/logging.py` (extend `safe_log_fields` if needed)

---

## Objective

Add a structured `INFO` log entry for every significant domain event defined in
REQ-OBS-003. Each log line must use the `safe_log_fields` utility from CHIT-318 to
prevent PII leakage. Simultaneously increment the corresponding Prometheus counter
from CHIT-320.

## Repository inspection

Before writing any code:
1. Read each service file listed above — identify the exact function where each event
   occurs (e.g. `send_otp`, `verify_otp`, `create_payment`, `process_webhook`).
2. Read `backend/app/core/logging.py` — confirm `safe_log_fields` is available.
3. Read `backend/app/core/metrics.py` — confirm all counters from CHIT-320 are defined.
4. Do NOT add logging to endpoint handlers — it belongs in the service layer only.

## Events to instrument

### Auth service — `backend/app/services/auth_service.py`

```python
import logging
from app.core.logging import safe_log_fields
from app.core.metrics import otp_sent_total, otp_verified_total

logger = logging.getLogger("app.services.auth")

# After OTP send attempt:
logger.info("otp_sent", extra=safe_log_fields(
    event="otp_sent",
    mobile_number=mobile_number,   # auto-redacted to last-4 by safe_log_fields
    provider=settings.SMS_PROVIDER,
    success=str(success),
    correlation_id=correlation_id,
))
otp_sent_total.labels(provider=settings.SMS_PROVIDER, success=str(success).lower()).inc()

# After successful OTP verification:
logger.info("otp_verified", extra=safe_log_fields(
    event="otp_verified",
    member_id=str(member_id),
    attempt_number=attempt_number,
    success="true",
    correlation_id=correlation_id,
))
otp_verified_total.labels(success="true").inc()

# After failed OTP verification:
logger.warning("otp_verification_failed", extra=safe_log_fields(
    event="otp_verification_failed",
    mobile_number=mobile_number,
    attempt_number=attempt_number,
    remaining_attempts=remaining,
    correlation_id=correlation_id,
))
otp_verified_total.labels(success="false").inc()
```

### Payment service — `backend/app/services/payment_service.py`

```python
logger = logging.getLogger("app.services.payment")

# After payment order created:
logger.info("payment_order_created", extra=safe_log_fields(
    event="payment_order_created",
    payment_id=str(payment.id),
    member_id=str(payment.member_id),
    group_id=str(payment.chit_group_id),
    amount_paise=int(payment.total_amount * 100),
    method=payment.payment_method,
    correlation_id=correlation_id,
))
payment_orders_created_total.labels(method=payment.payment_method).inc()

# On webhook received — before signature check:
logger.info("webhook_received", extra=safe_log_fields(
    event="webhook_received",
    payment_id=payload.get("payment_id", "unknown"),
    event_type=event_type,
    correlation_id=correlation_id,
))
payment_webhooks_received_total.labels(
    event_type=event_type, valid_signature="pending"
).inc()

# On valid webhook signature:
logger.info("webhook_signature_valid", extra=safe_log_fields(
    event="webhook_signature_valid",
    payment_id=str(payment_id),
    success="true",
    correlation_id=correlation_id,
))

# On INVALID webhook signature:
logger.warning("webhook_signature_invalid", extra=safe_log_fields(
    event="webhook_signature_invalid",
    payment_id=payload.get("payment_id", "unknown"),
    success="false",
    correlation_id=correlation_id,
))
payment_webhooks_received_total.labels(
    event_type=event_type, valid_signature="false"
).inc()

# After payment status updated:
logger.info("payment_status_updated", extra=safe_log_fields(
    event="payment_status_updated",
    payment_id=str(payment.id),
    old_status=old_status,
    new_status=new_status,
    correlation_id=correlation_id,
))
payment_status_transitions_total.labels(
    from_status=old_status, to_status=new_status
).inc()

# After ledger entry appended:
logger.info("ledger_entry_appended", extra=safe_log_fields(
    event="ledger_entry_appended",
    entry_id=str(entry.id),
    payment_id=str(entry.payment_id),
    entry_type=entry.entry_type,
    correlation_id=correlation_id,
))
ledger_entries_appended_total.labels(entry_type=entry.entry_type).inc()
```

### AI service — `backend/app/services/ai_service.py`

```python
logger = logging.getLogger("app.services.ai")

# On AI chat request received:
logger.info("ai_request_started", extra=safe_log_fields(
    event="ai_request_started",
    member_id=str(member_id),
    message_length=len(message),
    language_detected=detected_language,
    correlation_id=correlation_id,
))
ai_requests_total.labels(language=detected_language).inc()

# After AI tool call — NEVER log the tool result value:
logger.info("ai_tool_called", extra=safe_log_fields(
    event="ai_tool_called",
    tool_name=tool_name,
    member_id=str(member_id),
    correlation_id=correlation_id,
))
ai_tool_calls_total.labels(tool_name=tool_name, success="true").inc()

# If tool call is denied (wrong member, no permission):
logger.warning("ai_tool_call_denied", extra=safe_log_fields(
    event="ai_tool_call_denied",
    tool_name=tool_name,
    reason=denial_reason,
    member_id=str(member_id),
    correlation_id=correlation_id,
))
ai_tool_calls_total.labels(tool_name=tool_name, success="false").inc()
```

### Admin service — every admin mutation

```python
logger = logging.getLogger("app.services.admin")

logger.info("admin_action", extra=safe_log_fields(
    event="admin_action",
    admin_id=str(admin_user.id),
    role=admin_user.role,
    action=action_name,           # e.g. "suspend_member", "approve_reconciliation"
    entity_type=entity_type,      # e.g. "member", "payment", "reconciliation"
    entity_id=str(entity_id),
    correlation_id=correlation_id,
))
```

## Obtaining `correlation_id` in services

Services are called from endpoint handlers. Pass `correlation_id` from
`request.state.correlation_id` (set by the logging middleware in CHIT-318) through to
the service call:

```python
# In endpoint handler:
async def create_payment(request: Request, body: PaymentRequest, ...):
    correlation_id = getattr(request.state, "correlation_id", None)
    result = await payment_service.create_payment(..., correlation_id=correlation_id)
```

## Acceptance criteria

- [ ] Every event listed in REQ-OBS-003 produces exactly one log line at the correct level
- [ ] All log lines produced by services are valid JSON (by virtue of using the shared logger)
- [ ] No OTP value, JWT token, bank account number, or full mobile number appears in any log line
- [ ] Prometheus counters are incremented on every event
- [ ] `mobile_number` values in log lines are masked to last 4 digits only
- [ ] AI tool result values (account balance, payment amounts) are NOT logged
- [ ] Unit tests cover: each event type, redaction of mobile_number, counter increments

## Definition of done

- `pytest tests/test_domain_event_logging.py -v` passes
- `ruff check` and `mypy` pass on all modified service files
- `grep -r "otp" backend/app --include="*.py" | grep "logger"` shows only masked output
