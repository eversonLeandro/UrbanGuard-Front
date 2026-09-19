package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Request para validar código de activación.
 *
 * Endpoint: POST /api/activations/verifications
 * Body: { "code": "ACT-XXXX-YYYY" }
 * Respuesta: 204 No Content
 */
data class ActivationRequestDto(
    val code: String
)