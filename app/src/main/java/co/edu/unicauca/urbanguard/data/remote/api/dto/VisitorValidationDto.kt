package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo para validar un pase QR de visitante (flujo guardia).
 *
 * POST `api/complexes/{complexId}/visitor-invitations/validation`.
 * Body: { "code": "<22 caracteres base64url>" }
 *
 * El backend hace parseo estricto: el objeto debe traer solo `code` como
 * string de 22 caracteres [A-Za-z0-9_-] que decodifique a 16 bytes.
 */
data class VisitorValidationRequestDto(
    val code: String
)

/**
 * Respuesta exitosa de la validación (consume el pase, un solo uso).
 *
 * POST `api/complexes/{complexId}/visitor-invitations/validation` → 200 OK
 * Body: { invitationId, visitorName, guestCount, visitDate, status, usedAt }
 *
 * `visitDate` llega como texto ISO (yyyy-MM-dd) y se convierte a LocalDate
 * en el mapper. `status` llega como "USED" tras el consumo.
 */
data class VisitorValidationResponseDto(
    val invitationId: String,
    val visitorName: String,
    val guestCount: Int,
    val visitDate: String,
    val status: String? = null,
    val usedAt: String? = null
)

/**
 * Cuerpo de error específico de la validación de invitaciones.
 *
 * A diferencia del `ApiError` estándar, este endpoint responde
 * `{ reason, message }` con `reason` en:
 * INVALID_TOKEN (404), USED (409), EXPIRED (410), NOT_YET_VALID (409).
 */
data class VisitorValidationErrorDto(
    val reason: String? = null,
    val message: String? = null
)
