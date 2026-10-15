# Authentication & Registration — Requirements

**Epic:** CS-EP-01  
**Figma frames:** App-Auth-1.1 through App-Auth-1.5  
**Scope:** In scope — current release

---

## Functional requirements

### REQ-AUTH-001 — Splash Screen
The app MUST display a branded splash screen on cold start showing the ChitSmart logo, tagline, and a loading indicator before navigating to Onboarding or Login.

### REQ-AUTH-002 — Onboarding
First-time users MUST see a 3-slide onboarding carousel. Users who have previously completed onboarding MUST be taken directly to Login. The onboarding MUST NOT contain auction or bidding illustrations.

### REQ-AUTH-003 — Mobile Number Login
The system MUST accept a 10-digit Indian mobile number with +91 prefix. The system MUST validate the format before sending an OTP. The system MUST show a loading state while the OTP is being dispatched.

### REQ-AUTH-004 — OTP Verification
The system MUST send a 6-digit OTP to the provided mobile number via SMS. The OTP MUST expire after 10 minutes. The user MUST be allowed to resend the OTP after a 45-second countdown. The system MUST show an inline error for an incorrect OTP.

### REQ-AUTH-005 — New User Registration
When a mobile number is not registered, the system MUST prompt the user to register with: full name (required), email (optional), date of birth (optional), gender (optional). The system MUST NOT collect Aadhaar or PAN numbers in this release (KYC deferred).

### REQ-AUTH-006 — JWT Session
On successful OTP verification or registration, the system MUST return a JWT access token (24-hour expiry) and a refresh token (30-day expiry). The Android app MUST store tokens securely using EncryptedSharedPreferences.

### REQ-AUTH-007 — Token Refresh
The system MUST automatically refresh the access token using the refresh token when the access token expires, without requiring the user to log in again.

### REQ-AUTH-008 — Logout
The user MUST be able to log out from the Profile screen. On logout, all tokens MUST be revoked and cleared from device storage.

---

## Non-functional requirements

- OTP SMS delivery MUST succeed within 10 seconds under normal network conditions.
- Auth endpoints MUST respond within 500 ms (p95) under normal load.
- Tokens MUST be signed with HS256 and a secret key managed via environment variable.
- Mobile numbers MUST be stored in normalised E.164 format (+91XXXXXXXXXX).

---

## Out of scope (this release)

- Aadhaar/PAN collection — deferred with KYC (see docs/scope-decisions.md).
- Biometric login at registration — biometric is a Profile setting, not part of the auth flow.
- Social login (Google, Apple) — not required.
