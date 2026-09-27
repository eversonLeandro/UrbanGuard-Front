package co.edu.unicauca.urbanguard.domain.model

/**
 * Resultado del alta de un vigilante (HU-009).
 * POST /api/complexes/{complexId}/guards → 201 Created.
 *
 * [userId] es el `publicId` UUID de identity-service, no el `sub` de Auth0.
 */
data class CreatedGuard(
    val membershipId: String,
    val userId: String,
    val email: String,
    val nombres: String,
    val role: MembershipRole
)
