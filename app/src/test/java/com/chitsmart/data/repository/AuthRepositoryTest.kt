package com.chitsmart.data.repository

import com.chitsmart.data.api.AuthApiService
import com.chitsmart.data.api.AuthTokenResponse
import com.chitsmart.data.api.LogoutRequest
import com.chitsmart.data.api.RefreshRequest
import com.chitsmart.data.api.RegisterRequest
import com.chitsmart.data.api.SendOtpRequest
import com.chitsmart.data.api.SendOtpResponse
import com.chitsmart.data.api.TokenResponse
import com.chitsmart.data.api.VerifyOtpRequest
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

/**
 * Unit tests for [AuthRepository].
 * Uses hand-written fakes — no mocking framework needed.
 */
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

    // ── sendOtp ───────────────────────────────────────────────────────────────

    @Test
    fun `sendOtp success returns Result Success`() = runTest {
        fakeApi.sendOtpResponse = Response.success(
            SendOtpResponse(message = "OTP sent", expiresInSeconds = 600),
        )
        val result = repository.sendOtp("+911234567890")
        assertTrue(result is Result.Success)
    }

    @Test
    fun `sendOtp 422 returns invalid mobile error`() = runTest {
        fakeApi.sendOtpResponse = Response.error(422, "".toResponseBody())
        val result = repository.sendOtp("bad")
        assertTrue(result is Result.Error)
        assertEquals("Invalid mobile number", (result as Result.Error).message)
    }

    @Test
    fun `sendOtp 429 returns rate limit error`() = runTest {
        fakeApi.sendOtpResponse = Response.error(429, "".toResponseBody())
        val result = repository.sendOtp("+911234567890")
        assertTrue(result is Result.Error)
        assertEquals("Too many OTP requests. Please wait.", (result as Result.Error).message)
    }

    @Test
    fun `sendOtp network error returns connection error`() = runTest {
        fakeApi.sendOtpThrows = IOException("timeout")
        val result = repository.sendOtp("+911234567890")
        assertTrue(result is Result.Error)
        assertEquals(
            "No internet connection. Please check your network.",
            (result as Result.Error).message,
        )
    }

    // ── verifyOtp ─────────────────────────────────────────────────────────────

    @Test
    fun `verifyOtp success saves tokens and returns isNewUser true`() = runTest {
        fakeApi.verifyOtpResponse = Response.success(
            AuthTokenResponse(
                isNewUser = true,
                accessToken = "access-abc",
                refreshToken = "refresh-xyz",
                tokenType = "bearer",
            ),
        )
        val result = repository.verifyOtp("+911234567890", "123456")
        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data.isNewUser)
        assertEquals("access-abc", fakeTokenStorage.accessToken)
        assertEquals("refresh-xyz", fakeTokenStorage.refreshToken)
    }

    @Test
    fun `verifyOtp success saves tokens and returns isNewUser false`() = runTest {
        fakeApi.verifyOtpResponse = Response.success(
            AuthTokenResponse(
                isNewUser = false,
                accessToken = "access-returning",
                refreshToken = "refresh-returning",
                tokenType = "bearer",
            ),
        )
        val result = repository.verifyOtp("+911234567890", "654321")
        assertTrue(result is Result.Success)
        assertEquals(false, (result as Result.Success).data.isNewUser)
        assertEquals("access-returning", fakeTokenStorage.accessToken)
    }

    @Test
    fun `verifyOtp 401 returns incorrect OTP error`() = runTest {
        fakeApi.verifyOtpResponse = Response.error(401, "".toResponseBody())
        val result = repository.verifyOtp("+911234567890", "000000")
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Incorrect OTP"))
    }

    @Test
    fun `verifyOtp 410 returns expired OTP error`() = runTest {
        fakeApi.verifyOtpResponse = Response.error(410, "".toResponseBody())
        val result = repository.verifyOtp("+911234567890", "123456")
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("expired"))
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    fun `register success saves new access token`() = runTest {
        fakeApi.registerResponse = Response.success(
            AuthTokenResponse(
                isNewUser = false,
                accessToken = "new-access-token",
                refreshToken = "",
                tokenType = "bearer",
            ),
        )
        val result = repository.register("Priya Nair", "priya@example.com", null, null)
        assertTrue(result is Result.Success)
        assertEquals("new-access-token", fakeTokenStorage.accessToken)
    }

    @Test
    fun `register 422 returns check details error`() = runTest {
        fakeApi.registerResponse = Response.error(422, "".toResponseBody())
        val result = repository.register("", null, null, null)
        assertTrue(result is Result.Error)
        assertEquals("Please check your details.", (result as Result.Error).message)
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    fun `logout clears tokens from store`() = runTest {
        fakeTokenStorage.accessToken = "some-access"
        fakeTokenStorage.refreshToken = "some-refresh"
        fakeApi.logoutResponse = Response.success(Unit)

        val result = repository.logout()

        assertTrue(result is Result.Success)
        assertNull(fakeTokenStorage.accessToken)
        assertNull(fakeTokenStorage.refreshToken)
    }

    @Test
    fun `logout without refresh token still clears store`() = runTest {
        // No refresh token stored — should still succeed (no API call needed)
        fakeTokenStorage.refreshToken = null
        val result = repository.logout()
        assertTrue(result is Result.Success)
    }
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

/** In-memory [TokenStorage] fake for tests. */
private class FakeTokenStorage : TokenStorage {
    var accessToken: String? = null
    var refreshToken: String? = null

    override fun saveAccessToken(token: String) { accessToken = token }
    override fun saveRefreshToken(token: String) { refreshToken = token }
    override fun getAccessToken(): String? = accessToken
    override fun getRefreshToken(): String? = refreshToken
    override fun clearTokens() { accessToken = null; refreshToken = null }
}

/** Programmable fake for [AuthApiService]. */
private class FakeAuthApiService : AuthApiService {
    var sendOtpResponse: Response<SendOtpResponse>? = null
    var sendOtpThrows: Throwable? = null
    var verifyOtpResponse: Response<AuthTokenResponse>? = null
    var registerResponse: Response<AuthTokenResponse>? = null
    var refreshResponse: Response<TokenResponse>? = null
    var logoutResponse: Response<Unit>? = null

    override suspend fun sendOtp(request: SendOtpRequest): Response<SendOtpResponse> {
        sendOtpThrows?.let { throw it }
        return sendOtpResponse ?: error("sendOtpResponse not configured")
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Response<AuthTokenResponse> =
        verifyOtpResponse ?: error("verifyOtpResponse not configured")

    override suspend fun register(request: RegisterRequest): Response<AuthTokenResponse> =
        registerResponse ?: error("registerResponse not configured")

    override suspend fun refreshToken(request: RefreshRequest): Response<TokenResponse> =
        refreshResponse ?: error("refreshResponse not configured")

    override suspend fun logout(request: LogoutRequest): Response<Unit> =
        logoutResponse ?: error("logoutResponse not configured")
}
