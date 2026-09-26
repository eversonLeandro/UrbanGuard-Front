package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.model.LinkRequestState

data class LinkRequestDetailUiState(
    // Carga inicial del detalle
    val isLoading: Boolean = true,
    val request: LinkRequest? = null,
    // Acción de aprobar/rechazar en curso (muestra spinner)
    val isLoadingAction: Boolean = false,
    // Diálogos de confirmación
    val showApproveDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Resultado de la acción (APROBADA/RECHAZADA) cuando el review finaliza,
    // para disparar el auto-retorno a la lista de la que se llegó.
    val reviewResult: String? = null
) {
    // Estado tipado de la solicitud. Se resuelve con LinkRequestState para
    // tolerar los valores del backend (PENDING/APPROVED/REJECTED) y los
    // heredados en español, igual que el badge de la pantalla de detalle.
    val estado: LinkRequestState
        get() = LinkRequestState.fromString(request?.estado)

    // Solo una solicitud pendiente se puede revisar. Al aprobar o rechazar
    // deja de estar pendiente, por lo que la decisión ya no es reversible.
    val isPending: Boolean
        get() = request != null && estado == LinkRequestState.PENDIENTE

    val isActionEnabled: Boolean
        get() = isPending && !isLoadingAction
}