package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Request del endpoint de verificación de código de activación.
 * POST /api/activations/verifications
 * Body: { "code": "ACT-XXXX-YYYY" }
 * Éxito: 204 No Content (sin body de respuesta).
 */
data class ActivationRequestDto(
    val code: String
)

/**
 * Request del endpoint de creación de conjunto (complex).
 * POST /api/complexes
 * Body: { activationCode, name, address, city }
 *
 * NOTA: totalUnidades NO se incluye aquí porque el backend lo rechaza
 * con 400 ante campo desconocido. Se mantiene en el formulario local.
 */
data class RegisterConjuntoRequestDto(
    val activationCode: String,
    val name: String,
    val address: String,
    val city: String
)

/**
 * Respuesta del endpoint de creación de conjunto.
 * POST /api/complexes → 201 Created
 */
data class CreateComplexResponseDto(
    val complexId: String,
    val membershipId: String,
    val roles: List<String>
)
