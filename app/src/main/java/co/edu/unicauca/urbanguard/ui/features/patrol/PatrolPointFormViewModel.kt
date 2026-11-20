package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolPointValidation
import co.edu.unicauca.urbanguard.domain.model.isValidNfcUid
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import co.edu.unicauca.urbanguard.domain.model.validatePatrolPoint
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.SavePatrolPointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Eventos de una sola entrega del formulario. */
sealed interface PatrolPointFormEvent {
    /** El punto quedó guardado; la pantalla debe volver al listado. */
    data object Saved : PatrolPointFormEvent
}

@HiltViewModel
class PatrolPointFormViewModel @Inject constructor(
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val savePatrolPointUseCase: SavePatrolPointUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatrolPointFormUiState())
    val uiState: StateFlow<PatrolPointFormUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<PatrolPointFormEvent>(Channel.BUFFERED)
    val events: Flow<PatrolPointFormEvent> = eventChannel.receiveAsFlow()

    private var pointId: String = ""

    fun load(pointId: String?) {
        this.pointId = pointId.orEmpty()
        if (pointId.isNullOrBlank()) return
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        viewModelScope.launch {
            getPatrolPointsUseCase(complexId).fold(
                onSuccess = { points ->
                    points.firstOrNull { it.id == pointId }?.let { applyPoint(it) }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: PatrolConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onFieldChange(
        name: String? = null,
        location: String? = null,
        description: String? = null
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            name = name ?: state.name,
            location = location ?: state.location,
            description = description ?: state.description,
            nameError = if (name != null) null else state.nameError,
            errorMessage = null
        )
    }

    fun onTagIdChange(tagId: String) {
        _uiState.value = _uiState.value.copy(
            nfcTagId = normalizeNfcUid(tagId),
            nfcState = NfcReadState.READ,
            tagError = null,
            errorMessage = null
        )
    }

    /** Acepta solo la primera lectura física mientras se espera un tag. */
    fun onNfcTagRead(tagId: String) {
        val normalized = normalizeNfcUid(tagId)
        _uiState.update { state ->
            if (state.step != 1 || state.nfcState != NfcReadState.WAITING) state
            else state.copy(
                nfcTagId = normalized,
                nfcState = NfcReadState.READ,
                tagError = null,
                errorMessage = null
            )
        }
    }

    fun onStartNfcRead(nfcSupported: Boolean) {
        if (!nfcSupported) {
            _uiState.value = _uiState.value.copy(nfcState = NfcReadState.UNSUPPORTED)
            return
        }
        _uiState.value = _uiState.value.copy(nfcState = NfcReadState.WAITING, tagError = null)
    }

    fun onActiveChange(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = isActive)
    }

    /** Avanza de paso validando solo lo que ese paso pide. */
    fun onNext() {
        val state = _uiState.value
        if (state.step == 0) {
            if (state.name.isBlank()) {
                _uiState.value = state.copy(nameError = PatrolConstants.MSG_NAME_REQUIRED)
                return
            }
            if (state.name.trim().length > PatrolConstants.MAX_NAME_LENGTH) {
                _uiState.value = state.copy(nameError = PatrolConstants.MSG_NAME_TOO_LONG)
                return
            }
            _uiState.value = state.copy(step = 1, nameError = null)
        } else if (state.step == 1) {
            if (state.nfcTagId.isBlank()) {
                _uiState.value = state.copy(tagError = PatrolConstants.MSG_TAG_REQUIRED)
                return
            }
            if (!isValidNfcUid(state.nfcTagId)) {
                _uiState.value = state.copy(tagError = PatrolConstants.MSG_TAG_INVALID)
                return
            }
            _uiState.value = state.copy(step = 2, tagError = null)
        }
    }

    fun onBack() {
        val step = _uiState.value.step
        if (step > 0) _uiState.value = _uiState.value.copy(step = step - 1)
    }

    fun onSaveClick() {
        val state = _uiState.value
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = PatrolConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        when (validatePatrolPoint(state.name, state.nfcTagId)) {
            PatrolPointValidation.NAME_REQUIRED ->
                _uiState.value = state.copy(nameError = PatrolConstants.MSG_NAME_REQUIRED)
            PatrolPointValidation.NAME_TOO_LONG ->
                _uiState.value = state.copy(nameError = PatrolConstants.MSG_NAME_TOO_LONG)
            PatrolPointValidation.TAG_REQUIRED ->
                _uiState.value = state.copy(tagError = PatrolConstants.MSG_TAG_REQUIRED)
            PatrolPointValidation.TAG_INVALID ->
                _uiState.value = state.copy(tagError = PatrolConstants.MSG_TAG_INVALID)
            PatrolPointValidation.VALID -> {
                _uiState.value = state.copy(isSaving = true, errorMessage = null)
                viewModelScope.launch {
                    savePatrolPointUseCase(
                        PatrolControlPoint(
                            id = pointId,
                            complexId = complexId,
                            name = state.name.trim(),
                            location = state.location.trim(),
                            description = state.description.trim(),
                            nfcTagId = normalizeNfcUid(state.nfcTagId),
                            isActive = state.isActive
                        )
                    ).fold(
                        onSuccess = { eventChannel.send(PatrolPointFormEvent.Saved) },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                errorMessage = error.message
                                    ?: PatrolConstants.MSG_SAVE_ERROR
                            )
                        }
                    )
                }
            }
        }
    }

    private fun applyPoint(point: PatrolControlPoint) {
        _uiState.value = PatrolPointFormUiState(
            name = point.name,
            location = point.location,
            description = point.description,
            nfcTagId = point.nfcTagId,
            isActive = point.isActive,
            nfcState = NfcReadState.READ
        )
    }
}
