package co.edu.unicauca.urbanguard.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.repository.ReauthLoginProvider
import co.edu.unicauca.urbanguard.domain.usecase.users.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val reauthLoginProvider: ReauthLoginProvider,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.session.collect { session ->
                _uiState.update {
                    it.copy(
                        userName = session?.name ?: "",
                        userRole = when (session?.role) {
                            UserRole.ADMIN -> "ADMIN"
                            UserRole.GUARD -> "GUARDIA"
                            UserRole.RESIDENT -> "RESIDENT"
                            UserRole.USER -> "USER"
                            UserRole.SUPERADMIN -> "SUPERADMIN"
                            null -> ""
                        },
                        role = session?.role
                    )
                }
            }
        }
    }

    fun onLogout() {
        sessionManager.clearSession()
    }

    /**
     * Solicita al backend un ticket de cambio de contraseña.
     *
     * Flujo normal:
     *  1. Muestra loading.
     *  2. Llama al backend → recibe la URL del ticket.
     *  3. Escribe [ticketUrl]; la UI lo consume abriendo el Custom Tab.
     *
     * Flujo REAUTH_REQUIRED:
     *  1. El backend responde 401 REAUTH_REQUIRED (auth_time > 5 min).
     *  2. Se activa [needsReauth]; la UI muestra el diálogo de reautenticación.
     *  3. El usuario proporciona su contraseña via [onReauthSubmit].
     *  4. Tras re-login exitoso se reintenta automáticamente.
     */
    fun requestPasswordChange() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingTicket = true, passwordChangeError = null) }
            val result = changePasswordUseCase()
            result.fold(
                onSuccess = { url ->
                    _uiState.update {
                        it.copy(isRequestingTicket = false, ticketUrl = url)
                    }
                },
                onFailure = { throwable ->
                    val error = (throwable as? AppException)?.error
                        ?: AppError.Unexpected(AppError.Unexpected.MESSAGE)
                    when (error) {
                        is AppError.ReauthRequired -> _uiState.update {
                            it.copy(isRequestingTicket = false, needsReauth = true)
                        }
                        else -> _uiState.update {
                            it.copy(isRequestingTicket = false, passwordChangeError = error.message)
                        }
                    }
                }
            )
        }
    }

    /**
     * Llamado desde la UI cuando el usuario confirma su email y contraseña en el diálogo
     * de reautenticación. Realiza login interactivo para renovar [auth_time] y reintenta
     * automáticamente la solicitud del ticket.
     */
    @Suppress("TooGenericExceptionCaught")
    fun onReauthSubmit(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingTicket = true, needsReauth = false, passwordChangeError = null) }
            try {
                val newSession = reauthLoginProvider.login(email, password)
                sessionManager.saveSession(newSession)
                // Reintentar la solicitud del ticket con el token renovado
                val result = changePasswordUseCase()
                result.fold(
                    onSuccess = { url ->
                        _uiState.update { it.copy(isRequestingTicket = false, ticketUrl = url) }
                    },
                    onFailure = { throwable ->
                        val error = (throwable as? AppException)?.error
                            ?: AppError.Unexpected(AppError.Unexpected.MESSAGE)
                        _uiState.update {
                            it.copy(isRequestingTicket = false, passwordChangeError = error.message)
                        }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRequestingTicket = false,
                        passwordChangeError = (e as? AppException)?.error?.message
                            ?: "Contraseña incorrecta. Inténtalo de nuevo."
                    )
                }
            }
        }
    }

    /** Llamado por la UI una vez que consume [ticketUrl] abriendo el Custom Tab. */
    fun onTicketConsumed() {
        _uiState.update { it.copy(ticketUrl = null) }
    }

    /** Descarta el error visible al usuario. */
    fun onPasswordChangeErrorDismissed() {
        _uiState.update { it.copy(passwordChangeError = null) }
    }

    /** Cancela el diálogo de reautenticación sin reintentar. */
    fun onReauthDismissed() {
        _uiState.update { it.copy(needsReauth = false) }
    }
}
