package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.status
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Nombre del argumento de navegación con el id de la ronda a consultar. */
const val ARG_ROUND_SUMMARY_ID = "roundId"

/**
 * Resumen de una ronda terminada (HU finalización NFC).
 *
 * Dos orígenes sin lógica de escritura:
 * - Sin argumento: la última ronda cerrada en la sesión (recién finalizada),
 *   y se muestra el diálogo de confirmación de cierre.
 * - Con `roundId`: consulta la ejecución puntual al backend (estado real,
 *   solo lectura) para volver a revisar una ronda ya terminada.
 *
 * Los nombres salen del snapshot de puntos de la propia ronda.
 */
@HiltViewModel
class RoundSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PatrolRoundRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val roundIdArg: String? = savedStateHandle.get<String>(ARG_ROUND_SUMMARY_ID)
        ?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(RoundSummaryUiState(isLoading = roundIdArg != null))
    val uiState: StateFlow<RoundSummaryUiState> = _uiState.asStateFlow()

    init {
        val local = repository.lastFinishedRound()
        if (local != null && roundIdArg == local.id) {
            // Recién finalizada: memoria y backend coinciden, sin recarga.
            _uiState.value = finishedState(local, showConfirmation = true)
        } else if (roundIdArg != null) {
            loadFromBackend(roundIdArg)
        } else if (local != null) {
            _uiState.value = finishedState(local, showConfirmation = true)
        } else {
            _uiState.value = RoundSummaryUiState(
                errorMessage = "No hay una ronda terminada para mostrar."
            )
        }
    }

    /** Cierra el diálogo de confirmación y se queda en el detalle. */
    fun onDismissFinishConfirmation() {
        _uiState.value = _uiState.value.copy(showFinishConfirmation = false)
    }

    private fun loadFromBackend(executionId: String) {        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = RoundSummaryUiState(
                errorMessage = "No hay un conjunto activo."
            )
            return
        }
        viewModelScope.launch {
            repository.getFinishedRound(complexId, executionId).fold(
                onSuccess = { round ->
                    if (round.status() == RoundStatus.IN_PROGRESS) {
                        _uiState.value = RoundSummaryUiState(
                            errorMessage = "Esta ronda aún está en curso."
                        )
                    } else {
                        _uiState.value = finishedState(round, showConfirmation = false)
                    }
                },
                onFailure = { error ->
                    _uiState.value = RoundSummaryUiState(
                        errorMessage = error.message ?: "No se pudo consultar la ronda."
                    )
                }
            )
        }
    }

    private fun finishedState(
        round: PatrolRound,
        showConfirmation: Boolean
    ): RoundSummaryUiState = RoundSummaryUiState(
        round = round,
        pointNames = round.points.associate { it.id to it.name },
        pointOrder = round.points.map { it.id },
        showFinishConfirmation = showConfirmation && round.status() != RoundStatus.IN_PROGRESS
    )
}
