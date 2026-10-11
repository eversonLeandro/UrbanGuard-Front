package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound

/**
 * Estado de la ronda en curso: progreso, puntos en orden, última lectura y
 * confirmación de cierre.
 */
data class RoundActiveUiState(
    val isLoading: Boolean = false,
    val round: PatrolRound? = null,
    val points: List<PatrolControlPoint> = emptyList(),
    val lastScanName: String? = null,
    val lastScanKnown: Boolean = true,
    val lastScanAt: Long = 0L,
    val showFinishConfirm: Boolean = false,
    val errorMessage: String? = null
)
