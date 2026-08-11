package com.fypnetwork.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSucceeded: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(value: String) { _uiState.value = _uiState.value.copy(firstName = value, errorMessage = null) }
    fun onLastNameChange(value: String) { _uiState.value = _uiState.value.copy(lastName = value, errorMessage = null) }
    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, errorMessage = null) }

    fun register() {
        val state = _uiState.value
        if (state.firstName.isBlank() || state.lastName.isBlank() || state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Fill in all fields")
            return
        }
        if (state.password.length < 8) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 8 characters")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                authRepository.register(state.email.trim(), state.password, state.firstName.trim(), state.lastName.trim())
                _uiState.value = _uiState.value.copy(isLoading = false, registerSucceeded = true)
            } catch (e: HttpException) {
                val message = if (e.code() == 409) "An account with this email already exists" else "Something went wrong (${e.code()})"
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Couldn't reach the server. Is the backend running?",
                )
            }
        }
    }
}
