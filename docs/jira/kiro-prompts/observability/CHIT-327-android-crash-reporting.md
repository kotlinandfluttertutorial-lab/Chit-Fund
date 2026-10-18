# Kiro Prompt — CHIT-327: Android Crash Reporting

**Ticket:** CHIT-327  
**Epic:** CHIT-317 — Observability  
**Priority:** High  
**Skill:** Use the `android-screen` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-012  
**Prerequisites:** CHIT-326 (correlation ID — `CorrelationIdInterceptor.lastCorrelationId`)  
**Files:**
- `app/build.gradle.kts` (add Crashlytics dependency)
- `app/google-services.json` (placeholder — team must add real file)
- `app/src/main/java/com/chitsmart/ChitSmartApplication.kt`
- `app/src/main/java/com/chitsmart/core/crash/CrashReporting.kt` (new)

---

## Objective

Integrate Firebase Crashlytics to capture crashes, ANRs, and non-fatal exceptions
in release builds. Attach `last_correlation_id` as a Crashlytics custom key so
backend logs can be correlated with crash reports. Must be completely disabled for
debug builds.

## Repository inspection

Before writing any code:
1. Read `app/build.gradle.kts` — check if Firebase BOM or Crashlytics is already present.
2. Read `app/src/main/java/com/chitsmart/ChitSmartApplication.kt` — understand the
   Application class where Crashlytics should be initialised.
3. Read `app/google-services.json` — if missing, add a note in the PR that the team
   must add this file from the Firebase console before Crashlytics will function.

## `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

Root `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
```

## Crash reporting wrapper — `backend/app/core/crash/CrashReporting.kt`

```kotlin
package com.chitsmart.core.crash

import android.util.Log
import com.chitsmart.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Centralised crash reporting. All calls are no-ops in debug builds.
 */
object CrashReporting {

    private val isEnabled: Boolean = !BuildConfig.DEBUG

    fun setUserId(memberId: String) {
        if (!isEnabled) return
        FirebaseCrashlytics.getInstance().setUserId(memberId)
    }

    fun setCorrelationId(correlationId: String) {
        if (!isEnabled) return
        FirebaseCrashlytics.getInstance()
            .setCustomKey("last_correlation_id", correlationId)
    }

    fun recordNonFatalException(throwable: Throwable, context: String = "") {
        if (!isEnabled) {
            Log.w("CrashReporting", "Non-fatal (debug only): $context", throwable)
            return
        }
        if (context.isNotBlank()) {
            FirebaseCrashlytics.getInstance().log(context)
        }
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun log(message: String) {
        if (!isEnabled) return
        FirebaseCrashlytics.getInstance().log(message)
    }
}
```

## Application initialisation — `ChitSmartApplication.kt`

```kotlin
class ChitSmartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Disable Crashlytics in debug builds via manifest / gradle flag
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
```

## Update correlation ID after every API call

In the ViewModel or Repository layer, after every API call completes, update the
Crashlytics key:

```kotlin
// In any ViewModel that makes API calls:
CrashReporting.setCorrelationId(CorrelationIdInterceptor.lastCorrelationId)
```

Or add this to the `CorrelationIdInterceptor` directly:

```kotlin
override fun intercept(chain: Interceptor.Chain): Response {
    // ... existing code ...
    lastCorrelationId = response.header("X-Correlation-ID") ?: correlationId
    CrashReporting.setCorrelationId(lastCorrelationId)
    return response
}
```

## `app/google-services.json`

This file is NOT committed to the repository (already in `.gitignore`). Add a
`google-services.json.example` with a comment explaining how to obtain the real file:

```json
{
  "_comment": "Download this file from the Firebase console for the ChitSmart project.",
  "_comment2": "Place it at app/google-services.json (gitignored).",
  "_comment3": "Without this file, Crashlytics will not function."
}
```

## Acceptance criteria

- [ ] `BuildConfig.DEBUG == true` → Crashlytics collection is disabled; no events sent
- [ ] `BuildConfig.DEBUG == false` (release build) → Crashlytics collection is enabled
- [ ] After `CrashReporting.setCorrelationId("abc-123")`, the Crashlytics key
  `last_correlation_id` is set to `"abc-123"`
- [ ] `CrashReporting.recordNonFatalException(e)` does not throw in debug builds
- [ ] `./gradlew assembleDebug` succeeds (Firebase BOM resolves)
- [ ] `./gradlew assembleRelease` succeeds with a placeholder `google-services.json`
- [ ] Unit tests: all `CrashReporting` methods are no-ops when `DEBUG == true`

## Note for the team

The real `app/google-services.json` must be downloaded from the Firebase console
and placed at `app/google-services.json` before Crashlytics will function in
production. It must not be committed to the repository. Add it to 1Password or your
secrets manager.

## Definition of done

- `./gradlew :app:testDebugUnitTest` passes including `CrashReportingTest`
- `./gradlew assembleDebug` succeeds
- `docs/deployment/local-setup.md` updated with Crashlytics setup note
