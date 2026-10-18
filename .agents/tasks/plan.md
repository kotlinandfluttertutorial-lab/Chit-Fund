# OTP Login End-to-End Wiring — Implementation Plan

**Generated:** Planning step  
**Scope:** Wire the existing backend auth endpoints to the existing Android UI. No new screens required.

---

## What already exists (do not recreate)

### Backend — fully implemented
- `POST /api/v1/auth/send-otp` — rate-limited, hashes OTP with SHA-256, stores in `otp_sessions`, calls `send_sms_otp()`
- `POST /api/v1/auth/verify-otp` — verifies hash, marks session used, creates/finds Member, issues JWT access + refresh tokens, returns `is_new_user`
- `POST /api/v1/auth/register` — updates member profile fields (name, email, DOB, gender); requires JWT from verify-otp
- `POST /api/v1/auth/refresh` — exchanges refresh token for a new access token
- `POST /api/v1/auth/logout` — revokes refresh token in `member_sessions`
- `app/models/otp_session.py` — `otp_sessions` table (id, mobile_number, otp_hash, expires_at, is_used, attempt_count, created_at)
- `app/models/member_session.py` — `member_sessions` table (id, member_id, refresh_token_hash, device_info, ip_address, expires_at, is_revoked)
- `app/models/member.py` — `members` table with `kyc_status=NOT_REQUIRED` default
- `app/services/sms_service.py` — thin wrapper: logs to console when `settings.DEBUG=true`, calls Twilio/MSG91 stubs in prod
- `app/core/security.py` — `create_access_token()`, `hash_refresh_token()`, `verify_token()`
- `app/core/deps.py` — `get_current_member()` JWT bearer validation
- `backend/alembic/versions/001_initial_schema.py` — all tables migrated
- `backend/.env.example` — OTP_EXPIRE_MINUTES, SMS_PROVIDER, SMS_API_KEY, SECRET_KEY documented

### Android — UI only (stubs, no backend calls)
- `LoginScreen.kt` — mobile number entry, calls `viewModel.sendOtp()`, handles `Loading`/`OtpSent`/`Error` states
- `OTPScreen.kt` — 6-digit input, countdown, resend button, error card; **verify button is a hardcoded stub** (`otp != "123456"`)
- `RegistrationScreen.kt` — collects full_name, email, DOB, gender; calls `onRegister(...)` lambda — **not wired to ViewModel**
- `NavGraph.kt` — routes LOGIN → OTP(mobile) → REGISTRATION(mobile)/HOME already defined

### Android — missing entirely
- No Retrofit service interface
- No Hilt `NetworkModule` or `AuthModule`
- No `AuthRepository`
- No `OtpViewModel`
- No `RegistrationViewModel`
- No token store (EncryptedSharedPreferences)

---

## Design decisions

**OTP storage strategy:** `otp_sessions` table already exists with all required columns. SHA-256 hash stored, plain OTP never persisted. Expiry enforced by `expires_at` column checked at verify time. Rate limit: 3 requests per mobile per 60 minutes (DB-based, no Redis). No new migration needed.

**SMS service:** `send_sms_otp()` already implements the dev-mode skip — when `settings.DEBUG=true` it logs `[DEV OTP] +91... → 123456` to the console. Twilio and MSG91 are stub implementations (commented-out code); to enable production sending, uncomment and add `SMS_API_KEY` / `SMS_API_SECRET` to `.env`. No plan item is needed for the backend SMS layer.

**JWT issuance point:** `verify_otp` issues both the access token and refresh token. `register` issues a fresh access token only (refresh token unchanged). `refresh` issues a new access token against the stored refresh hash. All handled by existing `create_access_token()` in `security.py`.

**Android token storage:** `androidx.security:security-crypto` is already in `build.gradle.kts`. Store `access_token` and `refresh_token` in `EncryptedSharedPreferences` under keys `"access_token"` and `"refresh_token"`. Wrap in a `TokenStore` singleton provided by Hilt (`@Singleton`). The `DataStore` dependency in the build file is for user preferences (onboarding flag, language) — keep that separate from tokens.

**Retrofit base URL:** already set in `build.gradle.kts` as `BuildConfig.API_BASE_URL = "http://127.0.0.1:8000/api/v1/"` for the emulator. OkHttp `AuthInterceptor` reads the access token from `TokenStore` and attaches it as `Authorization: Bearer <token>` on every request except auth endpoints.

