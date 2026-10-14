package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary

/**
 * Estado del resumen de un vigilante (parte del flujo de HU-034). Los agregados
 * los calcula el backend.
 */
data class PatrolGuardSummaryUiState(
    val isLoading: Boolean = false,
    val summary: PatrolGuardSummary? = null,
    val errorMessage: String? = null
)
