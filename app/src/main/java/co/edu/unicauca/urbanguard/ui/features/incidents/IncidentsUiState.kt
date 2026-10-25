package co.edu.unicauca.urbanguard.ui.features.incidents

import co.edu.unicauca.urbanguard.domain.model.SecurityIncident

/**
 * Estado de la bandeja de novedades del guardia.
 *
 * La lista siempre se obtiene del backend.
 */
data class IncidentsUiState(
    val isLoading: Boolean = false,
    val incidents: List<SecurityIncident> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = incidents.isEmpty() && !isLoading && errorMessage == null
}
