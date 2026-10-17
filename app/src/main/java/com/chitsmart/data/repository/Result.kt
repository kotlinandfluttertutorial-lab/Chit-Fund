package com.chitsmart.data.repository

/**
 * Sealed result type for repository operations.
 * Decouples API response codes from UI state handling.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()

    data class Error(val message: String) : Result<Nothing>()
}
