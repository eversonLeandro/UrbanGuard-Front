package co.edu.unicauca.urbanguard.domain.model

data class GuardMembership(
    val membershipId: String,
    val userId: String,
    val email: String?,
    val fullName: String,
    val active: Boolean
)
