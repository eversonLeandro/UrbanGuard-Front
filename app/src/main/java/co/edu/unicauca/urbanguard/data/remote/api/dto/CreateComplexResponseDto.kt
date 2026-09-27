package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Respuesta de creación de conjunto (complex).
 *
 * Endpoint: POST /api/complexes → 201 Created
 * Body: { complexId, membershipId, role }
 */
data class CreateComplexResponseDto(
    val complexId: String,
    val membershipId: String,
    val role: String
)
