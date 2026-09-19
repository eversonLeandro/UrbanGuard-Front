package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager
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
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onLogout() {
        sessionManager.clearSession()
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        // ── LLAMADA REAL AL BACKEND ──────────────────────────────────────
        // TODO: descomentar cuando el backend esté listo y comentar
        // el bloque de simulación de abajo.
        //
           viewModelScope.launch {
               loginUseCase(state.email, state.password).fold(
                    onSuccess = { userSession ->
                        android.util.Log.d("LoginViewModel", "=== [LOGIN SUCCESS] ===")
                        android.util.Log.d("LoginViewModel", "  email: ${state.email}")
                        android.util.Log.d("LoginViewModel", "  userId: ${userSession.userId}")
                        android.util.Log.d("LoginViewModel", "  name: ${userSession.name}")
                        android.util.Log.d("LoginViewModel", "  role: ${userSession.role}")
                        android.util.Log.d("LoginViewModel", "========================")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = true,
                            userRole = userSession.role
                        )
                    },
                    onFailure = { error ->
                        android.util.Log.e("LoginViewModel", "=== [LOGIN FAILED] === error: ${error.message}", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = false,
                            userRole = null,
                            errorMessage = error.message ?: "Ocurrió un error. Intenta de nuevo"
                        )
                    }
                )
          }
        // ────────────────────────────────────────────────────────────────

        // ── SIMULACIÓN SIN BACKEND ───────────────────────────────────────
        // CASO 5 — Usuario nuevo (USER) con 2 conjuntos → CONJUNTO_SELECTION
//        viewModelScope.launch {
//            sessionManager.saveSession(
//                UserSession(
//                    token = "fake-token",
//                    userId = "fake-id",
//                    name = "Usuario Prueba",
//                    role = UserRole.USER
//                )
//            )
//            _uiState.value = _uiState.value.copy(
//                isLoading = false,
//                loginSuccess = true,
//                userRole = UserRole.USER
//            )
//        }
        // ────────────────────────────────────────────────────────────────
    }
}