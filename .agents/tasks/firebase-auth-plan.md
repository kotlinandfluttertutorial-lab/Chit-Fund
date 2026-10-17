# Firebase Phone Auth Migration Plan

**Goal:** Replace the custom OTP flow (backend generates OTP → SMS → backend verifies hash) with Firebase Phone Auth.  
**New flow:** Android SDK → Firebase sends SMS → app gets Firebase credential → app sends Firebase ID token to backend → backend verifies with Firebase Admin SDK → backend issues its own JWT.

---

## Constraints observed from reading the code

- `otp_sessions` table: **do not drop** — leave migration and model intact.
- `register`, `refresh`, `logout` endpoints: **unchanged**.
- No Aadhaar/PAN fields, no KYC work.
- `firebase-service-account.json` must never be committed; `.gitignore` already excludes `google-services.json` but needs a second rule for the service account JSON.
- Firebase BOM: `33.7.0`, `firebase-admin` Python: `6.6.0`.
- `minSdk = 26`, `compileSdk = 35`, `kotlin = "2.0.21"`, `hilt = "2.52"` — no conflicts with Firebase Auth SDK.
- `google-services` Gradle plugin is required but **not yet present** in `build.gradle.kts` or `libs.versions.toml`.
- `google-services.json` is already gitignored.
- `NetworkModule` has a hard-coded exclusion list (`unauthenticatedPaths`) that references `auth/send-otp` and `auth/verify-otp`; these must be updated to `auth/verify-firebase-token` once the old endpoints are removed.
- `LoginScreen.kt` calls `viewModel.sendOtp("+91$mobile")` and navigates on `LoginUiState.OtpSent`; with Firebase Auth the OTP trigger moves into the ViewModel with an `Activity` reference — this requires threading the `Activity` down from `LoginScreen` (which has `LocalContext` available) to the ViewModel via a trigger method rather than storing it in the ViewModel.
- `OTPScreen.kt` currently calls `viewModel.verifyOtp(otp)` and `viewModel.resendOtp()` — both of these need to be rewired but the UI itself (6-digit boxes, error card, countdown, trust row) stays **unchanged**.
- `AuthModule.kt` provides `AuthRepository` manually with `api` and `tokenStorage`; after the migration it will also need `FirebaseAuth` — simpler to keep the manual `@Provides` and add the parameter.
- `AuthApiService`: `sendOtp` and `verifyOtp` methods are removed; `verifyFirebaseToken` is added.
- `AuthRepository`: `sendOtp` and `verifyOtp` methods are removed; `verifyFirebaseToken` is added.

---

## Implementation Plan

- [ ] 1. Register the app in Firebase Console and place `google-services.json`
      This is a one-time manual step that must be done before any build can compile with Firebase.
      - In Firebase Console → Project Settings → Add Android app → package name `com.chitsmart`.
      - Download `google-services.json` and place it at `app/google-services.json`.
      - `google-services.json` is already in `.gitignore` — nothing extra needed.
      - Also enable **Phone Authentication** in Firebase Console → Authentication → Sign-in method.
      Files: `app/google-services.json` (placed manually, never committed)
      Verify: file exists at `app/google-services.json` before attempting any Gradle build.

