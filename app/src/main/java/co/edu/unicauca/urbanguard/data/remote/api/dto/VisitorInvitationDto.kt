package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Elemento del listado de invitaciones del residente.
 *
 * GET `api/complexes/{complexId}/visitor-invitations` → 200 OK
 * Body: [{ invitationId, visitorName, guestCount, visitDate, status,
 *          createdAt, usedAt }]
 *
 * `status` es el estado funcional (ACTIVE, PROGRAMMED, EXPIRED, USED).
 * El listado no trae el código QR: se recupera con `.../{invitationId}/code`.
 * `visitDate` llega como texto ISO (yyyy-MM-dd).
 */
data class VisitorInvitationListItemDto(
    val invitationId: String,
    val visitorName: String,
    val guestCount: Int,
    val visitDate: String,
    val status: String? = null,
    val createdAt: String? = null,
    val usedAt: String? = null
)

/**
 * Código recuperado de una invitación activa o programada.
 *
 * GET `api/complexes/{complexId}/visitor-invitations/{invitationId}/code`
 * → 200 OK con `{ code }`, el mismo contenido que se dibuja como QR.
 * Si ya no está disponible responde 410.
 */
data class VisitorInvitationCodeDto(
    val code: String
)
