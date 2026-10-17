package com.chitsmart.data.repository

import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.api.LogoutRequest
import com.chitsmart.data.api.RefreshRequest
import com.chitsmart.data.api.RegisterRequest
import com.chitsmart.data.api.VerifyFirebaseTokenRequest
import com.chitsmart.data.local.TokenStorage
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for all authentication operations.
 * Wraps [AuthApiService] calls in try/catch and maps HTTP responses
 * to the sealed [Result] type. Persists tokens to [TokenStorage] on success.
 */
@Singleton
class AuthRepository
    @Inject
    constructor(
        private val api: AuthApiService,
        private val tokenStore: TokenStorage,
    ) {
        /**
         * Verify a Firebase Phone Auth ID token with the ChitSmart backend.
         * On success, saves both access and refresh tokens to [TokenStorage].
         * Returns [Result.Success] with [VerifyResult] containing [isNewUser].
         */
        suspend fun verifyFirebaseToken(
            firebaseIdToken: String,
            mobileNumber: String,
        ): Result<VerifyResult> =
            safeCall {
                val response =
                    api.verifyFirebaseToken(
                        VerifyFirebaseTokenRequest(
                            firebaseIdToken = firebaseIdToken,
                            mobileNumber = mobileNumber,
                        ),
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

        /**
         * Complete registration for a new user after Firebase verification.
         * On success, saves the refreshed access token.
         */
        suspend fun register(
            fullName: String,
            email: String?,
            dob: String?,
            gender: String?,
        ): Result<Unit> =
            safeCall {
                val response = api.register(RegisterRequest(fullName, email, dob, gender))
                when {
                    response.isSuccessful -> {
                        val body = response.body()!!
                        tokenStore.saveAccessToken(body.accessToken)
                        Result.Success(Unit)
                    }
                    response.code() == 422 -> Result.Error("Please check your details.")
                    response.code() == 401 -> Result.Error("Session expired. Please log in again.")
                    else -> Result.Error("Something went wrong. Please try again.")
                }
            }

        /**
         * Exchange the stored refresh token for a new access token.
         */
        suspend fun refreshToken(): Result<Unit> =
            safeCall {
                val refresh =
                    tokenStore.getRefreshToken()
                        ?: return@safeCall Result.Error("No refresh token. Please log in again.")
                val response = api.refreshToken(RefreshRequest(refresh))
                when {
                    response.isSuccessful -> {
                        val body = response.body()!!
                        tokenStore.saveAccessToken(body.accessToken)
                        Result.Success(Unit)
                    }
                    else -> Result.Error("Session expired. Please log in again.")
                }
            }

        /**
         * Revoke the server-side session and clear stored tokens.
         */
        suspend fun logout(): Result<Unit> =
            safeCall {
                val refresh = tokenStore.getRefreshToken()
                if (refresh != null) {
                    api.logout(LogoutRequest(refresh))
                }
                tokenStore.clearTokens()
                Result.Success(Unit)
            }

        // ── Private helpers ───────────────────────────────────────────────────────

        /**
         * Wraps a suspend block in try/catch, converting [IOException] to a
         * user-facing network error and any other [Exception] to a generic error.
         */
        private suspend inline fun <T> safeCall(block: () -> Result<T>): Result<T> {
            return try {
                block()
            } catch (e: IOException) {
                Result.Error("No internet connection. Please check your network.")
            } catch (e: Exception) {
                Result.Error("Something went wrong. Please try again.")
            }
        }
    }

/** Value returned by [AuthRepository.verifyFirebaseToken] on success. */
data class VerifyResult(val isNewUser: Boolean)
