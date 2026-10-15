# Kiro Prompt — KAN-258: Auth API — Send OTP

**Ticket:** KAN-258  
**Epic:** KAN-251 — Backend — Core API  
**Priority:** Highest  
**Skill:** Use the `fastapi-endpoint` skill  
**File:** `backend/app/api/v1/endpoints/auth.py`

---

## Task

Implement the `POST /api/v1/auth/send-otp` endpoint.

## Endpoint contract

```
POST /api/v1/auth/send-otp
No auth required

Request body:
{
  "mobile_number": "+91XXXXXXXXXX"   // E.164 format, mandatory
}

Response 200:
{
  "message": "OTP sent successfully",
  "expires_in_seconds": 600
}

Response 422: validation error (invalid mobile format)
Response 429: rate limit exceeded
```

## Implementation steps

### 1. Pydantic schema (`app/schemas/auth.py`)
```python
class SendOtpRequest(BaseModel):
    mobile_number: str

    @field_validator("mobile_number")
    def validate_mobile(cls, v: str) -> str:
        import re
        if not re.match(r"^\+91[6-9]\d{9}$", v):
            raise ValueError("Invalid Indian mobile number. Must be +91 followed by 10 digits starting 6-9.")
        return v

class SendOtpResponse(BaseModel):
    message: str
    expires_in_seconds: int
```

### 2. OTP session model (`app/models/otp_session.py`)
```python
class OTPSession(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "otp_sessions"
    mobile_number = Column(String(15), nullable=False, index=True)
    otp_hash = Column(String(64), nullable=False)        # SHA-256 hex
    expires_at = Column(DateTime(timezone=True), nullable=False)
    is_used = Column(Boolean, default=False, nullable=False)
    attempt_count = Column(Integer, default=0, nullable=False)
```

### 3. OTP service logic (`app/services/auth_service.py`)
- Generate 6-digit OTP: `secrets.randbelow(900000) + 100000`
- Hash: `hashlib.sha256(otp.encode()).hexdigest()`
- Store in `otp_sessions` with `expires_at = now + 10 minutes`
- Rate-limit: count OTP sessions for this mobile in the last hour; reject if ≥ 3
- Send SMS via provider configured in `settings.SMS_PROVIDER`
- For dev (`settings.DEBUG = True`): log OTP to console instead of sending SMS

### 4. SMS abstraction (`app/services/sms_service.py`)
```python
class SMSService:
    async def send_otp(self, mobile_number: str, otp: str) -> None:
        if settings.DEBUG:
            logger.info(f"[DEV] OTP for {mobile_number}: {otp}")
            return
        # Call Twilio / MSG91 / AWS SNS based on settings.SMS_PROVIDER
```

## Acceptance criteria
- [ ] Returns 200 with `expires_in_seconds: 600` on success
- [ ] Rejects mobile numbers not matching `+91[6-9]\d{9}` with 422
- [ ] Returns 429 after 3 OTP requests within 60 minutes for the same number
- [ ] OTP hash stored in DB, not the raw OTP
- [ ] In DEBUG mode, OTP is logged to console (no real SMS sent)
- [ ] Unit test: valid request, invalid format, rate limit
