package co.edu.unicauca.urbanguard.data.remote.api.dto

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Respuesta del endpoint POST /api/complexes/{complexId}/join-codes
 * Body: { "code": "ABC123", "expiresAt": "2026-09-19T02:35:00Z" }
 *
 * NOTA: El backend devuelve expiresAt como STRING ISO-8601 (ej: "2026-09-19T02:35:00Z"),
 * no como objeto. Se usa String en el DTO y se convierte en el mapper.
 */
data class ConjuntoCodeResponseDto(
    val code: String,
    val expiresAt: String
) {
    fun toInstant(): Instant {
        return try {
            Instant.parse(expiresAt)
        } catch (e: DateTimeParseException) {
            // Fallback para formatos con zona horaria explícita
            Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(expiresAt))
        }
    }
}
