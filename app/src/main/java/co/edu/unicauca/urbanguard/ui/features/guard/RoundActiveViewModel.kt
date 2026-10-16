package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.patrol.RoundActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Eventos de una sola entrega de la ronda activa. */
sealed interface RoundActiveEvent {
    /** No hay ronda abierta; volver atrás. */
    data object NoActiveRound : RoundActiveEvent

    /** Ronda cerrada; ir al resumen. */
    data object RoundFinished : RoundActiveEvent
}

@HiltViewModel
class RoundActiveViewModel @Inject constructor(
    private val roundActions: RoundActions,
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundActiveUiState(isLoading = true))
    val uiState: StateFlow<RoundActiveUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<RoundActiveEvent>(Channel.BUFFERED)
    val events: Flow<RoundActiveEvent> = eventChannel.receiveAsFlow()

    private var lastTag: String = ""
    private var lastTagAt: Long = 0L

    /**
     * La pantalla lo llama en cada ON_RESUME (entrada y vuelta), así la
     * ronda vencida se cierra sola al volver a ella.
     */
    fun onRefresh() {
        load()
    }

    fun onTagScanned(tagId: String) {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        val normalized = tagId.trim().uppercase()
        if (normalized.isEmpty() || isDuplicateRead(normalized)) return
        viewModelScope.launch {
            roundActions.registerScan(complexId, normalized).fold(
                onSuccess = { (round, match) ->
                    _uiState.value = _uiState.value.copy(
                        round = round,
                        lastScanName = match?.name,
                        lastScanKnown = match != null,
                        lastScanAt = System.currentTimeMillis(),
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "No se pudo registrar la lectura."
                    )
                }
            )
        }
    }

    fun onRequestFinish() {
        _uiState.value = _uiState.value.copy(showFinishConfirm = true)
    }

    fun onDismissFinish() {
        _uiState.value = _uiState.value.copy(showFinishConfirm = false)
    }

    fun onConfirmFinish() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        _uiState.value = _uiState.value.copy(showFinishConfirm = false)
        viewModelScope.launch {
            roundActions.finish(complexId).fold(
                onSuccess = { eventChannel.send(RoundActiveEvent.RoundFinished) },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "No se pudo cerrar la ronda."
                    )
                }
            )
        }
    }

    private fun load() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            viewModelScope.launch { eventChannel.send(RoundActiveEvent.NoActiveRound) }
            return
        }
        viewModelScope.launch {
            // Si se pasó el límite, se cierra sola como vencida y se va al
            // resumen en vez de seguir escaneando.
            val autoClosed = roundActions.closeOverdue(complexId)
            if (autoClosed != null) {
                eventChannel.send(RoundActiveEvent.RoundFinished)
                return@launch
            }
            val round = roundActions.activeRound(complexId)
            if (round == null) {
                eventChannel.send(RoundActiveEvent.NoActiveRound)
                return@launch
            }
            val points = getPatrolPointsUseCase(complexId).getOrDefault(emptyList())
            _uiState.value = RoundActiveUiState(round = round, points = points)
        }
    }

    /**
     * El lector entrega el mismo tag mientras siga presente: se ignora si es
     * el mismo dentro de la ventana para no duplicar lecturas.
     */
    private fun isDuplicateRead(normalized: String): Boolean {
        val now = System.currentTimeMillis()
        if (normalized == lastTag && now - lastTagAt < DUPLICATE_WINDOW_MS) return true
        lastTag = normalized
        lastTagAt = now
        return false
    }

    private companion object {
        const val DUPLICATE_WINDOW_MS = 3000L
    }
}
