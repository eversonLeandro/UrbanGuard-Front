package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Request para crear un conjunto (complex) tras validar el código de activación.
 *
 * Endpoint: POST /api/complexes
 * Body: { activationCode, name, address, city, numberOfUnits }
 * Respuesta: 201 Created con CreateComplexResponseDto
 *
 * Nota: numberOfUnits es requerido por el backend (@NotNull @Positive).
 */
data class CreateComplexRequestDto(
    val activationCode: String,
    val name: String,
    val address: String,
    val city: String,
    val numberOfUnits: Int
)