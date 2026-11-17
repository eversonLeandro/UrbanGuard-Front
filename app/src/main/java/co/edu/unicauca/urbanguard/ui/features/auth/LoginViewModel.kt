package co.edu.unicauca.urbanguard.ui.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.push.PushRegistrar
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.usecase.auth.LoginUseCase
import co.edu.unicauca.urbanguard.domain.validation.InputValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val pushRegistrar: PushRegistrar
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value =
            _uiState.value.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun onLoginHandled() {
        _uiState.value = _uiState.value.copy(loginSuccess = false, userRole = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return

        // Se valida el formato antes de llamar a Auth0: un correo mal escrito devuelve
        // un error de autenticación que no explica que el problema es el correo.
        val emailError = if (!InputValidators.isValidEmail(state.email)) {
            "Ingresa un correo electrónico válido"
        } else {
            null
        }
        val passwordError = if (state.password.isBlank()) "Ingresa tu contraseña" else null

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            loginUseCase(state.email.trim(), state.password).fold(
                onSuccess = { userSession ->
                    Log.d(
                        TAG,
                        "Login correcto: userId=${userSession.userId}, role=${userSession.role}"
                    )
                    // Best-effort y silencioso: si ya hay conjunto activo el
                    // token queda registrado; si no, se registra al elegirlo.
                    pushRegistrar.registerActiveConjunto()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        userRole = userSession.role
                    )
                },
                onFailure = { error ->
                    // El repositorio ya devuelve un AppError con el mensaje en español
                    // (credenciales incorrectas, cuenta bloqueada, sin conexión, etc.).
                    val appError = AppError.from(error)
                    Log.w(TAG, "Login fallido: ${appError::class.simpleName}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = false,
                        userRole = null,
                        errorMessage = appError.message
                    )
                }
            )
        }
    }
}
