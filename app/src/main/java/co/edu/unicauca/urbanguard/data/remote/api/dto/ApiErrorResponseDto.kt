package co.edu.unicauca.urbanguard.data.remote.api.dto
/**
 * Forma esperada del cuerpo de error que devuelve el Gateway o los microservicios.
 * AJUSTAR los nombres de campo en cuanto el equipo de backend confirme
 * el formato real (esto es una suposición razonable, no un contrato confirmado).
 */
data class ApiErrorResponseDto(
    val message: String? = null,
    val code: String? = null,
    val service: String? = null
)