**Error-state mapping:**

| Backend response | Android `LoginUiState` / `OtpUiState` |
|---|---|
| HTTP 200 send-otp | `OtpSent` |
| HTTP 422 (invalid phone) | `Error("Invalid mobile number")` |
| HTTP 429 (rate limit) | `Error("Too many OTP requests. Please wait.")` |
| Network timeout / no connectivity | `Error("No internet connection")` |
| HTTP 200 verify-otp | `Verified(isNewUser)` |
| HTTP 401 (wrong OTP) | `Error("Incorrect OTP. X attempts remaining.")` — bump local `attemptCount` |
| HTTP 410 (expired OTP) | `Error("OTP has expired. Please request a new one.")` |
| HTTP 200 register | navigate to HOME |
| HTTP 422 register | `Error("Please check your details.")` |
| Any unhandled 5xx | `Error("Something went wrong. Please try again.")` |

---

## Implementation Plan

- [ ] 1. Create `TokenStore` — EncryptedSharedPreferences wrapper for JWT tokens.
      Single responsibility: read and write access_token / refresh_token. Hilt `@Singleton`.
      Files to create:
      - `app/src/main/java/com/chitsmart/data/local/TokenStore.kt`
      Verify: project compiles (`./gradlew :app:compileDebugKotlin` exits 0).

- [ ] 2. Create the Retrofit `AuthApiService` interface.
      Define the four auth operations as suspend functions matching the backend contracts
      exactly: `sendOtp`, `verifyOtp`, `register`, `refreshToken`, `logout`.
      Request/response data classes go in the same file.
      Files to create:
      - `app/src/main/java/com/chitsmart/data/api/AuthApiService.kt`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 3. Create the Hilt `NetworkModule`.
      Provides `OkHttpClient` with `HttpLoggingInterceptor` (BODY in debug, NONE in release)
      and an `AuthInterceptor` that reads the stored access token and sets the Bearer header.
      Provides `Retrofit` singleton using `BuildConfig.API_BASE_URL` and Gson converter.
      Provides `AuthApiService` as a Retrofit-created implementation.
      Files to create:
      - `app/src/main/java/com/chitsmart/di/NetworkModule.kt`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 4. Create `AuthRepository`.
      Wraps `AuthApiService` calls in try/catch and maps results to a sealed `Result<T>` type.
      Methods: `sendOtp(mobile)`, `verifyOtp(mobile, otp)`, `register(fullName, email, dob, gender)`,
      `refreshToken()`, `logout()`. On `verifyOtp` success, saves both tokens to `TokenStore`.
      On `logout` success, clears tokens. On `register` success, saves new access token.
      Files to create:
      - `app/src/main/java/com/chitsmart/data/repository/AuthRepository.kt`
      - `app/src/main/java/com/chitsmart/data/repository/Result.kt`  ← sealed class `Success(data)` / `Error(message)`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 5. Create a Hilt `AuthModule` that provides `AuthRepository`.
      Binds `AuthRepository` as a singleton so all ViewModels receive the same instance.
      Files to create:
      - `app/src/main/java/com/chitsmart/di/AuthModule.kt`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 6. Replace the stub in `LoginViewModel` with a real `AuthRepository` call.
      Uncomment the `AuthRepository` constructor parameter (Hilt will inject it).
      Replace `delay(1500)` with `authRepository.sendOtp(mobileNumber)`.
      Map `Result.Success` → `LoginUiState.OtpSent`, `Result.Error` → `LoginUiState.Error(message)`.
      Files to modify:
      - `app/src/main/java/com/chitsmart/ui/auth/LoginViewModel.kt`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 7. Create `OtpViewModel` and wire it into `OTPScreen`.
      The ViewModel holds: `uiState: StateFlow<OtpUiState>`, `mobileNumber: String` (passed via
      SavedStateHandle from nav arg), `attemptCount: Int`.
      States: `Idle`, `Loading`, `Verified(isNewUser: Boolean)`, `Resending`, `Error(message)`.
      Methods: `verifyOtp(otp)` calls `authRepository.verifyOtp(mobile, otp)`;
      `resendOtp()` calls `authRepository.sendOtp(mobile)` and resets the countdown.
      In `OTPScreen.kt`:
      - Add `viewModel: OtpViewModel = hiltViewModel()` parameter.
      - Remove the hardcoded `showError = otp != "123456"` stub from the Verify button's `onClick`.
      - Call `viewModel.verifyOtp(otp)` instead.
      - Observe `uiState` to drive `isLoading`, `showError`, `errorAttemptsLeft`.
      - Add `LaunchedEffect` on `uiState is OtpUiState.Verified` → call `onVerified(isNewUser)`.
      - Wire "Resend SMS" click → `viewModel.resendOtp()`.
      Files to create:
      - `app/src/main/java/com/chitsmart/ui/auth/OtpViewModel.kt`
      Files to modify:
      - `app/src/main/java/com/chitsmart/ui/auth/OTPScreen.kt`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 8. Create `RegistrationViewModel` and wire it into `RegistrationScreen`.
      The ViewModel holds `uiState: StateFlow<RegistrationUiState>`.
      States: `Idle`, `Loading`, `Success`, `Error(message)`.
      Method: `register(fullName, email, dob, gender)` calls `authRepository.register(...)`.
      In `RegistrationScreen.kt`:
      - Add `viewModel: RegistrationViewModel = hiltViewModel()` parameter.
      - Replace the `isLoading = true; onRegister(...)` block in the CTA button with
        `viewModel.register(fullName, email.ifBlank{null}, dob.ifBlank{null}, gender.ifBlank{null})`.
      - Add `LaunchedEffect` on `uiState is RegistrationUiState.Success` → call `onRegister(...)`.
      - Show `uiState is RegistrationUiState.Error` as a snackbar or inline error message.
      Files to create:
      - `app/src/main/java/com/chitsmart/ui/auth/RegistrationViewModel.kt`
      Files to modify:
      - `app/src/main/java/com/chitsmart/ui/auth/RegistrationScreen.kt`
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 9. Pass `mobileNumber` into `OtpViewModel` via `SavedStateHandle` in the nav graph.
      In `NavGraph.kt`, the `OTP` composable already extracts `mobileNumber` from the back stack
      and passes it to `OTPScreen`. Update `OTPScreen` to forward it to `OtpViewModel`
      (the ViewModel reads it from `SavedStateHandle["mobileNumber"]`).
      Confirm `NavGraph.kt`'s registration route also passes `mobileNumber` to `RegistrationScreen`
      unchanged (it already does — no change needed there).
      Files to modify:
      - `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt` (only if the `OTPScreen`
        signature change requires it; otherwise no change needed)
      Verify: `./gradlew :app:compileDebugKotlin` exits 0.

