package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import co.edu.unicauca.urbanguard.domain.usecase.patrol.GetPatrolPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Entrega la última ronda cerrada para el resumen.
 *
 * Viaja por memoria del repositorio (igual que el pase generado), así la
 * pantalla no necesita argumentos de navegación.
 */
@HiltViewModel
class RoundSummaryViewModel @Inject constructor(
    private val repository: PatrolRoundRepository,
    private val getPatrolPointsUseCase: GetPatrolPointsUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundSummaryUiState())
    val uiState: StateFlow<RoundSummaryUiState> = _uiState.asStateFlow()

    init {
        val round = repository.lastFinishedRound()
        if (round == null) {
            _uiState.value = RoundSummaryUiState(
                errorMessage = "No hay una ronda terminada para mostrar."
            )
        } else {
            _uiState.value = RoundSummaryUiState(round = round)
            val complexId = sessionManager.getActiveConjunto()?.conjuntoId
            if (complexId != null) {
                viewModelScope.launch {
                    val points = getPatrolPointsUseCase(complexId).getOrDefault(emptyList())
                    _uiState.value = RoundSummaryUiState(
                        round = round,
                        pointNames = points.associate { it.id to it.name },
                        pointOrder = points.map { it.id }
                    )
                }
            }
        }
    }
}
