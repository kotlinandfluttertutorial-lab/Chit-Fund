# Kiro Prompt — KAN-322: Sensitive Data Redaction Enforcement

**Ticket:** KAN-322  
**Epic:** KAN-317 — Observability  
**Priority:** Highest  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-004  
**Prerequisites:** KAN-318 (logging middleware — `safe_log_fields` utility)  
**Files:**
- `backend/app/core/logging.py` (extend redaction rules)
- `backend/tests/test_redaction.py` (new)

---

## Objective

Harden the `safe_log_fields` redaction utility from KAN-318 and add a comprehensive
unit test suite that proves sensitive values never reach log output. This ticket is a
security control — treat any test failure as a critical defect.

## Repository inspection

Before writing any code:
1. Read `backend/app/core/logging.py` — read the current `safe_log_fields` and
   `redact_mobile` implementations added in KAN-318.
2. Check if KAN-318 is already merged. If not, implement the redaction utility here
   as the primary ticket.
3. Read `backend/app/services/` — identify every place a logger is called to verify
   no raw sensitive values are passed as log `extra` fields.

## Redaction rules (complete set — REQ-OBS-004)

Extend `safe_log_fields` to enforce all of the following:

```python
# Keys that must always be redacted to "***REDACTED***"
ALWAYS_REDACT_KEYS = {
    "otp",
    "token",
    "access_token",
    "refresh_token",
    "account_number",
    "secret",
    "password",
    "api_key",
    "secret_key",
    "payment_key_secret",
    "hmac",
    "signature",
    "private_key",
    "pan",
    "aadhaar",
}

# Keys where the value is a mobile number — mask to last 4 digits
MOBILE_KEYS = {"mobile_number", "phone", "phone_number"}

# Keys where the value is a bank account number — mask to last 4 digits
ACCOUNT_KEYS = {"bank_account", "account_no", "account_number"}
```

Add a `redact_value` function for use when the key is not in the above sets but the
value pattern looks like a token (JWT starts with `eyJ`, long hex strings ≥ 40 chars):

```python
import re

_JWT_RE = re.compile(r"^eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+$")
_HEX_SECRET_RE = re.compile(r"^[0-9a-fA-F]{40,}$")
_MOBILE_RE = re.compile(r"^\+91[6-9]\d{9}$")
_ACCOUNT_RE = re.compile(r"^\d{9,18}$")   # typical Indian bank account length

def redact_value(value: str) -> str:
    """Redact a string value if it looks like a secret or PII."""
    if _JWT_RE.match(value):
        return "***JWT_REDACTED***"
    if _HEX_SECRET_RE.match(value):
        return "***SECRET_REDACTED***"
    return value

def redact_mobile(value: str) -> str:
    if _MOBILE_RE.match(value):
        return f"****{value[-4:]}"
    return value

def redact_account(value: str) -> str:
    if _ACCOUNT_RE.match(value):
        return f"****{value[-4:]}"
    return value
```

Updated `safe_log_fields`:

```python
def safe_log_fields(**kwargs) -> dict:
    result = {}
    for k, v in kwargs.items():
        str_v = str(v) if not isinstance(v, str) else v
        if k in ALWAYS_REDACT_KEYS:
            result[k] = "***REDACTED***"
        elif k in MOBILE_KEYS:
            result[k] = redact_mobile(str_v)
        elif k in ACCOUNT_KEYS:
            result[k] = redact_account(str_v)
        elif isinstance(v, str):
            result[k] = redact_value(str_v)
        else:
            result[k] = v
    return result
```

## Test suite — `backend/tests/test_redaction.py`

Write unit tests covering every rule:

```python
import pytest
from app.core.logging import safe_log_fields, redact_mobile, redact_account, redact_value

class TestRedactMobile:
    def test_valid_mobile_masked(self):
        assert redact_mobile("+919876543210") == "****3210"

    def test_non_mobile_unchanged(self):
        assert redact_mobile("not-a-mobile") == "not-a-mobile"

class TestRedactAccount:
    def test_account_number_masked(self):
        assert redact_account("123456789012") == "****9012"

    def test_short_number_unchanged(self):
        assert redact_account("12345") == "12345"

class TestRedactValue:
    def test_jwt_redacted(self):
        jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.abc123"
        assert redact_value(jwt) == "***JWT_REDACTED***"

    def test_hex_secret_redacted(self):
        assert redact_value("a" * 64) == "***SECRET_REDACTED***"

    def test_normal_string_unchanged(self):
        assert redact_value("hello") == "hello"

class TestSafeLogFields:
    def test_otp_redacted(self):
        result = safe_log_fields(event="otp_sent", otp="123456")
        assert result["otp"] == "***REDACTED***"

    def test_token_redacted(self):
        result = safe_log_fields(access_token="eyJhbGc.eyJzdWIi.sig")
        assert result["access_token"] == "***REDACTED***"

    def test_mobile_masked(self):
        result = safe_log_fields(mobile_number="+919876543210")
        assert result["mobile_number"] == "****3210"

    def test_account_number_masked(self):
        result = safe_log_fields(account_number="987654321012")
        assert result["account_number"] == "****1012"

    def test_safe_fields_pass_through(self):
        result = safe_log_fields(event="payment_created", amount=2500)
        assert result["event"] == "payment_created"
        assert result["amount"] == 2500

    def test_none_value_handled(self):
        result = safe_log_fields(user_id=None)
        assert result["user_id"] is None

    def test_aadhaar_redacted(self):
        result = safe_log_fields(aadhaar="123456789012")
        assert result["aadhaar"] == "***REDACTED***"

    def test_pan_redacted(self):
        result = safe_log_fields(pan="ABCDE1234F")
        assert result["pan"] == "***REDACTED***"
```

## Static analysis check (add to CI)

After all unit tests, add a grep-based check to the `check-before-push` scripts to
catch accidental raw sensitive values in log calls. Document the pattern in
`docs/deployment/check-before-push.md`.

## Acceptance criteria

- [ ] All 16 unit tests in `test_redaction.py` pass
- [ ] `safe_log_fields(otp="123456")["otp"] == "***REDACTED***"`
- [ ] `safe_log_fields(mobile_number="+919876543210")["mobile_number"] == "****3210"`
- [ ] `safe_log_fields(access_token="eyJ...")["access_token"] == "***REDACTED***"`
- [ ] `safe_log_fields(amount=2500)["amount"] == 2500` (non-sensitive passthrough)
- [ ] `safe_log_fields` handles `None` values without raising
- [ ] `redact_value` detects and redacts JWT-shaped strings and long hex strings
- [ ] `ruff check` and `mypy` pass on `backend/app/core/logging.py`

## Security note

A test failure in this file is a **critical security defect** — it means PII or secrets
could appear in production logs. Treat it with the same urgency as a data breach.

## Definition of done

- `pytest tests/test_redaction.py -v` — all tests pass
- No failures under `ruff check` or `mypy`