- [ ] 10. Add `SMS_API_SECRET` key to `.env.example` for Twilio (the existing `sms_service.py`
       references it but `.env.example` only documents `SMS_API_KEY`).
       Also add `REFRESH_TOKEN_EXPIRE_DAYS` which is used in `config.py` but absent from `.env.example`.
       Files to modify:
       - `backend/.env.example`
       Verify: manual review — both keys appear in the file.

- [ ] 11. Confirm the backend test suite passes without modification.
       The backend endpoints are already implemented; no new server-side code is being added.
       Run the existing test suite to establish a baseline.
       Command: `cd j:\Android\AndroidStudioProjects\Kiro\Chit-Fund\backend && python -m pytest --tb=short -q`
       Expected: all existing tests pass (or skip if DB unavailable in CI).

- [ ] 12. Write unit tests for `AuthRepository` on Android.
       Use JUnit 4 + `kotlinx-coroutines-test` (both already in `build.gradle.kts`).
       Mock `AuthApiService` with a fake or Mockito stub.
       Cover: sendOtp success, sendOtp 429, verifyOtp success saves tokens, verifyOtp 401,
       verifyOtp 410, register success updates access token, logout clears tokens.
       Files to create:
       - `app/src/test/java/com/chitsmart/data/repository/AuthRepositoryTest.kt`
       Verify: `./gradlew :app:testDebugUnitTest` exits 0 with all new tests passing.

---

## File manifest

