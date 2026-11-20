package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.model.status
import co.edu.unicauca.urbanguard.domain.usecase.patrol.RoundActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
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
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundActiveUiState(isLoading = true))
    val uiState: StateFlow<RoundActiveUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<RoundActiveEvent>(Channel.BUFFERED)
    val events: Flow<RoundActiveEvent> = eventChannel.receiveAsFlow()

    private var lastTag: String = ""
    private var lastTagAt: Long = 0L
    private val scanInProgress = AtomicBoolean(false)

    /**
     * La pantalla lo llama en cada ON_RESUME (entrada y vuelta), así la
     * ronda vencida se cierra sola al volver a ella.
     */
    fun onRefresh() {
        load()
    }

    fun onTagScanned(tagId: String) {
        if (_uiState.value.showUnexpectedPointDialog) return
        if (_uiState.value.round?.isFinished() == true) return
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        if (!scanInProgress.compareAndSet(false, true)) return
        val normalized = normalizeNfcUid(tagId)
        if (normalized.isEmpty() || isDuplicateRead(normalized)) {
            scanInProgress.set(false)
            return
        }
        viewModelScope.launch {
            try {
                roundActions.registerScan(complexId, normalized).fold(
                    onSuccess = { (round, match) ->
                        _uiState.value = _uiState.value.copy(
                            round = round,
                            lastScanName = match?.name,
                            lastScanKnown = match != null,
                            lastScanAt = System.currentTimeMillis(),
                            showUnexpectedPointDialog = false,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        val appError = (error as? AppException)?.error
                        _uiState.value = _uiState.value.copy(
                            showUnexpectedPointDialog = appError is AppError.UnexpectedPatrolPoint,
                            errorMessage = if (appError is AppError.UnexpectedPatrolPoint) null
                                else appError?.message ?: "No se pudo registrar la lectura."
                        )
                    }
                )
            } finally {
                scanInProgress.set(false)
            }
        }
    }

    fun onDismissUnexpectedPointDialog() {
        _uiState.value = _uiState.value.copy(showUnexpectedPointDialog = false)
    }

    fun onRequestFinish() {
        val round = _uiState.value.round ?: return
        if (_uiState.value.isFinishing || _uiState.value.showCompletionDialog) return
        if (round.points.isEmpty() || round.donePointIds().size != round.points.size) return
        if (round.status() == RoundStatus.COMPLETED) {
            _uiState.value = _uiState.value.copy(showCompletionDialog = true, errorMessage = null)
            return
        }
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        _uiState.value = _uiState.value.copy(isFinishing = true, errorMessage = null)
        viewModelScope.launch {
            roundActions.finish(complexId).fold(
                onSuccess = { finished ->
                    _uiState.value = _uiState.value.copy(
                        round = finished,
                        points = finished.points,
                        showCompletionDialog = finished.status() == RoundStatus.COMPLETED,
                        errorMessage = if (finished.status() == RoundStatus.COMPLETED) null
                            else "La ronda no se completó. Actualiza el estado e inténtalo de nuevo."
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "No se pudo cerrar la ronda."
                    )
                }
            )
            _uiState.value = _uiState.value.copy(isFinishing = false)
        }
    }

    private fun load() {
        if (_uiState.value.round?.status() == RoundStatus.COMPLETED) return
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
            _uiState.value = RoundActiveUiState(round = round, points = round.points)
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
