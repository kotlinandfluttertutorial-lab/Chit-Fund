package com.chitsmart.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chitsmart.data.repository.AuthRepository
import com.chitsmart.data.repository.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class OtpUiState {
    object Idle : OtpUiState()

    object Loading : OtpUiState()

    object Resending : OtpUiState()

    data class Verified(val isNewUser: Boolean) : OtpUiState()

    data class Error(val message: String) : OtpUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class OtpViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val firebaseAuth: FirebaseAuth,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        /** Mobile number passed from LoginScreen via the nav argument "mobileNumber". */
        val mobileNumber: String = savedStateHandle.get<String>("mobileNumber") ?: ""

        /** Firebase verificationId passed via nav argument "verificationId". Empty on auto-verify path. */
        private val verificationId: String = savedStateHandle.get<String>("verificationId") ?: ""

        private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
        val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

        /**
         * Called when user taps "Verify & Continue" with the 6 digits they typed.
         * Creates a [PhoneAuthCredential] from the stored [verificationId] + [otp].
         */
        fun verifyOtp(otp: String) {
            viewModelScope.launch {
                _uiState.value = OtpUiState.Loading
                try {
                    val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                    signInWithCredential(credential)
                } catch (e: Exception) {
                    _uiState.value = OtpUiState.Error("Invalid OTP. Please try again.")
                }
            }
        }

        /**
         * Called on the auto-verification path (SMS auto-retrieved on the same device).
         * [credential] comes from [LoginViewModel.pendingCredential].
         */
        fun verifyWithCredential(credential: PhoneAuthCredential) {
            viewModelScope.launch {
                _uiState.value = OtpUiState.Loading
                signInWithCredential(credential)
            }
        }

        private suspend fun signInWithCredential(credential: PhoneAuthCredential) {
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                val idToken = result.user?.getIdToken(false)?.await()?.token
                if (idToken == null) {
                    _uiState.value = OtpUiState.Error("Could not retrieve Firebase token. Please try again.")
                    return
                }
                when (val r = authRepository.verifyFirebaseToken(idToken, mobileNumber)) {
                    is Result.Success -> _uiState.value = OtpUiState.Verified(r.data.isNewUser)
                    is Result.Error -> _uiState.value = OtpUiState.Error(r.message)
                }
            } catch (e: Exception) {
                _uiState.value = OtpUiState.Error(e.message ?: "Verification failed. Please try again.")
            }
        }

        /**
         * Resend: navigate back to LoginScreen to trigger a fresh Firebase send.
         * Signals [OtpUiState.Idle] so [OTPScreen] can call [onBack].
         */
        fun resendOtp() {
            _uiState.value = OtpUiState.Resending
            // OTPScreen observes Resending → calls onBack() to go back to LoginScreen
            // LoginScreen will then allow the user to tap "Send OTP" again
            _uiState.value = OtpUiState.Idle
        }

        fun resetError() {
            _uiState.value = OtpUiState.Idle
        }
    }
