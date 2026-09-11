package co.edu.unicauca.urbanguard.ui.features.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest

data class LinkRequestDetailUiState(
    // Carga inicial del detalle
    val isLoading: Boolean = true,
    val request: LinkRequest? = null,
    // Acción de aprobar/rechazar en curso (deshabilita botones y muestra spinner)
    val isLoadingAction: Boolean = false,
    // Diálogos de confirmación
    val showApproveDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val errorMessage: String? = null
) {
    // Tras aprobar/rechazar, la solicitud deja de estar pendiente y se bloquean los botones
    val isActionEnabled: Boolean
        get() = !isLoadingAction &&
            request != null &&
            !request.estado.equals("APROBADA", ignoreCase = true) &&
            !request.estado.equals("RECHAZADA", ignoreCase = true)
}