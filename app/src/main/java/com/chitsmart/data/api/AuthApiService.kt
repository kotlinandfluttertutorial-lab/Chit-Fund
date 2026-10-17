package com.chitsmart.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ── Request bodies ────────────────────────────────────────────────────────────

data class VerifyFirebaseTokenRequest(
    @SerializedName("firebase_id_token") val firebaseIdToken: String,
    @SerializedName("mobile_number") val mobileNumber: String,
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String?,
    @SerializedName("date_of_birth") val dateOfBirth: String?,
    @SerializedName("gender") val gender: String?,
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

// ── Response bodies ───────────────────────────────────────────────────────────

data class AuthTokenResponse(
    @SerializedName("is_new_user") val isNewUser: Boolean,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
)

// ── API service interface ─────────────────────────────────────────────────────

interface AuthApiService {
    @POST("auth/verify-firebase-token")
    suspend fun verifyFirebaseToken(
        @Body request: VerifyFirebaseTokenRequest,
    ): Response<AuthTokenResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<AuthTokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequest,
    ): Response<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest,
    ): Response<Unit>
}
