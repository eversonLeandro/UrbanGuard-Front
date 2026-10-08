package co.edu.unicauca.urbanguard.domain.model

import java.time.LocalDate

/**
 * Resultado de validar un pase QR de visitante (flujo guardia).
 *
 * Es lo que devuelve el backend al consumir la invitación: los datos del
 * visitante para mostrar en la tarjeta de "Acceso Autorizado".
 */
data class VisitorValidationResult(
    val invitationId: String,
    val visitorName: String,
    val guestCount: Int,
    val visitDate: LocalDate
)

/**
 * Motivo de rechazo de un pase QR, mapeado desde el `reason` del backend
 * (`INVALID_TOKEN`, `USED`, `EXPIRED`, `NOT_YET_VALID`) o desde fallos
 * locales / HTTP genéricos.
 */
enum class VisitorValidationFailure {
    INVALID_TOKEN,
    USED,
    EXPIRED,
    NOT_YET_VALID,
    FORBIDDEN,
    NOT_FOUND,
    NO_ACTIVE_COMPLEX,
    INVALID_FORMAT,
    SESSION_EXPIRED,
    NO_CONNECTION,
    SERVER_ERROR,
    UNKNOWN
}

/**
 * Extrae el [VisitorValidationFailure] de un `Result.failure` producido por
 * la validación. El repositorio lo prefija como `FAILURE|mensaje`.
 */
fun Throwable.toValidationFailure(): VisitorValidationFailure {
    val raw = (this as? AppException)?.error?.message ?: this.message ?: return VisitorValidationFailure.UNKNOWN
    val prefix = raw.substringBefore("|", "").trim()
    return runCatching { VisitorValidationFailure.valueOf(prefix) }.getOrNull()
        ?: VisitorValidationFailure.UNKNOWN
}

/** Mensaje limpio sin el prefijo `FAILURE|`. */
fun Throwable.validationDisplayMessage(fallback: String): String {
    val raw = (this as? AppException)?.error?.message ?: this.message ?: return fallback
    return if ("|" in raw) raw.substringAfter("|") else raw
}
