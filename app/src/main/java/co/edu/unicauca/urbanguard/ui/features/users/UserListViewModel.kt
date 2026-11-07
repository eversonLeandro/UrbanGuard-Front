package co.edu.unicauca.urbanguard.ui.features.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.domain.usecase.users.DeactivateUserUseCase
import co.edu.unicauca.urbanguard.domain.usecase.users.ListUsersUseCase
import co.edu.unicauca.urbanguard.domain.usecase.users.ReactivateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Gestión de cuentas para el superadministrador (HU-006).
 *
 * La fila propia queda marcada desde la sesión ([UserSession.userId] = `sub`
 * de Auth0): es la defensa de UX contra la autodesactivación. La guardia
 * definitiva es el backend (409), cuyos mensajes de negocio se muestran tal
 * cual si de todos modos llegara una respuesta de conflicto.
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val listUsers: ListUsersUseCase,
    private val deactivateUser: DeactivateUserUseCase,
    private val reactivateUser: ReactivateUserUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        // `sub` de Auth0 del actor: identifica la fila propia en pantalla.
        _uiState.value = _uiState.value.copy(myUserId = sessionManager.session.value?.userId)
        onSelectFilter(UserListFilter.ACTIVE)
    }

    /** Cambia de filtro y recarga el catálogo correspondiente. */
    fun onSelectFilter(filter: UserListFilter) {
        val current = _uiState.value
        _uiState.value = current.copy(
            filter = filter,
            isLoading = current.users.isEmpty(),
            isRefreshing = current.users.isNotEmpty(),
            errorMessage = null,
            successMessage = null
        )
        viewModelScope.launch {
            listUsers(filter.apiValue).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        users = users,
                        isLoading = false,
                        isRefreshing = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }

    /** Restaura el estado de carga tras un error sin vaciar la lista visible. */
    fun onRetry() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
        selectFilterKeepingState(_uiState.value.filter)
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }

    /** Abre el diálogo de confirmación; el desactivado real ocurre en [onConfirmDeactivate]. */
    fun onRequestDeactivate(user: User) {
        _uiState.value = _uiState.value.copy(pendingDeactivation = user, errorMessage = null)
    }

    fun onDismissDeactivateDialog() {
        _uiState.value = _uiState.value.copy(pendingDeactivation = null)
    }

    fun onConfirmDeactivate() {
        val target = _uiState.value.pendingDeactivation ?: return
        if (_uiState.value.actionInProgressUserId != null) return
        performAction(target.userId, isDeactivation = true)
    }

    fun onReactivate(user: User) {
        if (_uiState.value.actionInProgressUserId != null) return
        performAction(user.userId, isDeactivation = false)
    }

    /** Descarta el mensaje efímero de éxito cuando la snackbar termina. */
    fun onSuccessMessageShown() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /** Descarta el banner de error (p. ej. el 409 de la red de seguridad). */
    fun onErrorMessageShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun performAction(userId: String, isDeactivation: Boolean) {
        _uiState.value = _uiState.value.copy(
            actionInProgressUserId = userId,
            pendingDeactivation = null,
            errorMessage = null
        )
        viewModelScope.launch {
            // Ramas separadas a propósito: Result<Unit> y Result<User> no comparten
            // supertipo útil (Any) y unificarlas con if-else perdería el tipado.
            if (isDeactivation) {
                deactivateUser(userId).fold(
                    onSuccess = {
                        onActionSuccess(isDeactivation = true)
                        // La fila sale de la vista de inmediato (optimista) y la
                        // recarga del filtro confirma con el servidor.
                        refreshCurrentFilter(userId)
                    },
                    onFailure = { onActionFailure(it) }
                )
            } else {
                reactivateUser(userId).fold(
                    onSuccess = { updated ->
                        onActionSuccess(isDeactivation = false)
                        // La lista cargada pertenece al filtro "inactivos": la cuenta
                        // reactivada sale de la vista y la recarga lo confirma.
                        refreshCurrentFilter(updated.userId)
                    },
                    onFailure = { onActionFailure(it) }
                )
            }
        }
    }

    private fun onActionSuccess(isDeactivation: Boolean) {
        _uiState.value = _uiState.value.copy(
            actionInProgressUserId = null,
            successMessage = if (isDeactivation) {
                "Acceso revocado. El historial de la cuenta se conserva."
            } else {
                "Acceso restaurado."
            }
        )
    }

    private fun onActionFailure(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            actionInProgressUserId = null,
            errorMessage = AppError.from(error).message
        )
    }

    /**
     * Saca la fila de la lista cargada tan pronto la acción terminó bien, y la
     * recarga del filtro vigente reemplaza el ajuste local con el estado real.
     */
    private fun refreshCurrentFilter(userId: String) {
        _uiState.value = _uiState.value.copy(
            users = _uiState.value.users.filterNot { it.userId == userId }
        )
        selectFilterKeepingState(_uiState.value.filter)
    }

    private fun selectFilterKeepingState(filter: UserListFilter) {
        viewModelScope.launch {
            listUsers(filter.apiValue).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        users = users,
                        isLoading = false,
                        isRefreshing = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }
}
