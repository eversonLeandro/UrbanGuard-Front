package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint

/**
 * Estado de la lista de puntos de control del conjunto activo.
 *
 * [pendingDelete] retiene el punto con el diálogo de confirmación abierto.
 */
data class PatrolPointsUiState(
    val isLoading: Boolean = false,
    val points: List<PatrolControlPoint> = emptyList(),
    val errorMessage: String? = null,
    val pendingDelete: PatrolControlPoint? = null
) {
    val isEmpty: Boolean = points.isEmpty() && !isLoading && errorMessage == null
    val activeCount: Int = points.count { it.isActive }
}
