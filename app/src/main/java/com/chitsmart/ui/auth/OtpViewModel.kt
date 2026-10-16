package com.chitsmart.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chitsmart.data.repository.AuthRepository
import com.chitsmart.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        /** Mobile number passed from LoginScreen via the nav argument "mobileNumber". */
        val mobileNumber: String = savedStateHandle.get<String>("mobileNumber") ?: ""

        private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
        val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

        fun verifyOtp(otp: String) {
            viewModelScope.launch {
                _uiState.value = OtpUiState.Loading
                when (val result = authRepository.verifyOtp(mobileNumber, otp)) {
                    is Result.Success -> _uiState.value = OtpUiState.Verified(result.data.isNewUser)
                    is Result.Error -> _uiState.value = OtpUiState.Error(result.message)
                }
            }
        }

        fun resendOtp() {
            viewModelScope.launch {
                _uiState.value = OtpUiState.Resending
                when (val result = authRepository.sendOtp(mobileNumber)) {
                    is Result.Success -> _uiState.value = OtpUiState.Idle
                    is Result.Error -> _uiState.value = OtpUiState.Error(result.message)
                }
            }
        }

        fun resetError() {
            _uiState.value = OtpUiState.Idle
        }
    }
