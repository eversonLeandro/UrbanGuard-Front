package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.IncidentConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.IncidentValidation
import co.edu.unicauca.urbanguard.domain.model.validateIncident
import co.edu.unicauca.urbanguard.domain.usecase.incidents.ReportIncidentUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/** Eventos de una sola entrega del formulario de novedad. */
sealed interface IncidentFormEvent {
    /** La novedad quedó guardada; ir a la bandeja. */
    data object Saved : IncidentFormEvent
}

@HiltViewModel
class IncidentFormViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentFormUiState())
    val uiState: StateFlow<IncidentFormUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<IncidentFormEvent>(Channel.BUFFERED)
    val events: Flow<IncidentFormEvent> = eventChannel.receiveAsFlow()
    private var idempotencyKey: String = UUID.randomUUID().toString()
    private var activeExecutionId: String? = null

    fun load(preselectedPointId: String?, executionId: String?) {
        activeExecutionId = executionId?.takeIf(String::isNotBlank)
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        viewModelScope.launch {
            getPatrolPointsUseCase(complexId).fold(
                onSuccess = { points ->
                    _uiState.value = _uiState.value.copy(
                        availablePoints = points,
                        pointId = preselectedPointId?.takeIf { id ->
                            points.any { it.id == id }
                        }
                    )
                },
                onFailure = { /* Sin puntos no se bloquea el reporte. */ }
            )
        }
    }

    fun onCategoryChange(category: IncidentCategory) {
        idempotencyKey = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            category = category,
            categoryError = null,
            errorMessage = null
        )
    }

    fun onDescriptionChange(description: String) {
        idempotencyKey = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            description = description,
            descriptionError = null,
            errorMessage = null
        )
    }

    fun onPhotoCaptured(file: File) {
        idempotencyKey = UUID.randomUUID().toString()
        _uiState.value.photoPath?.takeIf { it != file.absolutePath }?.let { File(it).delete() }
        _uiState.value = _uiState.value.copy(photoPath = file.absolutePath)
    }

    fun onCameraError(shown: Boolean) {
        _uiState.value = _uiState.value.copy(cameraError = shown)
    }

    fun onPointChange(pointId: String?) {
        idempotencyKey = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(pointId = pointId)
    }

    fun onSaveClick() {
        val state = _uiState.value
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = IncidentConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        when (validateIncident(state.category, state.description, state.photoPath)) {
            IncidentValidation.CATEGORY_REQUIRED ->
                _uiState.value = state.copy(
                    categoryError = IncidentConstants.MSG_CATEGORY_REQUIRED
                )
            IncidentValidation.DESCRIPTION_REQUIRED ->
                _uiState.value = state.copy(
                    descriptionError = IncidentConstants.MSG_DESCRIPTION_REQUIRED
                )
            IncidentValidation.DESCRIPTION_TOO_SHORT ->
                _uiState.value = state.copy(
                    descriptionError = IncidentConstants.MSG_DESCRIPTION_TOO_SHORT
                )
            IncidentValidation.PHOTO_REQUIRED ->
                _uiState.value = state.copy(
                    errorMessage = IncidentConstants.MSG_PHOTO_REQUIRED
                )
            IncidentValidation.VALID -> save(state, complexId)
        }
    }

    private fun save(state: IncidentFormUiState, complexId: String) {
        val requestKey = idempotencyKey
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            reportIncidentUseCase(
                complexId = complexId,
                category = state.category ?: IncidentCategory.OTHER,
                description = state.description.trim(),
                photoPath = state.photoPath,
                pointId = state.pointId,
                executionId = activeExecutionId,
                idempotencyKey = requestKey
            ).fold(
                onSuccess = {
                    state.photoPath?.let { File(it).delete() }
                    eventChannel.send(IncidentFormEvent.Saved)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: IncidentConstants.MSG_SAVE_ERROR
                    )
                }
            )
        }

    }

    override fun onCleared() {
        _uiState.value.photoPath?.let { File(it).delete() }
        super.onCleared()
    }
}
