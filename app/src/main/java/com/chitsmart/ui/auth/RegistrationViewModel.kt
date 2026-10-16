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

// ── UI state ──────────────────────────────────────────────────────────────────

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()

    object Loading : RegistrationUiState()

    object Success : RegistrationUiState()

    data class Error(val message: String) : RegistrationUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class RegistrationViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
        val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

        fun register(
            fullName: String,
            email: String?,
            dob: String?,
            gender: String?,
        ) {
            viewModelScope.launch {
                _uiState.value = RegistrationUiState.Loading
                when (val result = authRepository.register(fullName, email, dob, gender)) {
                    is Result.Success -> _uiState.value = RegistrationUiState.Success
                    is Result.Error -> _uiState.value = RegistrationUiState.Error(result.message)
                }
            }
        }

        fun resetError() {
            _uiState.value = RegistrationUiState.Idle
        }
    }
