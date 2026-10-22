package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint

/** Estado de la lista de checkpoints NFC del conjunto activo. */
data class PatrolPointsUiState(
    val isLoading: Boolean = false,
    val points: List<PatrolControlPoint> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = points.isEmpty() && !isLoading && errorMessage == null
    val activeCount: Int = points.count { it.isActive }
}
