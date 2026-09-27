package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.usecase.auth.SignUpUseCase
import co.edu.unicauca.urbanguard.domain.validation.InputValidators
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
        _uiState.value = _uiState.value.copy(name = value, nameError = null, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        // Se revalida en caliente: al elegir una contraseña válida el error desaparece
        // sin tener que volver a pulsar el botón.
        val policyError = InputValidators.passwordPolicyMessage(value)
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = policyError,
            confirmPasswordError = confirmPasswordErrorFor(value),
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = confirmPasswordErrorFor(value),
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun onRegisterClick() {
        val state = _uiState.value
        if (state.isLoading) return

        // Se validan todos los campos a la vez: el usuario ve de una vez qué debe
        // corregir, en lugar de descubrir los problemas de uno en uno.
        val nameError = if (state.name.isBlank()) "Ingresa tu nombre completo" else null
        val emailError = if (!InputValidators.isValidEmail(state.email)) {
            "Ingresa un correo electrónico válido"
        } else {
            null
        }
        val passwordError = InputValidators.passwordPolicyMessage(state.password)
        val confirmPasswordError = confirmPasswordErrorFor(state.confirmPassword)

        val hasErrors = nameError != null || emailError != null ||
                passwordError != null || confirmPasswordError != null

        if (hasErrors) {
            _uiState.value = state.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                errorMessage = null
            )
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            signUpUseCase(state.email.trim(), state.password, state.name.trim()).fold(
                onSuccess = { userSession ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registerSuccess = true,
                        userRole = userSession.role
                    )
                },
                onFailure = { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = messageFor(throwable)
                    )
                }
            )
        }
    }

    /**
     * El error de confirmación depende del campo de contraseña, así que se recalcula
     * cuando cambia cualquiera de los dos.
     */
    private fun confirmPasswordErrorFor(confirmPassword: String): String? = when {
        confirmPassword.isBlank() -> "Confirma tu contraseña"
        confirmPassword != _uiState.value.password -> "Las contraseñas no coinciden"
        else -> null
    }

    private fun messageFor(throwable: Throwable): String =
        AppError.from(throwable).message
}
