package co.edu.unicauca.urbanguard.ui.features.guard

import co.edu.unicauca.urbanguard.domain.model.PatrolRound

/**
 * Estado del resumen: la última ronda cerrada en la sesión y los nombres de
 * sus puntos para describir cada lectura.
 */
data class RoundSummaryUiState(
    val round: PatrolRound? = null,
    val pointNames: Map<String, String> = emptyMap(),
    /** Ids de la ruta en orden, para mostrar también los no marcados. */
    val pointOrder: List<String> = emptyList(),
    val errorMessage: String? = null
)