### New Android files (create)
| Path | Purpose |
|---|---|
| `app/src/main/java/com/chitsmart/data/local/TokenStore.kt` | EncryptedSharedPreferences wrapper for JWT tokens |
| `app/src/main/java/com/chitsmart/data/api/AuthApiService.kt` | Retrofit interface + request/response DTOs |
| `app/src/main/java/com/chitsmart/data/repository/Result.kt` | Sealed `Result<T>` for repository return types |
| `app/src/main/java/com/chitsmart/data/repository/AuthRepository.kt` | Repository bridging API + TokenStore |
| `app/src/main/java/com/chitsmart/di/NetworkModule.kt` | Hilt module: OkHttpClient, Retrofit, AuthApiService |
| `app/src/main/java/com/chitsmart/di/AuthModule.kt` | Hilt module: AuthRepository binding |
| `app/src/main/java/com/chitsmart/ui/auth/OtpViewModel.kt` | ViewModel for OTPScreen |
| `app/src/main/java/com/chitsmart/ui/auth/RegistrationViewModel.kt` | ViewModel for RegistrationScreen |
| `app/src/test/java/com/chitsmart/data/repository/AuthRepositoryTest.kt` | Unit tests for AuthRepository |

### Modified Android files
| Path | Change |
|---|---|
| `app/src/main/java/com/chitsmart/ui/auth/LoginViewModel.kt` | Inject `AuthRepository`; replace stub delay with real API call |
| `app/src/main/java/com/chitsmart/ui/auth/OTPScreen.kt` | Accept `OtpViewModel`; replace hardcoded stub with ViewModel calls |
| `app/src/main/java/com/chitsmart/ui/auth/RegistrationScreen.kt` | Accept `RegistrationViewModel`; wire CTA and state observation |
| `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt` | No change expected unless OTPScreen signature forces it |

### Modified backend files
| Path | Change |
|---|---|
| `backend/.env.example` | Add `SMS_API_SECRET` and `REFRESH_TOKEN_EXPIRE_DAYS` keys |

### Backend files confirmed correct — no changes needed
- `backend/app/api/v1/endpoints/auth.py` — all 5 endpoints fully implemented
- `backend/app/services/sms_service.py` — dev-mode skip + provider stubs in place
- `backend/app/core/security.py` — JWT helpers complete
- `backend/app/core/config.py` — OTP_EXPIRE_MINUTES, SMS settings present
- `backend/app/models/otp_session.py` — schema complete
- `backend/app/models/member_session.py` — schema complete
- `backend/alembic/versions/001_initial_schema.py` — all tables migrated

---

## Dependency order

Items 1–5 are pure infrastructure (no UI touch) and must complete first, in order, because each item's output is an input to the next:
1 → TokenStore (used by AuthRepository)
2 → AuthApiService (used by AuthRepository)
3 → NetworkModule (provides AuthApiService to Hilt graph)
4 → AuthRepository (depends on 1 + 2)
5 → AuthModule (registers 4 in Hilt)

Items 6–9 are ViewModel + screen wiring and can proceed in any order after item 5:
6 (LoginViewModel), 7 (OtpViewModel + OTPScreen), 8 (RegistrationViewModel + RegistrationScreen), 9 (SavedStateHandle nav arg) are independent of each other.

Items 10 and 11 are documentation/verification and are independent of all other items.

Item 12 (tests) depends on item 4.

---

## Notes and assumptions

- **No backend changes required.** All five auth endpoints are fully implemented and registered in `router.py`. The `otp_sessions`, `members`, and `member_sessions` tables are all migrated by `001_initial_schema.py`.
- **SMS sending in dev:** set `DEBUG=true` in `backend/.env`. The OTP will appear in the FastAPI log output (`[DEV OTP] +91XXXXXXXXXX → 123456`). No Twilio credentials needed for local development.
- **Production SMS:** uncomment the Twilio client code in `sms_service.py` and set `SMS_API_KEY` + `SMS_API_SECRET` in `.env`. MSG91 (preferred in India) requires the same pattern — implement `_send_msg91()` body when ready.
- **attempt_count logic:** `OtpSession.attempt_count` is incremented on each wrong OTP in the backend. The Android error card already shows `errorAttemptsLeft`; after wiring, this should be derived from the backend's attempt_count or computed locally as `3 - attemptCount`.
- **Token refresh:** the `AuthInterceptor` in `NetworkModule` should detect 401 responses from non-auth endpoints, call `authRepository.refreshToken()`, update the stored access token, and retry the original request once. This is the standard OkHttp Authenticator pattern and should be implemented inside `NetworkModule` as an `okhttp3.Authenticator`, not in the repository.
- **Logout wiring:** `ProfileScreen.kt` already calls `onLogout` which navigates to LOGIN. Before navigating, it should call `authRepository.logout()` to revoke the server-side refresh token. This requires a `ProfileViewModel` — that is out of scope for this plan but is noted as a follow-on.
