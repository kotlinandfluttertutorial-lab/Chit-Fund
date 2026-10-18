package com.chitsmart.utils

import com.chitsmart.BuildConfig

/**
 * Single source of truth for all build-variant configuration at runtime.
 *
 * Values are resolved from [BuildConfig] fields that are injected by Gradle
 * for each product flavor + build type combination:
 *
 *  Variant          API_BASE_URL                      IS_LOGGING_ENABLED
 *  ───────────────  ────────────────────────────────  ──────────────────
 *  localDebug       http://127.0.0.1:8000/api/v1/      true
 *  localRelease     http://127.0.0.1:8000/api/v1/      true
 *  cloudDebug       https://api.chitsmart.app/…        true  (debug override)
 *  cloudRelease     https://api.chitsmart.app/…        false
 *
 * Usage (anywhere in the app):
 *   val url = AppConfig.apiBaseUrl
 *   if (AppConfig.isLoggingEnabled) Timber.plant(Timber.DebugTree())
 */
object AppConfig {
    /** Base URL for all Retrofit calls — trailing slash guaranteed by Gradle. */
    val apiBaseUrl: String = BuildConfig.API_BASE_URL

    /**
     * True when HTTP body logging and Timber debug trees should be active.
     * Always true in debug builds; controlled by flavor in release builds.
     */
    val isLoggingEnabled: Boolean = BuildConfig.IS_LOGGING_ENABLED

    /**
     * Identifies which environment this APK targets.
     * Values: "local" | "cloud"
     */
    val buildVariant: String = BuildConfig.BUILD_VARIANT

    /** Convenience helpers */
    val isLocal: Boolean get() = buildVariant == "local"
    val isCloud: Boolean get() = buildVariant == "cloud"

    /**
     * OkHttp connect timeout (ms).
     * Shorter for local dev (fast LAN); longer for cloud (mobile networks).
     */
    val connectTimeoutMs: Long = if (isLocal) 10_000L else 30_000L

    /**
     * OkHttp read/write timeout (ms).
     */
    val readTimeoutMs: Long = if (isLocal) 30_000L else 60_000L

    /**
     * Retry failed requests at the network layer.
     * Disabled locally so failures surface immediately during development.
     */
    val retryOnConnectionFailure: Boolean = isCloud
}
