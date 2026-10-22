package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Entrega la última ronda cerrada para el resumen.
 *
 * Viaja por memoria del repositorio (igual que el pase generado), así la
 * pantalla no necesita argumentos de navegación. Los nombres salen del
 * snapshot de puntos de la propia ronda.
 */
@HiltViewModel
class RoundSummaryViewModel @Inject constructor(
    private val repository: PatrolRoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundSummaryUiState())
    val uiState: StateFlow<RoundSummaryUiState> = _uiState.asStateFlow()

    init {
        val round = repository.lastFinishedRound()
        _uiState.value = if (round == null) {
            RoundSummaryUiState(
                errorMessage = "No hay una ronda terminada para mostrar."
            )
        } else {
            RoundSummaryUiState(
                round = round,
                pointNames = round.points.associate { it.id to it.name },
                pointOrder = round.points.map { it.id }
            )
        }
    }
}
