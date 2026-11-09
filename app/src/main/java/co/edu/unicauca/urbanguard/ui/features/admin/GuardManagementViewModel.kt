package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.guards.GetGuardMembershipsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.guards.SetGuardMembershipActiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuardManagementViewModel @Inject constructor(
    private val getGuardMemberships: GetGuardMembershipsUseCase,
    private val setGuardActive: SetGuardMembershipActiveUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GuardManagementUiState(isAdmin = sessionManager.currentRole() == UserRole.ADMIN)
    )
    val uiState: StateFlow<GuardManagementUiState> = _uiState.asStateFlow()

    init {
        onRefresh()
    }

    fun onRefresh() {
        if (sessionManager.currentRole() != UserRole.ADMIN) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Solo un administrador puede gestionar los vigilantes."
            )
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No hay un conjunto activo seleccionado."
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getGuardMemberships(complexId, _uiState.value.statusFilter.apiValue).fold(
                onSuccess = { guards ->
                    _uiState.value = _uiState.value.copy(isLoading = false, guards = guards)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }

    fun onStatusFilterChange(filter: GuardStatusFilter) {
        if (_uiState.value.statusFilter != filter) {
            _uiState.value = _uiState.value.copy(statusFilter = filter)
            onRefresh()
        }
    }

    fun onChangeStatus(guard: GuardMembership) {
        if (_uiState.value.changingMembershipId != null) return
        if (sessionManager.currentRole() != UserRole.ADMIN) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Solo un administrador puede gestionar los vigilantes."
            )
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No hay un conjunto activo seleccionado."
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            changingMembershipId = guard.membershipId,
            successMessage = null,
            errorMessage = null
        )
        viewModelScope.launch {
            setGuardActive(complexId, guard.membershipId, !guard.active).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        changingMembershipId = null,
                        successMessage = if (guard.active) {
                            "Vigilante inactivado en este conjunto."
                        } else {
                            "Vigilante reactivado en este conjunto."
                        }
                    )
                    onRefresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        changingMembershipId = null,
                        errorMessage = AppError.from(error).message
                    )
                }
            )
        }
    }

    fun onDismissFeedback() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
