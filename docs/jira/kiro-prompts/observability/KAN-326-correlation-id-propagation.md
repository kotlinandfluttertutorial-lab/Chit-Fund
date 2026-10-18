# Kiro Prompt — KAN-326: Correlation ID Propagation

**Ticket:** KAN-326  
**Epic:** KAN-317 — Observability  
**Priority:** High  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-010  
**Prerequisites:** KAN-318 (logging middleware — backend side already done)  
**Files:**
- `app/src/main/java/com/chitsmart/core/network/ApiClient.kt` (Android — add header)
- `admin/src/api/client.ts` (Admin dashboard — log correlation ID on error)

---

## Objective

Complete the correlation ID flow across all three layers:
1. **Backend** — already done in KAN-318 (reads/generates header, returns in response).
2. **Android** — send `X-Correlation-ID` on every API request; store last value for Crashlytics.
3. **Admin dashboard** — log `X-Correlation-ID` from every 4xx/5xx API error response.

## Repository inspection

Before writing any code:
1. Read `app/src/main/java/com/chitsmart/core/network/ApiClient.kt` (or the equivalent
   Retrofit/OkHttp client file) — understand how the HTTP client is constructed.
2. Read `admin/src/api/client.ts` — understand the Axios or fetch-based client.
3. Read `backend/app/core/middleware.py` — confirm `X-Correlation-ID` is already
   returned in the response header (from KAN-318).

## Android — OkHttp interceptor

### `app/src/main/java/com/chitsmart/core/network/CorrelationIdInterceptor.kt` (new)

```kotlin
package com.chitsmart.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * Adds X-Correlation-ID to every outbound request.
 * Stores the latest correlation ID for Crashlytics (KAN-327).
 */
object CorrelationIdInterceptor : Interceptor {

    @Volatile
    var lastCorrelationId: String = ""
        private set

    override fun intercept(chain: Interceptor.Chain): Response {
        val correlationId = UUID.randomUUID().toString()
        val request = chain.request().newBuilder()
            .addHeader("X-Correlation-ID", correlationId)
            .build()
        val response = chain.proceed(request)
        // Read back whatever the server returns (may be the same UUID)
        lastCorrelationId = response.header("X-Correlation-ID") ?: correlationId
        return response
    }
}
```

### Register in the OkHttp client — `ApiClient.kt`

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(tokenRepository))
    .addInterceptor(CorrelationIdInterceptor)   // ← add after auth interceptor
    .build()
```

## Admin dashboard — API error logging

### `admin/src/api/client.ts`

Add a response interceptor that logs the correlation ID on every 4xx/5xx:

```typescript
import axios from "axios";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// Add correlation ID to every request
apiClient.interceptors.request.use((config) => {
  config.headers["X-Correlation-ID"] =
    crypto.randomUUID ? crypto.randomUUID() : generateFallbackId();
  return config;
});

// Log correlation ID on API errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status >= 400) {
      const correlationId =
        error.response.headers["x-correlation-id"] ?? "unknown";
      console.error(
        `[ChitSmart] API error ${error.response.status} — ` +
          `correlation_id: ${correlationId} — ` +
          `path: ${error.config?.url}`,
      );
      // If Sentry is integrated (KAN-329), attach the correlation ID
      if (typeof window.__chitsmart_sentry !== "undefined") {
        window.__chitsmart_sentry.setTag("correlation_id", correlationId);
      }
    }
    return Promise.reject(error);
  },
);

function generateFallbackId(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === "x" ? r : (r & 0x3) | 0x8).toString(16);
  });
}

export default apiClient;
```

## Acceptance criteria

- [ ] Every Android API request includes `X-Correlation-ID` header (UUID v4)
- [ ] `CorrelationIdInterceptor.lastCorrelationId` is updated after every response
- [ ] Every admin dashboard API request includes `X-Correlation-ID` header
- [ ] A `console.error` with `correlation_id` is printed for every 4xx/5xx response
  in the admin dashboard
- [ ] The correlation ID in the console log matches the value returned in the
  `X-Correlation-ID` response header
- [ ] Unit tests (Android): interceptor adds header, stores `lastCorrelationId`
- [ ] Unit tests (admin): interceptor logs correlation ID on 500 response

## Definition of done

- Android: `./gradlew :app:testDebugUnitTest` passes including `CorrelationIdInterceptorTest`
- Admin: `npm test` passes including `apiClient.interceptor.test.ts`
- Backend: existing KAN-318 tests already cover the backend side
