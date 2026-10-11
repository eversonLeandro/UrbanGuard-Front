package co.edu.unicauca.urbanguard.ui.features.incidents

import co.edu.unicauca.urbanguard.domain.model.IncidentSyncState
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident

/**
 * Estado de la bandeja de novedades del guardia.
 *
 * [pendingCount] alimenta el aviso de "tantas por enviar" cuando hay red.
 */
data class IncidentsUiState(
    val isLoading: Boolean = false,
    val incidents: List<SecurityIncident> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean = incidents.isEmpty() && !isLoading && errorMessage == null
    val pendingCount: Int = incidents.count {
        it.syncState == IncidentSyncState.PENDING
    }
}
