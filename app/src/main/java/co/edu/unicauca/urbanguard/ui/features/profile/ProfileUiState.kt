package co.edu.unicauca.urbanguard.ui.features.profile

import co.edu.unicauca.urbanguard.domain.model.UserRole

data class ProfileUiState(
    val userName: String = "",
    val userRole: String = "",
    val role: UserRole? = null,

    // ── Cambio de contraseña ─────────────────────────────────────────────────

    /** true mientras se espera la respuesta del backend. */
    val isRequestingTicket: Boolean = false,

    /**
     * URL del ticket de Auth0 lista para abrir en un Custom Tab.
     * La UI la consume (abre el Custom Tab) y debe llamar a [clearTicketUrl]
     * inmediatamente para no dejarla en el estado.
     */
    val ticketUrl: String? = null,

    /**
     * Mensaje de error visible al usuario tras un fallo en la solicitud del ticket.
     * null cuando no hay error activo.
     */
    val passwordChangeError: String? = null,

    /**
     * true cuando el backend devuelve REAUTH_REQUIRED (auth_time > 5 min).
     * La UI debe lanzar un login interactivo con prompt=login y, al completarse,
     * reintentar la solicitud del ticket automáticamente.
     */
    val needsReauth: Boolean = false,
)
