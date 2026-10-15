# Kiro Prompt — KAN-259: Auth API — Verify OTP + Register + Refresh

**Ticket:** KAN-259  
**Epic:** KAN-251 — Backend — Core API  
**Priority:** Highest  
**Skill:** Use the `fastapi-endpoint` skill  
**File:** `backend/app/api/v1/endpoints/auth.py`

---

## Task

Implement `POST /api/v1/auth/verify-otp`, `POST /api/v1/auth/register`, `POST /api/v1/auth/refresh`, and `POST /api/v1/auth/logout`.

## Endpoint contracts

### POST `/api/v1/auth/verify-otp`
```
Request:  { "mobile_number": "+91XXXXXXXXXX", "otp": "123456" }
Response: {
  "is_new_user": true,
  "access_token": "<jwt>",
  "refresh_token": "<uuid>",
  "token_type": "bearer"
}
Errors: 401 (wrong OTP), 410 (expired OTP)
```
- Look up latest unused OTP session for the mobile number
- Compare SHA-256 hash of provided OTP
- Mark session `is_used = true` (one-time use)
- If member exists: return tokens with `is_new_user: false`
- If not: create member with `account_status=ACTIVE`, `kyc_status=NOT_REQUIRED`, return `is_new_user: true`

### POST `/api/v1/auth/register`
```
Request:  { "full_name": "...", "email": "...", "date_of_birth": "YYYY-MM-DD", "gender": "..." }
Auth:     Bearer token from verify-otp
Response: { "member_id": "...", "full_name": "...", "access_token": "...", "refresh_token": "..." }
```
- Update member record with the provided fields
- NO Aadhaar/PAN fields accepted — reject if provided
- Return fresh token pair

### POST `/api/v1/auth/refresh`
```
Request:  { "refresh_token": "<uuid>" }
Response: { "access_token": "<jwt>", "token_type": "bearer" }
Errors: 401 (invalid/expired refresh token)
```

### POST `/api/v1/auth/logout`
```
Auth:     Bearer token
Response: { "message": "Logged out" }
```
- Invalidate all refresh tokens for this member

## JWT implementation
- Use `python-jose[cryptography]`
- Access token: HS256, 24h expiry, payload `{"sub": member_id, "type": "access"}`
- Refresh token: UUID stored in `member_sessions` table with 30-day expiry

## Acceptance criteria
- [ ] Correct OTP → 200 with tokens and `is_new_user` flag
- [ ] Wrong OTP → 401
- [ ] Expired OTP (>10min) → 410
- [ ] Replay of already-used OTP → 401
- [ ] Register endpoint rejects any Aadhaar/PAN fields with 422
- [ ] Refresh returns new access token; invalid refresh token → 401
- [ ] Logout invalidates refresh tokens in DB
- [ ] All endpoints have unit tests with pytest-asyncio
