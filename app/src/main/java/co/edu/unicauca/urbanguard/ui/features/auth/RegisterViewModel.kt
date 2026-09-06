package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun onRegisterClick() {
        val state = _uiState.value
        if (state.isLoading) return

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Por favor ingresa tu nombre completo")
            return
        }

        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.value = state.copy(errorMessage = "Ingresa un correo electrónico válido")
            return
        }

        if (state.password.length < 8) {
            _uiState.value = state.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres")
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            signUpUseCase(state.email, state.password, state.name).fold(
                onSuccess = { userSession ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registerSuccess = true,
                        userRole = userSession.role
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Ocurrió un error al registrarse. Intenta de nuevo"
                    )
                }
            )
        }
    }
}
