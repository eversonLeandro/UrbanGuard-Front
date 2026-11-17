package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.residential.GetCensusUseCase
import co.edu.unicauca.urbanguard.domain.validation.UnitIdentifierValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CensusViewModel @Inject constructor(
    private val getCensusUseCase: GetCensusUseCase,
    private val unitUseCases: CensusUnitUseCases,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CensusUiState(isAdmin = sessionManager.currentRole() == UserRole.ADMIN)
    )
    val uiState: StateFlow<CensusUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var hasObservedScreenResume = false

    init {
        onRefresh()
    }

    fun onRefresh() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            loadJob?.cancel()
            _uiState.value = CensusUiState(
                error = AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE)
            )
            return
        }

        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadJob = viewModelScope.launch {
            getCensusUseCase(complexId).fold(
                onSuccess = { units ->
                    val selectedId = _uiState.value.selectedUnitId
                        ?.takeIf { selectedId -> units.any { it.id == selectedId } }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        units = units,
                        error = null,
                        selectedUnitId = selectedId
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = AppError.from(error)
                    )
                }
            )
        }
    }

    fun onScreenResumed() {
        if (hasObservedScreenResume) {
            onRefresh()
        } else {
            hasObservedScreenResume = true
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterChange(filter: CensusUnitFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun onChangeResidentStatus(unitId: String, resident: ResidentialResident) {
        val state = _uiState.value
        if (state.isChangingResidentStatus) return
        if (!state.isAdmin || sessionManager.currentRole() != UserRole.ADMIN) {
            _uiState.value = state.copy(
                residentStatusError = AppError.Forbidden(NO_ADMIN_RESIDENT_MESSAGE)
            )
            return
        }
        val residentId = resident.ownerTenantId ?: resident.membershipId
        if (residentId.isNullOrBlank() || unitId.isBlank()) {
            _uiState.value = state.copy(
                residentStatusError = AppError.Validation(INVALID_RESIDENT_MESSAGE)
            )
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(
                residentStatusError = AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE)
            )
            return
        }

        _uiState.value = state.copy(
            isChangingResidentStatus = true,
            changingResidentId = residentId,
            residentStatusSuccess = null,
            residentStatusError = null
        )
        viewModelScope.launch {
            unitUseCases.setOwnerTenantActive(complexId, unitId, residentId, !resident.active).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isChangingResidentStatus = false,
                        changingResidentId = null,
                        residentStatusSuccess = if (resident.active) {
                            "Registro de ${resident.fullName} inactivado"
                        } else {
                            "Registro de ${resident.fullName} reactivado"
                        }
                    )
                    onRefresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isChangingResidentStatus = false,
                        changingResidentId = null,
                        residentStatusError = AppError.from(error)
                    )
                }
            )
        }
    }

    fun onUnitSelectionChange(unitId: String?) {
        if (unitId == null) {
            _uiState.value = _uiState.value.copy(selectedUnitId = null)
        } else if (_uiState.value.units.any { it.id == unitId }) {
            _uiState.value = _uiState.value.copy(selectedUnitId = unitId)
        }
    }

    fun onRegisterUnit() {
        val state = _uiState.value
        if (state.isRegistering) return
        if (sessionManager.currentRole() != UserRole.ADMIN) {
            _uiState.value = state.copy(registrationError = AppError.Forbidden(NO_ADMIN_MESSAGE))
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(
                registrationError = AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE)
            )
            return
        }

        _uiState.value = state.copy(
            isRegistering = true,
            registrationSuccess = null,
            registrationError = null
        )
        viewModelScope.launch {
            unitUseCases.registerUnit(complexId).fold(
                onSuccess = { unit ->
                    _uiState.value = _uiState.value.copy(
                        isRegistering = false,
                        registrationSuccess = "Apartamento ${unit.identifier} agregado"
                    )
                    onRefresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isRegistering = false,
                        registrationError = AppError.from(error)
                    )
                }
            )
        }
    }

    fun onDismissFeedback() {
        _uiState.value = _uiState.value.copy(
            registrationSuccess = null,
            registrationError = null,
            updateUnitSuccess = null,
            residentStatusSuccess = null,
            residentStatusError = null
        )
    }

    fun onEditUnitDialogChange(visible: Boolean) {
        val state = _uiState.value
        if (state.isUpdatingUnit) return
        if (visible && (!state.isAdmin || state.selectedUnit == null)) return
        _uiState.value = state.copy(
            isEditingUnit = visible,
            updateUnitError = null,
            updateUnitSuccess = null
        )
    }

    fun onUpdateUnit(identifier: String) {
        val state = _uiState.value
        if (state.isUpdatingUnit) return
        if (!state.isAdmin || sessionManager.currentRole() != UserRole.ADMIN) {
            _uiState.value = state.copy(
                updateUnitError = AppError.Forbidden(NO_ADMIN_EDIT_MESSAGE)
            )
            return
        }
        val unit = state.selectedUnit
        if (unit == null) {
            _uiState.value = state.copy(
                updateUnitError = AppError.Validation(NO_UNIT_SELECTED_MESSAGE)
            )
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(
                updateUnitError = AppError.Validation(NO_ACTIVE_COMPLEX_MESSAGE)
            )
            return
        }

        val trimmedIdentifier = identifier.trim()
        UnitIdentifierValidator.validate(trimmedIdentifier)?.let { message ->
            _uiState.value = state.copy(updateUnitError = AppError.Validation(message))
            return
        }
        if (trimmedIdentifier == unit.identifier) {
            _uiState.value = state.copy(
                isEditingUnit = false,
                updateUnitError = null
            )
            return
        }

        _uiState.value = state.copy(
            isUpdatingUnit = true,
            updateUnitError = null,
            updateUnitSuccess = null
        )
        viewModelScope.launch {
            unitUseCases.updateUnit(complexId, unit.id, trimmedIdentifier).fold(
                onSuccess = { updatedIdentifier ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingUnit = false,
                        isEditingUnit = false,
                        updateUnitSuccess = "Apartamento $updatedIdentifier actualizado"
                    )
                    onRefresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingUnit = false,
                        updateUnitError = AppError.from(error)
                    )
                }
            )
        }
    }

    private companion object {
        const val NO_ADMIN_EDIT_MESSAGE =
            "No tienes permisos de administrador para editar apartamentos."
        const val NO_UNIT_SELECTED_MESSAGE = "No se seleccionó un apartamento."
        const val NO_ADMIN_MESSAGE = "No tienes permisos de administrador para agregar apartamentos."
        const val NO_ACTIVE_COMPLEX_MESSAGE = "No hay un conjunto activo seleccionado."
        const val NO_ADMIN_RESIDENT_MESSAGE =
            "No tienes permisos de administrador para cambiar el estado del residente."
        const val INVALID_RESIDENT_MESSAGE = "No se pudo identificar el registro del residente."
    }
}
