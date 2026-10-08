package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo para crear un pase QR de visitante.
 *
 * POST `api/complexes/{complexId}/visitor-invitations` (el `complexId` va en la
 * ruta, nunca en el body). Éxito: 201 Created.
 * Body: { "visitorName": String, "guestCount": Int, "visitDate": "yyyy-MM-dd" }
 *
 * El backend hace parseo estricto del cuerpo: cualquier campo extra o de tipo
 * distinto responde 400, por eso los nombres JSON deben coincidir exactamente.
 */
data class VisitorQrRequestDto(
    val visitorName: String,
    val guestCount: Int,
    val visitDate: String
)

/**
 * Respuesta de la creación de un pase QR de visitante.
 *
 * POST `api/complexes/{complexId}/visitor-invitations` → 201 Created
 * Body: { "invitationId": "<UUID>", "code": "<22 caracteres>", "visitDate": "yyyy-MM-dd" }
 *
 * `code` es el contenido exacto que el frontend dibuja con ZXing (es el mismo
 * valor que el backend valida al escanear); `visitDate` llega como texto ISO
 * (yyyy-MM-dd) y se convierte a LocalDate en el mapper.
 */
data class VisitorQrResponseDto(
    val invitationId: String,
    val code: String,
    val visitDate: String
)
