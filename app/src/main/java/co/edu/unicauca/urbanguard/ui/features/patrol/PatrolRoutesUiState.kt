package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoute

/**
 * Estado de la lista de rutas de patrullaje del conjunto activo.
 *
 * [pendingDelete] retiene la ruta con el diálogo de confirmación abierto.
 */
data class PatrolRoutesUiState(
    val isLoading: Boolean = false,
    val routes: List<PatrolRoute> = emptyList(),
    val errorMessage: String? = null,
    val pendingDelete: PatrolRoute? = null
) {
    val isEmpty: Boolean = routes.isEmpty() && !isLoading && errorMessage == null
}
