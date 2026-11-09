package co.edu.unicauca.urbanguard.domain.model

data class ResidentialResident(
    val userId: String? = null,
    val ownerTenantId: String? = null,
    val membershipId: String? = null,
    val fullName: String,
    val type: ResidentType? = null,
    val active: Boolean = true
)

data class ResidentialUnit(
    val id: String,
    val identifier: String,
    val residents: List<ResidentialResident>
)
