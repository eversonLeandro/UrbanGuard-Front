package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onLoginHandled() {
        _uiState.value = _uiState.value.copy(loginSuccess = false, userRole = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            loginUseCase(state.email, state.password).fold(
                onSuccess = { userSession ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        userRole = userSession.role
                    )
                },
onFailure = { error ->
                // Se reinician los flags de sesión para evitar estados "fantasma" si el login falla después de haber estado logueado
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = false,
                    userRole = null,
                    errorMessage = error.message ?: "Ocurrió un error. Intenta de nuevo"
                )
            }
            )
        } //DESCOMENTAR CUENDO ESTE EL BACKEND
        // Login simulado para probar navegación sin backend:
        //_uiState.value = _uiState.value.copy(loginSuccess = true)
    }
}