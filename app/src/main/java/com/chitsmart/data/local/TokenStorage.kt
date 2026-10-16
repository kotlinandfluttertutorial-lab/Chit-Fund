package com.chitsmart.data.local

/**
 * Contract for JWT token persistence.
 * Implemented by [TokenStore] (EncryptedSharedPreferences) in production
 * and by an in-memory fake in unit tests.
 */
interface TokenStorage {
    fun saveAccessToken(token: String)
    fun saveRefreshToken(token: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
}
