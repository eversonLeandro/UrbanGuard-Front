package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.packages.RegisterPackageUseCase
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
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterPackageUiState())
    val uiState: StateFlow<RegisterPackageUiState> = _uiState.asStateFlow()

    fun updateRecipient(value: String) = _uiState.update { it.copy(recipientName = value, validationError = null) }
    fun updateUnit(value: String) = _uiState.update { it.copy(residentialUnitLabel = value) }
    fun updateSender(value: String) = _uiState.update { it.copy(sender = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value, validationError = null) }
    fun updateObservations(value: String) = _uiState.update { it.copy(observations = value) }
    fun updatePhoto(photoPath: String?) = _uiState.update { it.copy(photoPath = photoPath) }

    fun save() {
        val state = _uiState.value
        val recipient = state.recipientName.trim()
        val description = state.description.trim()
        if (recipient.isBlank()) {
            _uiState.value = state.copy(validationError = "Ingresa el nombre del destinatario")
            return
        }
        if (recipient.length > 150) {
            _uiState.value = state.copy(validationError = "El destinatario no puede superar 150 caracteres")
            return
        }
        if (description.isBlank()) {
            _uiState.value = state.copy(validationError = "Ingresa una descripción del paquete")
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = "Seleccione un conjunto activo")
            return
        }
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            registerPackage(
                complexId = complexId,
                residentialUnitId = null,
                residentialUnitLabel = state.residentialUnitLabel.ifBlank { null },
                residentId = null,
                recipientName = recipient,
                sender = state.sender.ifBlank { null },
                description = description,
                observations = state.observations.ifBlank { null },
                photoPath = state.photoPath
            ).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSaving = false, saved = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = it.message) }
            )
        }
    }
}
