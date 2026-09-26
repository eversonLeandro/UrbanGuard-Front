package co.edu.unicauca.urbanguard.domain.model

enum class MembershipRole { ADMIN, RESIDENT, GUARD }

/**
 * Resultado de la creación de un conjunto (complex).
 * POST /api/complexes → 201 Created con { complexId, membershipId, roles }.
 */
data class CreatedComplex(
    val complexId: String,
    val membershipId: String,
    val roles: List<MembershipRole>
)