- [ ] 2. Add `google-services` plugin and Firebase BOM to version catalog and Gradle files
      The project has no Firebase dependency at all today. Add the plugin declaration to the version catalog and both Gradle files, then add the Firebase Auth dependency to `app/build.gradle.kts`.

      **`gradle/libs.versions.toml`** — add to `[versions]`:
      ```
      firebaseBom = "33.7.0"
      googleServices = "4.4.2"
      ```
      Add to `[libraries]`:
      ```
      firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
      firebase-auth-ktx = { group = "com.google.firebase", name = "firebase-auth-ktx" }
      ```
      Add to `[plugins]`:
      ```
      google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
      ```

      **`build.gradle.kts` (project root)** — add to the `plugins {}` block (apply false):
      ```kotlin
      alias(libs.plugins.google.services) apply false
      ```

      **`app/build.gradle.kts`** — add to the `plugins {}` block:
      ```kotlin
      alias(libs.plugins.google.services)
      ```
      Add to `dependencies {}` after the Compose BOM block:
      ```kotlin
      // Firebase BOM + Phone Auth
      val firebaseBom = platform(libs.firebase.bom)
      implementation(firebaseBom)
      implementation(libs.firebase.auth.ktx)
      ```

      Files:
      - `gradle/libs.versions.toml`
      - `build.gradle.kts` (project root)
      - `app/build.gradle.kts`

      Verify: `./gradlew assembleDebug` — project compiles without Firebase-related errors. (Requires `google-services.json` from step 1.)

- [ ] 3. Update `.gitignore` to exclude `firebase-service-account.json`
      The backend will need a service account JSON file. It must never be committed.
      Add two lines to the repo root `.gitignore` directly after the existing `google-services.json` entry:

      ```
      # Firebase service account key (backend) — never commit
      firebase-service-account.json
      backend/firebase-service-account.json
      ```

      Files: `.gitignore`
      Verify: `git check-ignore -v backend/firebase-service-account.json` returns a match after staging the file change.

- [ ] 4. Rewrite `AuthApiService.kt` — remove old OTP calls, add `verifyFirebaseToken`
      Remove: `SendOtpRequest`, `SendOtpResponse`, `VerifyOtpRequest`, `sendOtp()`, `verifyOtp()` (the Retrofit interface methods).
      Add:
      ```kotlin
      data class VerifyFirebaseTokenRequest(
          @SerializedName("firebase_id_token") val firebaseIdToken: String,
          @SerializedName("mobile_number") val mobileNumber: String,
      )
      ```
      Add to `AuthApiService` interface:
      ```kotlin
      @POST("auth/verify-firebase-token")
      suspend fun verifyFirebaseToken(
          @Body request: VerifyFirebaseTokenRequest,
      ): Response<AuthTokenResponse>
      ```
      Keep: `RegisterRequest`, `RefreshRequest`, `LogoutRequest`, `AuthTokenResponse`, `TokenResponse`, `register()`, `refreshToken()`, `logout()`.

      Files: `app/src/main/java/com/chitsmart/data/api/AuthApiService.kt`
      Verify: `./gradlew :app:compileDebugKotlin` — no unresolved reference errors on this file.

- [ ] 5. Rewrite `AuthRepository.kt` — remove `sendOtp`/`verifyOtp`, add `verifyFirebaseToken`
      Remove: the `sendOtp()` and `verifyOtp()` methods.
      Add:
      ```kotlin
      suspend fun verifyFirebaseToken(
          firebaseIdToken: String,
          mobileNumber: String,
      ): Result<VerifyResult> = safeCall {
          val response = api.verifyFirebaseToken(
              VerifyFirebaseTokenRequest(firebaseIdToken, mobileNumber)
          )
          when {
              response.isSuccessful -> {
                  val body = response.body()!!
                  tokenStore.saveAccessToken(body.accessToken)
                  if (body.refreshToken.isNotBlank()) {
                      tokenStore.saveRefreshToken(body.refreshToken)
                  }
                  Result.Success(VerifyResult(isNewUser = body.isNewUser))
              }
              response.code() == 401 -> Result.Error("Firebase token verification failed.")
              response.code() == 422 -> Result.Error("Invalid request. Please try again.")
              else -> Result.Error("Something went wrong. Please try again.")
          }
      }
      ```
      The `register()`, `refreshToken()`, `logout()`, and `safeCall()` methods remain unchanged.

      Files: `app/src/main/java/com/chitsmart/data/repository/AuthRepository.kt`
      Verify: `./gradlew :app:compileDebugKotlin` — no compilation errors.

