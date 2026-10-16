package com.chitsmart.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure token storage using EncryptedSharedPreferences.
 * Stores and retrieves JWT access and refresh tokens.
 */
@Singleton
class TokenStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TokenStorage {
        private val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val prefs =
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        override fun saveAccessToken(token: String) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        }

        override fun saveRefreshToken(token: String) {
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        }

        override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

        override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

        override fun clearTokens() {
            prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply()
        }

        companion object {
            private const val PREFS_NAME = "chitsmart_secure_prefs"
            private const val KEY_ACCESS_TOKEN = "access_token"
            private const val KEY_REFRESH_TOKEN = "refresh_token"
        }
    }
