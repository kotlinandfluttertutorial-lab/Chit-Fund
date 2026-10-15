# Kiro Implementation Prompt — CS-EP-01: Authentication & Registration

**Epic:** CS-EP-01  
**Figma file:** https://www.figma.com/design/H3sDKKBcj9dveyJdgs7Jn5/Untitled  
**Figma frames:** App-Auth-1.3-MobileLogin-Default (1:2), App-Auth-1.4-OTPVerification-Default (1:115), App-Auth-1.5-Registration-Default (1:1570), App-Auth-1.1-Splash-Default (1:1415), App-Auth-1.2-Onboarding-Default (1:1468)  
**Spec:** .kiro/specs/auth/requirements.md  
**Stack:** Android (Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit) + FastAPI backend

---

## Task

Implement the complete authentication and registration flow for the ChitSmart Android app and the supporting FastAPI backend endpoints.

---

## Android — screens to implement

### 1. Splash Screen (`ui/splash/SplashScreen.kt`)
- Full-bleed `#073763` (PrimaryNavy) background
- Centred ChitSmart logo + wordmark in white
- Tagline "Your trusted chit fund partner" in Inter Light
- Teal (#14B8A6) linear progress bar at bottom
- After 2 seconds: navigate to Onboarding (first launch) or Login (returning user)
- First-launch detection via DataStore preference key `onboarding_completed`

### 2. Onboarding (`ui/onboarding/OnboardingScreen.kt`)
- `HorizontalPager` with 3 slides
- Slide 1: savings illustration, "Grow Together"
- Slide 2: payment/calendar illustration, "Pay Easily, Track Everything" — NO auction illustration
- Slide 3: AI assistant illustration, "AI That Understands You"
- Page dots indicator, Skip button (top-right), Next button, Get Started on slide 3
- On complete: set `onboarding_completed = true` in DataStore, navigate to Login

### 3. Login (`ui/auth/LoginScreen.kt`)
- Language toggle EN | हिंदी (top-right)
- "+91" prefix chip, 10-digit `BasicTextField` with `KeyboardType.Number`
- Validate: must be exactly 10 digits, starts with 6–9
- "Send OTP" `Button` (PrimaryNavy fill, white label, 8dp corners)
- "New to ChitSmart? Register" `TextButton`
- Loading state: replace button with `CircularProgressIndicator`
- On success: navigate to OTP screen passing the mobile number

### 4. OTP Verification (`ui/auth/OTPScreen.kt`)
- 6 individual `BasicTextField` boxes in a `Row`, auto-advance focus on digit entry
- Countdown timer `00:45` → counts down → "Resend OTP" `TextButton` enabled at 0
- "Verify" primary `Button`
- Error state: red border on all boxes, inline error text "Incorrect OTP. Please try again."
- On success: if new user → navigate to Registration; if existing user → navigate to Home

### 5. Registration (`ui/auth/RegistrationScreen.kt`)
- Mobile pre-filled (read-only, locked icon)
- Full name `OutlinedTextField` (required)
- Email `OutlinedTextField` (optional, `KeyboardType.Email`)
- Date of birth `DatePickerDialog` triggered by tap
- Gender `ExposedDropdownMenuBox`
- Consent checkbox + "I agree to the Terms and Conditions" `ClickableText`
- "Register" primary `Button`
- NO Aadhaar/PAN fields — KYC is deferred
- On success: navigate to Home

---

## Backend — endpoints to implement

File: `backend/app/api/v1/endpoints/auth.py`

### POST `/api/v1/auth/send-otp`
```
Request:  { "mobile_number": "+91XXXXXXXXXX" }
Response: { "message": "OTP sent", "expires_in_seconds": 600 }
```
- Validate E.164 format (+91 + 10 digits starting 6–9)
- Generate 6-digit OTP, store SHA-256 hash + expiry in `otp_sessions` table
- Send OTP via SMS (SMS provider from `settings.SMS_PROVIDER`)
- Rate-limit: max 3 OTP requests per mobile per hour (use Redis or DB counter)

### POST `/api/v1/auth/verify-otp`
```
Request:  { "mobile_number": "+91XXXXXXXXXX", "otp": "123456" }
Response: { 
  "is_new_user": true|false,
  "access_token": "...",
  "refresh_token": "...",
  "token_type": "bearer"
}
```
- Validate OTP hash match and expiry
- Mark OTP session as used (one-time use)
- If member exists → return tokens
- If new member → create member record with account_status=ACTIVE, kyc_status=NOT_REQUIRED → return tokens

### POST `/api/v1/auth/register`
```
Request:  { "full_name": "...", "email": "...", "date_of_birth": "YYYY-MM-DD", "gender": "..." }
Headers:  Authorization: Bearer <access_token_from_verify_otp>
Response: { "member_id": "...", "full_name": "...", "access_token": "...", "refresh_token": "..." }
```
- Called after OTP verify when is_new_user=true
- Update member record with provided details
- Return fresh tokens

### POST `/api/v1/auth/refresh`
```
Request:  { "refresh_token": "..." }
Response: { "access_token": "...", "token_type": "bearer" }
```

### POST `/api/v1/auth/logout`
```
Headers:  Authorization: Bearer <access_token>
Response: { "message": "Logged out" }
```
- Invalidate refresh token in DB

---

## Architecture notes

- Use Hilt for DI on Android. `AuthViewModel` → `AuthUseCase` → `AuthRepository` → `AuthApiService` (Retrofit)
- All API calls via coroutines + `StateFlow<UiState<T>>`
- UiState: `Loading | Success<T> | Error(message)`
- Tokens stored in `EncryptedSharedPreferences` via `SecurityKeyStore`
- FastAPI: use `python-jose` for JWT, `passlib` for hashing, `asyncpg` for DB

---

## Acceptance criteria

- [ ] Splash shows for ~2 seconds then navigates correctly
- [ ] Onboarding can be skipped; completed state persists across app restarts
- [ ] Login rejects invalid mobile formats with inline error before API call
- [ ] OTP auto-advances between boxes, resend countdown works correctly
- [ ] Wrong OTP shows red border error without navigating away
- [ ] New user reaches Registration; existing user goes directly to Home
- [ ] Registration succeeds without Aadhaar/PAN fields
- [ ] JWT tokens stored encrypted; not visible in plain storage inspection
- [ ] Logout clears tokens and returns to Login screen
- [ ] All screens match Figma frames — verify using the node IDs above with Figma MCP
