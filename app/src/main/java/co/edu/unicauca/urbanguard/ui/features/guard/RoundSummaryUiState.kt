package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.domain.model.PatrolRound

/**
 * Estado del resumen: la ronda cerrada a mostrar (la última cerrada en la
 * sesión o una consultada por id) y los nombres de sus puntos para describir
 * cada lectura.
 */
data class RoundSummaryUiState(
    val isLoading: Boolean = false,
    val round: PatrolRound? = null,
    val pointNames: Map<String, String> = emptyMap(),
    /** Ids de la ruta en orden, para mostrar también los no marcados. */
    val pointOrder: List<String> = emptyList(),
    /** Diálogo de confirmación solo al llegar de cerrar la ronda. */
    val showFinishConfirmation: Boolean = false,
    val errorMessage: String? = null
)
