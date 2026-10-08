package co.edu.unicauca.urbanguard.ui.features.visitors

import co.edu.unicauca.urbanguard.domain.model.VisitorAuthorization

/**
 * Estado de la pantalla de recompartir: el código recuperado de una
 * invitación activa o programada, listo para mostrar y compartir.
 */
data class InvitationPassUiState(
    val isLoading: Boolean = false,
    val pass: VisitorAuthorization? = null,
    val errorMessage: String? = null
)
