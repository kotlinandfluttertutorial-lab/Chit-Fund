package com.chitsmart.di

import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.local.TokenStorage
import com.chitsmart.utils.AppConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module — provides OkHttpClient, Retrofit, and AuthApiService.
 *
 * Build-variant awareness comes from [AppConfig]:
 *  • local  — verbose BODY logging, short timeouts, no connection retry
 *  • cloud  — no logging, longer timeouts, retry on connection failure
 *
 * Token handling:
 *  • AuthInterceptor attaches Bearer header to every authenticated request.
 *  • TokenAuthenticator handles 401 by refreshing the access token (once).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /** Paths that must NOT receive an Authorization header. */
    private val unauthenticatedPaths =
        setOf(
            "auth/verify-firebase-token",
            "auth/refresh",
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenStore: TokenStorage): OkHttpClient {
        // ── Logging interceptor ──────────────────────────────────────────────
        val logging =
            HttpLoggingInterceptor().apply {
                level =
                    if (AppConfig.isLoggingEnabled) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

        // ── Auth interceptor (attach Bearer token) ───────────────────────────
        val authInterceptor =
            okhttp3.Interceptor { chain ->
                val original = chain.request()
                val encodedPath = original.url.encodedPath

                val isUnauthenticated = unauthenticatedPaths.any { encodedPath.contains(it) }
                if (isUnauthenticated) {
                    chain.proceed(original)
                } else {
                    val token = tokenStore.getAccessToken()
                    val request =
                        if (token != null) {
                            original.newBuilder()
                                .header("Authorization", "Bearer $token")
                                .build()
                        } else {
                            original
                        }
                    chain.proceed(request)
                }
            }

        // ── Token authenticator (handle 401 → refresh → retry once) ─────────
        val tokenAuthenticator =
            object : Authenticator {
                override fun authenticate(
                    route: Route?,
                    response: Response,
                ): Request? {
                    // Prevent infinite retry loops
                    if (response.request.header("X-Retry-Auth") != null) return null

                    val refreshToken = tokenStore.getRefreshToken() ?: return null

                    // Synchronously call the refresh endpoint (separate bare client)
                    val refreshClient =
                        OkHttpClient.Builder()
                            .connectTimeout(AppConfig.connectTimeoutMs, TimeUnit.MILLISECONDS)
                            .readTimeout(AppConfig.readTimeoutMs, TimeUnit.MILLISECONDS)
                            .build()

                    val refreshRequest =
                        okhttp3.Request.Builder()
                            .url("${AppConfig.apiBaseUrl}auth/refresh")
                            .post(
                                """{"refresh_token":"$refreshToken"}"""
                                    .toRequestBody("application/json".toMediaType()),
                            )
                            .build()

                    return try {
                        val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                        if (refreshResponse.isSuccessful) {
                            val body = refreshResponse.body?.string() ?: return null
                            val tokenMatch = Regex(""""access_token"\s*:\s*"([^"]+)"""").find(body)
                            val newToken = tokenMatch?.groupValues?.getOrNull(1) ?: return null
                            tokenStore.saveAccessToken(newToken)
                            response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .header("X-Retry-Auth", "true")
                                .build()
                        } else {
                            tokenStore.clearTokens()
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            }

        // ── Assemble the client ──────────────────────────────────────────────
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenAuthenticator)
            .connectTimeout(AppConfig.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(AppConfig.readTimeoutMs, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(AppConfig.retryOnConnectionFailure)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConfig.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)
}
