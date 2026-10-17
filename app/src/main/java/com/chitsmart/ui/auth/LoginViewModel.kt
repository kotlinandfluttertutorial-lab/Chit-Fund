package com.chitsmart.ui.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class LoginUiState {
    object Idle : LoginUiState()

    object Loading : LoginUiState()

    /** Firebase sent the SMS; [verificationId] is needed to create the credential. */
    data class OtpSent(val verificationId: String) : LoginUiState()

    /** SMS was auto-retrieved on the same device — no manual code entry needed. */
    object AutoVerified : LoginUiState()

    data class Error(val message: String) : LoginUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        /**
         * Shared state consumed by [OtpViewModel].
         * Set when Firebase auto-retrieves the SMS on the same device.
         */
        val pendingCredential = MutableStateFlow<PhoneAuthCredential?>(null)

        /** Stored so [OtpViewModel] can trigger a resend without navigating back. */
        val resendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)

        /**
         * Trigger Firebase Phone Auth. [mobileNumber] must include country code, e.g. "+919876543210".
         * The [activity] reference is required by Firebase for reCAPTCHA and SMS auto-retrieval;
         * it is NOT stored in the ViewModel — callers pass it on every invocation.
         */
        fun startFirebasePhoneAuth(
            mobileNumber: String,
            activity: Activity,
        ) {
            _uiState.value = LoginUiState.Loading

            val callbacks =
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // SMS auto-retrieved — signal OTP screen can skip manual entry
                        pendingCredential.value = credential
                        _uiState.value = LoginUiState.AutoVerified
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        _uiState.value =
                            LoginUiState.Error(
                                e.message ?: "Phone verification failed",
                            )
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        resendToken.value = token
                        _uiState.value = LoginUiState.OtpSent(verificationId)
                    }
                }

            val options =
                PhoneAuthOptions
                    .newBuilder(firebaseAuth)
                    .setPhoneNumber(mobileNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        fun resetState() {
            _uiState.value = LoginUiState.Idle
        }
    }
