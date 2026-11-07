package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo de error estándar que devuelve el API Gateway y los microservicios.
 *
 * Refleja el formato real de `ApiError` del backend. [correlationId] es la
 * referencia que permite rastrear el fallo en los logs del servidor, por eso
 * se conserva aunque no se muestre al usuario.
 */
data class ApiErrorResponseDto(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val correlationId: String? = null,
    val code: String? = null
)
