package com.chitsmart.di

import com.chitsmart.BuildConfig
import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.local.TokenStore
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
import javax.inject.Singleton

/**
 * Hilt module — provides OkHttpClient, Retrofit, and AuthApiService.
 *
 * The AuthInterceptor attaches the Bearer token to every request.
 * The TokenAuthenticator handles 401 responses by refreshing the access token
 * and retrying once (standard OkHttp Authenticator pattern).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Routes that must NOT receive the Authorization header.
    private val unauthenticatedPaths =
        setOf(
            "auth/send-otp",
            "auth/verify-otp",
            "auth/refresh",
        )

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenStore: TokenStore): OkHttpClient {
        val logging =
            HttpLoggingInterceptor().apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }

        val authInterceptor =
            okhttp3.Interceptor { chain ->
                val original = chain.request()
                val pathSegments = original.url.encodedPath

                val isAuthEndpoint =
                    unauthenticatedPaths.any { path ->
                        pathSegments.contains(path)
                    }

                if (isAuthEndpoint) {
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

        val tokenAuthenticator =
            object : Authenticator {
                override fun authenticate(
                    route: Route?,
                    response: Response,
                ): Request? {
                    // Prevent infinite retry loops
                    if (response.request.header("X-Retry-Auth") != null) return null

                    val refreshToken = tokenStore.getRefreshToken() ?: return null

                    // Synchronously call the refresh endpoint
                    val refreshClient = OkHttpClient()
                    val refreshRequest =
                        okhttp3.Request.Builder()
                            .url("${BuildConfig.API_BASE_URL}auth/refresh")
                            .post(
                                """{"refresh_token":"$refreshToken"}"""
                                    .toRequestBody("application/json".toMediaType()),
                            )
                            .build()

                    return try {
                        val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                        if (refreshResponse.isSuccessful) {
                            val body = refreshResponse.body?.string() ?: return null
                            // Parse the new access token from JSON manually to avoid circular dep
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

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)
}
