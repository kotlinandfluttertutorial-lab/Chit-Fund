package com.chitsmart.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ── Request bodies ────────────────────────────────────────────────────────────

data class SendOtpRequest(
    @SerializedName("mobile_number") val mobileNumber: String,
)

data class VerifyOtpRequest(
    @SerializedName("mobile_number") val mobileNumber: String,
    @SerializedName("otp") val otp: String,
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

data class SendOtpResponse(
    @SerializedName("message") val message: String,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int,
)

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
    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest,
    ): Response<SendOtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest,
    ): Response<AuthTokenResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<TokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequest,
    ): Response<TokenResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest,
    ): Response<Unit>
}
