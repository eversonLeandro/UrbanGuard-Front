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

    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        // ── LLAMADA REAL AL BACKEND ──────────────────────────────────────
        // TODO: descomentar cuando el backend esté listo y comentar
        // el bloque de simulación de abajo.
        //
           /*viewModelScope.launch {
               loginUseCase(state.email, state.password).fold(
                   onSuccess = { userSession ->
                       _uiState.value = _uiState.value.copy(
                           isLoading = false,
                           loginSuccess = true,
                           userRole = userSession.role
                       )
                   },
                   onFailure = { error ->
                       _uiState.value = _uiState.value.copy(
                           isLoading = false,
                           loginSuccess = false,
                           userRole = null,
                           errorMessage = error.message ?: "Ocurrió un error. Intenta de nuevo"
                       )
                   }
               )
         }*/
        // ────────────────────────────────────────────────────────────────

        // ── SIMULACIÓN SIN BACKEND ───────────────────────────────────────
        // Descomenta UNO solo de los bloques de abajo según lo que quieras probar.
        // Vuelve a comentarlo cuando cambies de caso.

        // CASO 1 — Residente SIN conjunto → debe ir a LinkByCodeScreen
       /* viewModelScope.launch {
            sessionManager.saveSession(
                UserSession(
                    token = "fake-token",
                    userId = "fake-id",
                    name = "Residente Prueba",
                    role = UserRole.RESIDENT,
                    hasConjunto = false
                )
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                loginSuccess = true,
                userRole = UserRole.RESIDENT
            )
        }*/

        // CASO 2 — Residente CON conjunto → debe ir a HOME
          /*viewModelScope.launch {
               sessionManager.saveSession(
                   UserSession(
                       token = "fake-token",
                       userId = "fake-id",
                       name = "Residente Prueba",
                       role = UserRole.RESIDENT,
                       hasConjunto = true
                   )
               )
               _uiState.value = _uiState.value.copy(
                   isLoading = false,
                   loginSuccess = true,
                  userRole = UserRole.RESIDENT
               )
           }*/

        // CASO 3 — Admin SIN conjunto → debe ir a ACTIVATE_CONJUNTO
        /* viewModelScope.launch {
               sessionManager.saveSession(
                   UserSession(
                       token = "fake-token",
                       userId = "fake-id",
                       name = "Admin Prueba",
                       role = UserRole.ADMIN,
                       hasConjunto = false
                   )
               )
               _uiState.value = _uiState.value.copy(
                   isLoading = false,
                   loginSuccess = true,
                   userRole = UserRole.ADMIN
               )
           }*/

        // CASO 4 — Admin CON conjunto → debe ir a HOME
           viewModelScope.launch {
               sessionManager.saveSession(
                   UserSession(
                       token = "fake-token",
                       userId = "fake-id",
                       name = "Admin Prueba",
                       role = UserRole.ADMIN,
                       hasConjunto = true
                   )
               )
               _uiState.value = _uiState.value.copy(
                   isLoading = false,
                   loginSuccess = true,
                   userRole = UserRole.ADMIN
               )
           }
        // ────────────────────────────────────────────────────────────────
    }
}