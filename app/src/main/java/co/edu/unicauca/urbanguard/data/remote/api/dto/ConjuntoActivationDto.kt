package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Payload de actualización de un conjunto YA existente (PUT onboarding/conjunto/{conjuntoId}).
 *
 * El conjunto se crea al validar el código de activación (paso ActivateConjunto),
 * por lo que aquí NO se envía codigoActivacion. El conjuntoId va en la URL del
 * endpoint, no en el body.
 */
data class RegisterConjuntoRequestDto(
    val nombre: String,
    val direccion: String,
    val ciudad: String,
    val totalUnidades: Int
)

/**
 * Request del endpoint de validación de activación.
 * Recibe el código de activación ACT-XXXX-YYYY.
 */
data class ActivationValidationRequestDto(
    val codigoActivacion: String
)

/**
 * Respuesta del endpoint de validación de activación
 * (POST onboarding/activation-code/validate).
 *
 * El backend, al validar ACT-XXXX-YYYY, crea el conjunto y retorna su PK
 * autogenerada. NO retorna el código de activación de vuelta.
 */
data class ActivationValidationResponseDto(
    val conjuntoId: String
)
