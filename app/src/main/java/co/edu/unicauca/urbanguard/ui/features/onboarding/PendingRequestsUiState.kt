package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.model.LinkRequestState

data class PendingRequestsUiState(
    // Estado de carga inicial de la lista
    val isLoading: Boolean = true,
    val requests: List<LinkRequest> = emptyList(),
    // Filtro activo (chips superiores): Pendientes, Aprobadas o Rechazadas
    val activeFilter: LinkRequestState = LinkRequestState.PENDIENTE,
    val errorMessage: String? = null,
    // Confirmación mostrada al volver del detalle tras aprobar/rechazar
    val message: String? = null
) {
    // Solo las solicitudes del filtro activo
    val filteredRequests: List<LinkRequest>
        get() = requests.filter {
            LinkRequestState.fromString(it.estado) == activeFilter
        }
}