- [ ] 6. Rewrite `LoginViewModel.kt` — replace `sendOtp` stub with Firebase Phone Auth trigger
      Firebase Phone Auth requires an `Activity` to show the invisible reCAPTCHA and handle SMS auto-retrieval. The ViewModel must **not** hold a reference to an `Activity`; instead, the composable passes the Activity each time it calls `startFirebasePhoneAuth()`.

      New state sealed class (replace `LoginUiState.OtpSent` with `OtpSent` carrying the `verificationId`):
      ```kotlin
      sealed class LoginUiState {
          object Idle : LoginUiState()
          object Loading : LoginUiState()
          data class OtpSent(val verificationId: String) : LoginUiState()
          object AutoVerified : LoginUiState()   // SMS auto-retrieved on same device
          data class Error(val message: String) : LoginUiState()
      }
      ```

      New ViewModel body — inject `FirebaseAuth` via Hilt:
      ```kotlin
      @HiltViewModel
      class LoginViewModel @Inject constructor(
          private val firebaseAuth: FirebaseAuth,
      ) : ViewModel() {

          private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
          val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

          /** mobileNumber must include country code, e.g. "+919876543210" */
          fun startFirebasePhoneAuth(mobileNumber: String, activity: Activity) {
              _uiState.value = LoginUiState.Loading
              val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                  .setPhoneNumber(mobileNumber)
                  .setTimeout(60L, TimeUnit.SECONDS)
                  .setActivity(activity)
                  .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                      override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                          // SMS auto-retrieved — signal the OTP screen can skip manual entry
                          _uiState.value = LoginUiState.AutoVerified
                          // Store the credential so OtpViewModel can use it
                          _pendingCredential.value = credential
                      }
                      override fun onVerificationFailed(e: FirebaseException) {
                          _uiState.value = LoginUiState.Error(
                              e.message ?: "Phone verification failed"
                          )
                      }
                      override fun onCodeSent(
                          verificationId: String,
                          token: PhoneAuthProvider.ForceResendingToken,
                      ) {
                          _resendToken.value = token
                          _uiState.value = LoginUiState.OtpSent(verificationId)
                      }
                  })
                  .build()
              PhoneAuthProvider.verifyPhoneNumber(options)
          }

          // Shared state consumed by OtpViewModel
          val _pendingCredential = MutableStateFlow<PhoneAuthCredential?>(null)
          val _resendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)

          fun resetState() { _uiState.value = LoginUiState.Idle }
      }
      ```

      **Important notes on `_pendingCredential` and `_resendToken`:**
      Both are `internal`-scoped mutable state that `OtpViewModel` reads. They are left on `LoginViewModel` because both ViewModels share the same `HiltViewModelFactory` scope within the auth back stack. In `OTPScreen`, `OtpViewModel` receives `LoginViewModel` as a constructor arg via Hilt-assisted injection — see step 8 for the `OtpViewModel` rewrite.

      Imports needed: `android.app.Activity`, `com.google.firebase.auth.*`, `java.util.concurrent.TimeUnit`.

      Files: `app/src/main/java/com/chitsmart/ui/auth/LoginViewModel.kt`
      Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 7. Update `LoginScreen.kt` — pass Activity context to ViewModel, handle `AutoVerified` state
      `LoginScreen` is a `@Composable`. Firebase Phone Auth needs a real `Activity`, available via `LocalContext.current as Activity`.

      Changes:
      1. Add `import android.app.Activity` and `import androidx.compose.ui.platform.LocalContext`.
      2. Inside the composable body, add:
         ```kotlin
         val activity = LocalContext.current as Activity
         ```
      3. Replace the button's `onClick`:
         ```kotlin
         onClick = { viewModel.startFirebasePhoneAuth("+91$mobile", activity) }
         ```
      4. Update the `LaunchedEffect(uiState)` to handle both `OtpSent` (navigate) and `AutoVerified` (navigate with an empty verificationId — `OtpViewModel` will detect the pending credential):
         ```kotlin
         LaunchedEffect(uiState) {
             when (val s = uiState) {
                 is LoginUiState.OtpSent -> onOtpSent("+91$mobile", s.verificationId)
                 is LoginUiState.AutoVerified -> onOtpSent("+91$mobile", "")
                 else -> Unit
             }
         }
         ```
      5. Update the composable signature:
         ```kotlin
         fun LoginScreen(
             viewModel: LoginViewModel = hiltViewModel(),
             onOtpSent: (mobileNumber: String, verificationId: String) -> Unit,
             onRegister: () -> Unit,
         )
         ```

      Files: `app/src/main/java/com/chitsmart/ui/auth/LoginScreen.kt`
      Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 8. Rewrite `OtpViewModel.kt` — replace custom OTP verify/resend with Firebase credential verification
      `OtpViewModel` now holds the `verificationId` (passed from nav args) and calls `FirebaseAuth.signInWithCredential()` then passes the Firebase ID token to `AuthRepository.verifyFirebaseToken()`.

      New ViewModel:
      ```kotlin
      @HiltViewModel
      class OtpViewModel @Inject constructor(
          private val authRepository: AuthRepository,
          private val firebaseAuth: FirebaseAuth,
          savedStateHandle: SavedStateHandle,
      ) : ViewModel() {

          val mobileNumber: String = savedStateHandle["mobileNumber"] ?: ""
          private val verificationId: String = savedStateHandle["verificationId"] ?: ""

          private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
          val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

          /** Called when user taps "Verify & Continue" with the 6 digits they typed. */
          fun verifyOtp(otp: String) {
              viewModelScope.launch {
                  _uiState.value = OtpUiState.Loading
                  try {
                      val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                      signInWithCredential(credential)
                  } catch (e: Exception) {
                      _uiState.value = OtpUiState.Error("Invalid OTP format")
                  }
              }
          }

          /** Called when auto-verification completes (LoginViewModel.AutoVerified path). */
          fun verifyWithCredential(credential: PhoneAuthCredential) {
              viewModelScope.launch {
                  _uiState.value = OtpUiState.Loading
                  signInWithCredential(credential)
              }
          }

          private suspend fun signInWithCredential(credential: PhoneAuthCredential) {
              firebaseAuth.signInWithCredential(credential)
                  .addOnSuccessListener { result ->
                      viewModelScope.launch {
                          val idToken = result.user?.getIdToken(false)?.await()?.token
                          if (idToken == null) {
                              _uiState.value = OtpUiState.Error("Could not retrieve Firebase token")
                              return@launch
                          }
                          when (val r = authRepository.verifyFirebaseToken(idToken, mobileNumber)) {
                              is Result.Success -> _uiState.value = OtpUiState.Verified(r.data.isNewUser)
                              is Result.Error -> _uiState.value = OtpUiState.Error(r.message)
                          }
                      }
                  }
                  .addOnFailureListener { e ->
                      _uiState.value = OtpUiState.Error(e.message ?: "Verification failed")
                  }
          }

          fun resendOtp(activity: Activity) {
              // Resend is handled by restarting Firebase Phone Auth from LoginViewModel.
              // Navigate back to LoginScreen to trigger a fresh send.
              // This ViewModel signals Idle so OTPScreen can call onBack.
              _uiState.value = OtpUiState.Idle
          }

          fun resetError() { _uiState.value = OtpUiState.Idle }
      }
      ```

      The `OtpUiState` sealed class stays unchanged (Idle, Loading, Resending, Verified, Error).

      Imports needed: `android.app.Activity`, `com.google.android.gms.tasks.await` (from `kotlinx-coroutines-play-services`), `com.google.firebase.auth.*`.

      **Note on `await()`:** Firebase Tasks don't natively suspend. Add `kotlinx-coroutines-play-services` to the version catalog and `app/build.gradle.kts`:
      - `libs.versions.toml` `[versions]`: `coroutinesPlayServices = "1.9.0"`
      - `libs.versions.toml` `[libraries]`: `kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutinesPlayServices" }`
      - `app/build.gradle.kts` dependencies: `implementation(libs.kotlinx.coroutines.play.services)`

      Files:
      - `app/src/main/java/com/chitsmart/ui/auth/OtpViewModel.kt`
      - `gradle/libs.versions.toml` (add coroutinesPlayServices)
      - `app/build.gradle.kts` (add coroutines-play-services dep)

      Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 9. Update `OTPScreen.kt` — wire the `AutoVerified` path and update `resendOtp` call
      The OTPScreen UI visuals are unchanged. Three targeted updates:

      1. Add an `Activity` local:
         ```kotlin
         val activity = LocalContext.current as Activity
         ```
      2. In the `LaunchedEffect(uiState)` block that handles `OtpUiState.Idle` (the resend-complete reset): also add a LaunchedEffect that runs once at composition time to check if LoginViewModel signalled `AutoVerified`. The simplest approach: accept `pendingCredential: PhoneAuthCredential? = null` as a parameter and call `viewModel.verifyWithCredential(credential)` inside a `LaunchedEffect(pendingCredential)`. However, threading a credential through nav args is impractical; instead, use a shared `SavedStateHandle`-backed `LoginViewModel` obtained via `hiltViewModel()` at the `NavGraph` level. The clean solution is:
         - `OTPScreen` acquires `loginViewModel: LoginViewModel = hiltViewModel()` alongside `viewModel: OtpViewModel`.
         - In a `LaunchedEffect(Unit)`, check `loginViewModel._pendingCredential.value`; if non-null, call `viewModel.verifyWithCredential(it)`.
      3. Update the Resend SMS `clickable` lambda:
         ```kotlin
         .clickable(enabled = canResend && !isResending) {
             otp = ""
             countdownSeconds = 45
             canResend = false
             onBack()   // navigate back to LoginScreen to trigger a fresh send
         }
         ```
         Remove the call to `viewModel.resendOtp()`.

      Trust row text updates (cosmetic, keep structure): change "Rate-limited: 3 attempts per hour" → "Secured by Firebase Phone Auth" to reflect the new provider.

      Files: `app/src/main/java/com/chitsmart/ui/auth/OTPScreen.kt`
      Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 10. Update `NavGraph.kt` — propagate `verificationId` nav arg and updated `LoginScreen` signature
       The nav graph currently navigates from Login → OTP passing only `mobileNumber`. Now it must also pass `verificationId`.

       Changes:
       1. Update `AppRoutes`:
          ```kotlin
          const val OTP = "otp/{mobileNumber}/{verificationId}"
          fun otp(mobileNumber: String, verificationId: String) = "otp/$mobileNumber/${Uri.encode(verificationId)}"
          ```
       2. Update `composable(AppRoutes.LOGIN)` → `LoginScreen` call signature:
          ```kotlin
          LoginScreen(
              onOtpSent = { mobile, verificationId ->
                  navController.navigate(AppRoutes.otp(mobile, verificationId))
              },
              onRegister = { navController.navigate(AppRoutes.registration("")) },
          )
          ```
       3. Update `composable(route = AppRoutes.OTP, arguments = ...)`:
          ```kotlin
          arguments = listOf(
              navArgument("mobileNumber") { type = NavType.StringType },
              navArgument("verificationId") { type = NavType.StringType; defaultValue = "" },
          )
          ```
          The `backStack` will deliver `verificationId` via `SavedStateHandle` automatically to `OtpViewModel`.
       4. Add `import android.net.Uri`.

       Files: `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`
       Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 11. Update `AuthModule.kt` — inject `FirebaseAuth` into `AuthRepository` and ViewModels
       `FirebaseAuth` is a singleton obtained via `FirebaseAuth.getInstance()`. Provide it via Hilt so it can be injected into `LoginViewModel` and `OtpViewModel`.

       Add to `AuthModule.companion object`:
       ```kotlin
       @Provides
       @Singleton
       fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
       ```
       The existing `provideAuthRepository()` signature stays unchanged (it only takes `api` and `tokenStorage`).
       Hilt will inject `FirebaseAuth` directly into `LoginViewModel` and `OtpViewModel` since they use `@Inject constructor`.

       Import: `com.google.firebase.auth.FirebaseAuth`.

       Files: `app/src/main/java/com/chitsmart/di/AuthModule.kt`
       Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 12. Update `NetworkModule.kt` — update `unauthenticatedPaths` exclusion list
       Replace the stale path names:
       ```kotlin
       private val unauthenticatedPaths = setOf(
           "auth/verify-firebase-token",
           "auth/refresh",
       )
       ```
       (Remove `auth/send-otp` and `auth/verify-otp`; the new token verification endpoint is also unauthenticated because the caller doesn't yet have a ChitSmart JWT.)

       Files: `app/src/main/java/com/chitsmart/di/NetworkModule.kt`
       Verify: `./gradlew :app:compileDebugKotlin` — no errors.

- [ ] 13. Add `firebase-admin==6.6.0` to `backend/requirements.txt`
       Add the line:
       ```
       firebase-admin==6.6.0
       ```
       Place it under the `# Auth / Security` block.

       Files: `backend/requirements.txt`
       Verify: `cd backend && pip install -r requirements.txt --dry-run` succeeds (or `pip install firebase-admin==6.6.0` on a dev machine confirms the version is available on PyPI).

- [ ] 14. Update `backend/app/core/config.py` — add Firebase setting, remove SMS/OTP bypass settings
       Remove these fields from `Settings`:
       - `OTP_EXPIRE_MINUTES`
       - `OTP_LENGTH`
       - `DEV_OTP_BYPASS`
       - `SMS_PROVIDER`
       - `SMS_API_KEY`
       - `SMS_API_SECRET`
       - `SMS_SENDER_ID`

       Add:
       ```python
       # Firebase Admin SDK — path to the service account key JSON file.
       # Never commit the key file itself; add firebase-service-account.json to .gitignore.
       FIREBASE_SERVICE_ACCOUNT_PATH: str = "firebase-service-account.json"
       ```

       The field has a sensible default so the server still starts even when the file is missing (it will fail at auth time, not at startup — that is acceptable for local dev without Firebase).

       Files: `backend/app/core/config.py`
       Verify: `cd backend && python -c "from app.core.config import settings; print(settings.FIREBASE_SERVICE_ACCOUNT_PATH)"` — prints the configured path without import errors.

- [ ] 15. Replace `send-otp` and `verify-otp` endpoints with `verify-firebase-token` in `backend/app/api/v1/endpoints/auth.py`
       **Remove entirely:**
       - `SendOtpRequest`, `SendOtpResponse`, `VerifyOtpRequest` Pydantic models.
       - `check_otp_rate_limit()` function.
       - `@router.post("/send-otp")` handler.
       - `@router.post("/verify-otp")` handler.
       - All imports only used by those two handlers: `hashlib`, `secrets` (keep `secrets` if used elsewhere — it is also used in `verify_otp` for refresh token generation, but that moves into the new handler), `send_sms_otp` service import.

       **Add:**
       ```python
       import firebase_admin
       from firebase_admin import auth as firebase_auth, credentials

       # Module-level Firebase initialisation (idempotent)
       def _get_firebase_app():
           try:
               return firebase_admin.get_app()
           except ValueError:
               cred = credentials.Certificate(settings.FIREBASE_SERVICE_ACCOUNT_PATH)
               return firebase_admin.initialize_app(cred)

       class VerifyFirebaseTokenRequest(BaseModel):
           firebase_id_token: str
           mobile_number: str

           @field_validator("mobile_number")
           @classmethod
           def validate_mobile(cls, v: str) -> str:
               import re
               if not re.match(r"^\+91[6-9]\d{9}$", v):
                   raise ValueError("Invalid Indian mobile number.")
               return v

       @router.post("/verify-firebase-token", response_model=AuthTokenResponse)
       async def verify_firebase_token(
           request: VerifyFirebaseTokenRequest,
           db: AsyncSession = Depends(get_db),
       ) -> AuthTokenResponse:
           """
           Verify a Firebase ID token issued after phone OTP verification.
           If the token is valid and the phone number matches, issue ChitSmart JWT tokens.
           Creates a member record if the mobile number is new.
           """
           import secrets as _secrets
           from app.models.member import AccountStatus, KYCStatus, Member
           from app.models.member_session import MemberSession

           # Verify the Firebase token
           _get_firebase_app()
           try:
               decoded = firebase_auth.verify_id_token(request.firebase_id_token)
           except Exception as e:
               raise HTTPException(
                   status_code=status.HTTP_401_UNAUTHORIZED,
                   detail=f"Firebase token verification failed: {e}",
               )

           # Confirm phone number in token matches what the app sent
           token_phone = decoded.get("phone_number", "")
           if token_phone != request.mobile_number:
               raise HTTPException(
                   status_code=status.HTTP_401_UNAUTHORIZED,
                   detail="Phone number mismatch",
               )

           # Find or create member
           member_result = await db.execute(
               select(Member).where(Member.mobile_number == request.mobile_number)
           )
           member = member_result.scalar_one_or_none()
           is_new_user = member is None

           if is_new_user:
               member = Member(
                   mobile_number=request.mobile_number,
                   full_name="",
                   account_status=AccountStatus.ACTIVE,
                   kyc_status=KYCStatus.NOT_REQUIRED,
                   is_mobile_verified=True,
               )
               db.add(member)
               await db.commit()
               await db.refresh(member)
           else:
               member.is_mobile_verified = True
               await db.commit()

           # Issue ChitSmart tokens
           access_token = create_access_token(subject=str(member.id))
           refresh_token = _secrets.token_urlsafe(64)
           refresh_hash = hash_refresh_token(refresh_token)

           member_session = MemberSession(
               member_id=member.id,
               refresh_token_hash=refresh_hash,
               expires_at=datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
           )
           db.add(member_session)
           await db.commit()

           return AuthTokenResponse(
               is_new_user=is_new_user,
               access_token=access_token,
               refresh_token=refresh_token,
           )
       ```

       The `register`, `refresh`, and `logout` handlers are untouched.

       Files: `backend/app/api/v1/endpoints/auth.py`
       Verify: `cd backend && python -c "from app.api.v1.endpoints.auth import router; print([r.path for r in router.routes])"` — output must include `/verify-firebase-token` and must NOT include `/send-otp` or `/verify-otp`.

- [ ] 16. Update `backend/.env.example` — remove SMS/OTP bypass keys, add Firebase path
       Remove these lines:
       ```
       OTP_EXPIRE_MINUTES=10
       DEV_OTP_BYPASS=000000
       SMS_PROVIDER=twilio
       SMS_API_KEY=
       SMS_API_SECRET=
       SMS_SENDER_ID=CHTSMT
       ```
       Add (after the SECRET_KEY block):
       ```
       # Firebase Admin SDK — path to service account key JSON (never commit the file itself)
       FIREBASE_SERVICE_ACCOUNT_PATH=firebase-service-account.json
       ```

       Files: `backend/.env.example`
       Verify: visual inspection that the file no longer references Twilio or DEV_OTP_BYPASS.

- [ ] 17. Run full Android compile and backend import checks
       This is the end-to-end verification step confirming all pieces fit together.

       Android:
       ```
       ./gradlew assembleDebug
       ```
       Expected: BUILD SUCCESSFUL, no unresolved references related to `sendOtp`, `verifyOtp`, `SendOtpRequest`, or `VerifyOtpRequest`.

       Backend:
       ```
       cd backend
       python -m pytest tests/ -x --ignore=tests/integration -q
       ```
       Expected: existing tests pass. Any test that imports `send_otp` or `verify_otp` endpoint helpers must be updated to use `verify_firebase_token` instead (if such tests exist in `tests/` — check `tests/test_auth.py` if present).

       Files: no new files — this is a verification-only step.
       Verify: both commands exit 0.

---

## What is NOT changed by this plan

| Item | Reason |
|---|---|
| `otp_sessions` DB table and Alembic migration | Keep per architecture constraints; the table is now unused but retained for future use. |
| `register`, `refresh`, `logout` endpoints | Unchanged — they were never part of the custom OTP flow. |
| `OTPScreen.kt` visuals | All UI composables, colours, and layout are preserved; only the ViewModel calls are rewired. |
| `NetworkModule` token authenticator | The 401 refresh-and-retry logic is unchanged. |
| KYC fields, Aadhaar/PAN | Not touched — deferred. |
| `kyc_status` column default `NOT_REQUIRED` | Preserved in the `verify-firebase-token` handler (same as the old `verify-otp` handler). |
| Payout endpoints, ledger, reports | Unrelated to auth. |

---

## Files touched summary

| File | Change type |
|---|---|
| `gradle/libs.versions.toml` | Add Firebase BOM + google-services + coroutines-play-services |
| `build.gradle.kts` (root) | Add google-services plugin (apply false) |
| `app/build.gradle.kts` | Add google-services plugin + Firebase BOM + Auth + coroutines-play-services |
| `.gitignore` | Add `firebase-service-account.json` exclusion |
| `app/src/main/java/com/chitsmart/data/api/AuthApiService.kt` | Remove send/verifyOtp; add verifyFirebaseToken |
| `app/src/main/java/com/chitsmart/data/repository/AuthRepository.kt` | Remove sendOtp/verifyOtp; add verifyFirebaseToken |
| `app/src/main/java/com/chitsmart/ui/auth/LoginViewModel.kt` | Full rewrite with Firebase Phone Auth |
| `app/src/main/java/com/chitsmart/ui/auth/LoginScreen.kt` | Add Activity context, update signature + LaunchedEffect |
| `app/src/main/java/com/chitsmart/ui/auth/OtpViewModel.kt` | Full rewrite with Firebase credential verification |
| `app/src/main/java/com/chitsmart/ui/auth/OTPScreen.kt` | Wire auto-verify path, update resend to navigate back |
| `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt` | Add verificationId nav arg, update otp() builder |
| `app/src/main/java/com/chitsmart/di/AuthModule.kt` | Add FirebaseAuth @Provides |
| `app/src/main/java/com/chitsmart/di/NetworkModule.kt` | Update unauthenticatedPaths |
| `backend/requirements.txt` | Add firebase-admin==6.6.0 |
| `backend/app/core/config.py` | Remove SMS/OTP fields; add FIREBASE_SERVICE_ACCOUNT_PATH |
| `backend/app/api/v1/endpoints/auth.py` | Remove send-otp/verify-otp; add verify-firebase-token |
| `backend/.env.example` | Remove SMS/OTP bypass vars; add FIREBASE_SERVICE_ACCOUNT_PATH |

---

## One-time setup steps (outside code)

1. **Firebase Console:** Create/use a Firebase project → add Android app with package `com.chitsmart` → enable Phone Auth sign-in method → download `google-services.json` → place at `app/google-services.json`.
2. **Firebase Admin:** In Firebase Console → Project Settings → Service Accounts → Generate new private key → save as `backend/firebase-service-account.json` (never commit).
3. Set `FIREBASE_SERVICE_ACCOUNT_PATH=backend/firebase-service-account.json` in `backend/.env`.
