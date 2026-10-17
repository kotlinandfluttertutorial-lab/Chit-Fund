package com.chitsmart.data.repository

import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.api.AuthTokenResponse
import com.chitsmart.data.api.LogoutRequest
import com.chitsmart.data.api.RefreshRequest
import com.chitsmart.data.api.RegisterRequest
import com.chitsmart.data.api.TokenResponse
import com.chitsmart.data.api.VerifyFirebaseTokenRequest
import com.chitsmart.data.local.TokenStorage
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class AuthRepositoryTest {
    private lateinit var fakeApi: FakeAuthApiService
    private lateinit var fakeTokenStorage: FakeTokenStorage
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        fakeApi = FakeAuthApiService()
        fakeTokenStorage = FakeTokenStorage()
        repository = AuthRepository(fakeApi, fakeTokenStorage)
    }

    @Test
    fun `verifyFirebaseToken success stores tokens and returns new user status`() =
        runTest {
            fakeApi.verifyFirebaseTokenResponse =
                Response.success(
                    AuthTokenResponse(
                        isNewUser = true,
                        accessToken = "access-token",
                        refreshToken = "refresh-token",
                        tokenType = "bearer",
                    ),
                )

            val result = repository.verifyFirebaseToken("firebase-token", "+911234567890")

            assertEquals(VerifyResult(isNewUser = true), (result as Result.Success).data)
            assertEquals("access-token", fakeTokenStorage.storedAccess)
            assertEquals("refresh-token", fakeTokenStorage.storedRefresh)
        }

    @Test
    fun `verifyFirebaseToken 401 returns verification error`() =
        runTest {
            fakeApi.verifyFirebaseTokenResponse = Response.error(401, "".toResponseBody())

            val result = repository.verifyFirebaseToken("invalid-token", "+911234567890")

            assertEquals("Firebase token verification failed.", (result as Result.Error).message)
        }

    @Test
    fun `verifyFirebaseToken network error returns connection error`() =
        runTest {
            fakeApi.verifyFirebaseTokenThrows = IOException("timeout")

            val result = repository.verifyFirebaseToken("firebase-token", "+911234567890")

            assertEquals(
                "No internet connection. Please check your network.",
                (result as Result.Error).message,
            )
        }

    @Test
    fun `register success stores access token`() =
        runTest {
            fakeApi.registerResponse =
                Response.success(
                    AuthTokenResponse(
                        isNewUser = false,
                        accessToken = "registered-access",
                        refreshToken = "",
                        tokenType = "bearer",
                    ),
                )

            val result = repository.register("Priya Nair", "priya@example.com", null, null)

            assertEquals(Unit, (result as Result.Success).data)
            assertEquals("registered-access", fakeTokenStorage.storedAccess)
        }

    @Test
    fun `register 422 returns check details error`() =
        runTest {
            fakeApi.registerResponse = Response.error(422, "".toResponseBody())

            val result = repository.register("", null, null, null)

            assertEquals("Please check your details.", (result as Result.Error).message)
        }

    @Test
    fun `refreshToken without stored token returns error`() =
        runTest {
            val result = repository.refreshToken()

            assertEquals("No refresh token. Please log in again.", (result as Result.Error).message)
        }

    @Test
    fun `refreshToken success stores new access token`() =
        runTest {
            fakeTokenStorage.saveRefreshToken("stored-refresh")
            fakeApi.refreshTokenResponse =
                Response.success(
                    TokenResponse(accessToken = "refreshed-access", tokenType = "bearer"),
                )

            val result = repository.refreshToken()

            assertEquals(Unit, (result as Result.Success).data)
            assertEquals("refreshed-access", fakeTokenStorage.storedAccess)
        }

    @Test
    fun `logout clears stored tokens`() =
        runTest {
            fakeTokenStorage.saveAccessToken("stored-access")
            fakeTokenStorage.saveRefreshToken("stored-refresh")
            fakeApi.logoutResponse = Response.success(Unit)

            val result = repository.logout()

            assertTrue(result is Result.Success)
            assertNull(fakeTokenStorage.storedAccess)
            assertNull(fakeTokenStorage.storedRefresh)
        }

    @Test
    fun `logout without refresh token still clears stored tokens`() =
        runTest {
            fakeTokenStorage.saveAccessToken("stored-access")

            val result = repository.logout()

            assertTrue(result is Result.Success)
            assertNull(fakeTokenStorage.storedAccess)
        }
}

private class FakeTokenStorage : TokenStorage {
    var storedAccess: String? = null
        private set
    var storedRefresh: String? = null
        private set

    override fun saveAccessToken(token: String) {
        storedAccess = token
    }

    override fun saveRefreshToken(token: String) {
        storedRefresh = token
    }

    override fun getAccessToken(): String? = storedAccess

    override fun getRefreshToken(): String? = storedRefresh

    override fun clearTokens() {
        storedAccess = null
        storedRefresh = null
    }
}

private class FakeAuthApiService : AuthApiService {
    var verifyFirebaseTokenResponse: Response<AuthTokenResponse>? = null
    var verifyFirebaseTokenThrows: Throwable? = null
    var registerResponse: Response<AuthTokenResponse>? = null
    var refreshTokenResponse: Response<TokenResponse>? = null
    var logoutResponse: Response<Unit>? = null

    override suspend fun verifyFirebaseToken(request: VerifyFirebaseTokenRequest): Response<AuthTokenResponse> {
        verifyFirebaseTokenThrows?.let { throw it }
        return verifyFirebaseTokenResponse ?: error("verifyFirebaseTokenResponse not configured")
    }

    override suspend fun register(request: RegisterRequest): Response<AuthTokenResponse> =
        registerResponse ?: error("registerResponse not configured")

    override suspend fun refreshToken(request: RefreshRequest): Response<TokenResponse> =
        refreshTokenResponse ?: error("refreshTokenResponse not configured")

    override suspend fun logout(request: LogoutRequest): Response<Unit> = logoutResponse ?: error("logoutResponse not configured")
}
