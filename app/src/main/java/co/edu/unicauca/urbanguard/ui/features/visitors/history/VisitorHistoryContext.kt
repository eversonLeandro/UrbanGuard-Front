package co.edu.unicauca.urbanguard.ui.features.visitors.history

import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession

internal data class VisitorHistoryContext(
    val userId: String,
    private val token: String,
    val role: UserRole,
    val complexId: String?,
    val canAccess: Boolean
) {
    // Session identity is compared to invalidate requests. Never print the token.
    override fun toString(): String = "VisitorHistoryContext(role=$role)"
}

internal fun UserSession.historyContext(): VisitorHistoryContext {
    val id = conjuntoActivoId?.takeIf(String::isNotBlank)
    val association = conjuntosAsociados.firstOrNull { it.conjuntoId == id }
    val canAccess = role == UserRole.ADMIN && (association == null || association.roleEnConjunto == UserRole.ADMIN)
    return VisitorHistoryContext(userId, token, role, id, canAccess)
}
