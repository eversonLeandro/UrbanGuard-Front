package co.edu.unicauca.urbanguard.domain.model

import java.time.LocalDate

/**
 * Invitación de visitante creada por el residente.
 *
 * A diferencia de [VisitorAuthorization] (pase recién generado con su `code`),
 * el listado del backend no trae el código QR: solo metadatos y el estado
 * funcional. El código se recupera bajo demanda para recompartir.
 */
data class VisitorInvitation(
    val invitationId: String,
    val visitorName: String,
    val guestCount: Int,
    val visitDate: LocalDate,
    val status: VisitorInvitationStatus
)

/**
 * Estado funcional de la invitación, tal como lo deriva el backend.
 *
 * - ACTIVE: vigente hoy, con código recuperable para compartir.
 * - PROGRAMMED: fecha futura, con código recuperable para compartir.
 * - EXPIRED: fecha pasada, ya sin código disponible.
 * - USED: consumida en portería, ya sin código disponible.
 */
enum class VisitorInvitationStatus {
    ACTIVE,
    PROGRAMMED,
    EXPIRED,
    USED;

    /** Las que aún tienen código disponible para volver a compartir. */
    val isShareable: Boolean
        get() = this == ACTIVE || this == PROGRAMMED

    companion object {
        fun fromBackend(raw: String?): VisitorInvitationStatus =
            when (raw?.trim()?.uppercase()) {
                "ACTIVE" -> ACTIVE
                "PROGRAMMED" -> PROGRAMMED
                "EXPIRED" -> EXPIRED
                else -> USED
            }
    }
}

/**
 * Orden del listado del residente: primero las compartibles (activas, luego
 * programadas por fecha), después las cerradas (usadas y expiradas) por fecha
 * descendente.
 */
fun List<VisitorInvitation>.sortedForResident(): List<VisitorInvitation> {
    val rank = mapOf(
        VisitorInvitationStatus.ACTIVE to 0,
        VisitorInvitationStatus.PROGRAMMED to 1,
        VisitorInvitationStatus.USED to 2,
        VisitorInvitationStatus.EXPIRED to 3
    )
    return sortedWith(
        compareBy({ rank[it.status] ?: 4 }, { it.visitDate })
    )
}
