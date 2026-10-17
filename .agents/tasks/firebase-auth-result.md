# Firebase Phone Auth Migration — Result Summary

**Commit:** `5d7ebe4`  
**Branch:** `Add-Compose-UI`  
**Status:** ✅ Complete

---

## Files Modified

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | Added `firebaseBom = "33.7.0"`, `googleServices = "4.4.2"`, `coroutinesPlayServices = "1.9.0"` versions; Firebase BOM, firebase-auth-ktx, coroutines-play-services library aliases; google-services plugin alias |
| `build.gradle.kts` (project root) | Added `alias(libs.plugins.google.services) apply false` |
| `app/build.gradle.kts` | Added `google-services` plugin; added Firebase BOM + `firebase-auth-ktx` + `kotlinx-coroutines-play-services` dependencies |
| `.gitignore` | Added `firebase-service-account.json` and `backend/firebase-service-account.json` |
| `app/.../data/api/AuthApiService.kt` | Removed `SendOtpRequest`, `SendOtpResponse`, `VerifyOtpRequest`, `sendOtp()`, `verifyOtp()`; added `VerifyFirebaseTokenRequest` and `verifyFirebaseToken()` Retrofit method |
| `app/.../data/repository/AuthRepository.kt` | Removed `sendOtp()`, `verifyOtp()`; added `verifyFirebaseToken(firebaseIdToken, mobileNumber)` |
| `app/.../ui/auth/LoginViewModel.kt` | Full rewrite: replaced `authRepository.sendOtp()` stub with `FirebaseAuth.verifyPhoneNumber()`; `LoginUiState` now has `OtpSent(verificationId)` and `AutoVerified`; `pendingCredential` and `resendToken` flows shared with `OtpViewModel` |
| `app/.../ui/auth/LoginScreen.kt` | Added `Activity` context; updated `sendOtp` call to `startFirebasePhoneAuth`; handles both `OtpSent` and `AutoVerified` states; removed duplicate `LoginUiState` definition; updated `onOtpSent` signature to carry `verificationId` |
| `app/.../ui/auth/OtpViewModel.kt` | Full rewrite: injects `FirebaseAuth`; `verifyOtp()` creates `PhoneAuthProvider.getCredential()` then calls `signInWithCredential()`; `verifyWithCredential()` for auto-verify path; `resendOtp()` signals `Idle` to trigger back-nav |
| `app/.../ui/auth/OTPScreen.kt` | Added `loginViewModel` parameter; `LaunchedEffect(pendingCredential)` auto-verifies on auto-retrieve path; resend now calls `onBack()` instead of `viewModel.resendOtp()`; trust row updated to "Secured by Firebase Phone Auth" |
| `app/.../ui/navigation/NavGraph.kt` | OTP route updated to `otp/{mobileNumber}/{verificationId}`; `AppRoutes.otp()` helper takes `verificationId`; `LoginScreen` call updated; `navArgument("verificationId")` added with `defaultValue = ""` |
| `app/.../di/AuthModule.kt` | Added `provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()` |
| `app/.../di/NetworkModule.kt` | Replaced `auth/send-otp`, `auth/verify-otp` in `unauthenticatedPaths` with `auth/verify-firebase-token` |
| `backend/requirements.txt` | Added `firebase-admin==6.6.0` |
| `backend/app/core/config.py` | Removed `OTP_EXPIRE_MINUTES`, `OTP_LENGTH`, `DEV_OTP_BYPASS`, `SMS_PROVIDER`, `SMS_API_KEY`, `SMS_API_SECRET`, `SMS_SENDER_ID`; added `FIREBASE_SERVICE_ACCOUNT_PATH`; added `extra = "ignore"` to tolerate legacy `.env` keys |
| `backend/app/api/v1/endpoints/auth.py` | Removed `send-otp`, `verify-otp` endpoints and `check_otp_rate_limit`; added `verify-firebase-token` endpoint that verifies Firebase ID token, finds/creates member, issues ChitSmart JWT |
| `backend/.env.example` | Removed SMS/OTP bypass keys; added `FIREBASE_SERVICE_ACCOUNT_PATH` |

---

## Compile Status

```
BUILD SUCCESSFUL in 3m 32s
17 actionable tasks: 2 executed, 15 up-to-date
```

Only pre-existing deprecation warnings (ArrowBack/ArrowForward icon naming). No errors.

## Backend Tests

`python -m pytest` could not run because `openai` is not installed in the system Python environment — this is a pre-existing environment issue unrelated to this migration. The `auth.py` module was verified structurally:

- `config.py` imports cleanly: `FIREBASE_PATH: firebase-service-account.json` ✅
- Auth router endpoints confirmed via grep: `/verify-firebase-token`, `/register`, `/refresh`, `/logout` ✅
- No `/send-otp` or `/verify-otp` in auth.py ✅

---

## Manual Steps Required Before the App Will Work

You must complete these steps in the Firebase Console and on the server before testing:

### 1. Firebase Console — Project Setup

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create a new project (or use an existing one)
3. Add an **Android app**:
   - Package name: `com.chitsmart`
   - Download `google-services.json` → place at **`app/google-services.json`**
   - This file is already gitignored
4. Enable **Phone Authentication**:
   - Firebase Console → Authentication → Sign-in method → Phone → Enable

### 2. Backend — Firebase Admin SDK Key

1. In Firebase Console → Project Settings → Service Accounts
2. Click **Generate new private key** → download the JSON file
3. Place the file at **`backend/firebase-service-account.json`**
4. This file is gitignored (both `firebase-service-account.json` and `backend/firebase-service-account.json` added to `.gitignore`)

### 3. Backend — Install New Dependency

```bash
cd backend
pip install -r requirements.txt
```

This installs `firebase-admin==6.6.0` along with other deps.

### 4. Backend — Update `.env` (if needed)

The old SMS/OTP keys in your `.env` are now ignored by the server (the `Settings` model has `extra = "ignore"`). You can remove them for cleanliness, but they won't cause errors if left. The only new key needed is:

```
FIREBASE_SERVICE_ACCOUNT_PATH=firebase-service-account.json
```

This is already the default, so it only needs to be in `.env` if you're placing the file at a non-default path.

### 5. Test Phone Numbers (Optional — for development)

In Firebase Console → Authentication → Sign-in method → Phone → **Phone numbers for testing**, you can add a test number + fixed OTP code to avoid real SMS costs during development. For example:

- Number: `+919876543210`  
- OTP: `123456`

These test numbers bypass SMS and work instantly.

---

## Architecture Notes

- `otp_sessions` table: **untouched** — schema and migrations intact, no new writes
- `firebase-service-account.json`: gitignored ✅
- `register`, `refresh`, `logout` endpoints: **unchanged** ✅
- No KYC, no Aadhaar/PAN fields added ✅
