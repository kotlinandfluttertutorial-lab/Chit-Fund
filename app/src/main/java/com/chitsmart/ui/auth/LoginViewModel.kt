package com.chitsmart.ui.auth

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

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        fun sendOtp(mobileNumber: String) {
            viewModelScope.launch {
                _uiState.value = LoginUiState.Loading
                when (val result = authRepository.sendOtp(mobileNumber)) {
                    is Result.Success -> _uiState.value = LoginUiState.OtpSent
                    is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }

        fun resetState() {
            _uiState.value = LoginUiState.Idle
        }
    }
