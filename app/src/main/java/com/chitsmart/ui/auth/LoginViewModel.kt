package com.chitsmart.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    // private val authRepository: AuthRepository  // inject when implemented
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun sendOtp(mobileNumber: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            // TODO: replace stub with authRepository.sendOtp(mobileNumber)
            kotlinx.coroutines.delay(1500)
            _uiState.value = LoginUiState.OtpSent
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
