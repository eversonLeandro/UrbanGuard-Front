package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Cuerpo de POST /api/complexes/{complexId}/guards.
 *
 * Los cuatro campos son obligatorios: el backend los marca con `@NotBlank` y el
 * teléfono además es la contraseña inicial con la que el vigilante entra a la
 * plataforma, por eso no puede llegar vacío ni en blanco.
 *
 * Los nombres coinciden con los del DTO Java `CreateGuardRequest`, así que no
 * hacen falta `@SerializedName`.
 */
data class CreateGuardRequestDto(
    val email: String,
    val nombres: String,
    val apellidos: String,
    val telefono: String
)

/**
 * Respuesta 201 de POST /api/complexes/{complexId}/guards.
 *
 * Refleja el record Java `CreateGuardResponse(membershipId, userId, email,
 * nombres, role)`. [userId] es el `publicId` UUID de identity-service, no el
 * `sub` de Auth0: la app no necesita el `sub` para nada de este flujo.
 *
 * No llega `apellidos` ni `telefono`, y `role` siempre vale "GUARD" porque el
 * endpoint no admite otro.
 */
data class CreateGuardResponseDto(
    val membershipId: String,
    val userId: String,
    val email: String,
    val nombres: String,
    val role: String
)

data class GuardMembershipDto(
    val membershipId: String,
    val userId: String,
    val email: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val active: Boolean
)
