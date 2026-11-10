package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.residential.GetCensusUseCase
import co.edu.unicauca.urbanguard.domain.usecase.packages.RegisterPackageUseCase
import co.edu.unicauca.urbanguard.domain.model.PackageRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterPackageViewModel @Inject constructor(
    private val registerPackage: RegisterPackageUseCase,
    private val getCensus: GetCensusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterPackageUiState())
    val uiState: StateFlow<RegisterPackageUiState> = _uiState.asStateFlow()

    init {
        loadResidents()
    }

    fun updateResidentSearch(value: String) = _uiState.update {
        it.copy(
            residentSearch = value,
            selectedResident = null,
            validationError = null
        )
    }

    fun selectResident(resident: PackageRecipientOption) = _uiState.update {
        it.copy(
            residentSearch = resident.displayLabel,
            selectedResident = resident,
            validationError = null,
            errorMessage = null
        )
    }

    fun updateSender(value: String) = _uiState.update { it.copy(sender = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updatePhoto(photoPath: String?) = _uiState.update { it.copy(photoPath = photoPath) }

    fun loadResidents() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Seleccione un conjunto activo") }
            return
        }
        _uiState.update { it.copy(isLoadingResidents = true, errorMessage = null) }
        viewModelScope.launch {
            getCensus(complexId, "activos").fold(
                onSuccess = { units ->
                    val options = units.flatMap { unit ->
                        unit.residents.filter { it.active }.map { resident ->
                            val keyId = resident.userId ?: resident.ownerTenantId ?: resident.membershipId
                                ?: "${unit.id}:${resident.fullName}"
                            PackageRecipientOption(
                                key = "${unit.id}:$keyId",
                                residentId = resident.userId,
                                residentName = resident.fullName,
                                unitId = unit.id,
                                unitLabel = unit.identifier
                            )
                        }
                    }.distinctBy(PackageRecipientOption::key)
                    _uiState.update {
                        it.copy(
                            residents = options,
                            isLoadingResidents = false,
                            errorMessage = if (options.isEmpty()) {
                                "No hay residentes activos registrados en este conjunto"
                            } else {
                                null
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoadingResidents = false, errorMessage = error.message)
                    }
                }
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val resident = state.selectedResident
        val description = state.description.trim()
        if (resident == null) {
            _uiState.value = state.copy(validationError = "Selecciona un residente activo de la lista")
            return
        }
        if (description.length > 500) {
            _uiState.value = state.copy(validationError = "La descripción no puede superar 500 caracteres")
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = "Seleccione un conjunto activo")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val registration = PackageRegistration(
                residentialUnitId = resident.unitId,
                residentialUnitLabel = resident.unitLabel,
                residentId = resident.residentId,
                recipientName = resident.residentName,
                sender = state.sender.ifBlank { null },
                description = description.ifBlank { null },
                photoPath = state.photoPath
            )
            registerPackage(complexId = complexId, registration = registration).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSaving = false, saved = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = it.message) }
            )
        }
    }
}
