package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.isFinished

/**
 * Entrada de patrullaje del guardia: rutas de hoy, ronda en curso si la hay
 * y rondas recientes del conjunto.
 */
data class GuardSeguridadUiState(
    val isLoading: Boolean = false,
    val todayRoutes: List<PatrolRoute> = emptyList(),
    val activeRound: PatrolRound? = null,
    val recentRounds: List<PatrolRound> = emptyList(),
    val errorMessage: String? = null
)

internal fun GuardSeguridadUiState.hasBlockingError(): Boolean =
    errorMessage != null && todayRoutes.isEmpty() && activeRound?.isFinished() != false
