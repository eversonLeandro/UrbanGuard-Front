package co.edu.unicauca.urbanguard.data.remote.api.dto

data class ResidentialUnitDto(
    val id: String? = null,
    val identifier: String? = null,
    val residents: List<ResidentialResidentDto>? = null
)

data class ResidentialResidentDto(
    val userId: String? = null,
    val ownerTenantId: String? = null,
    val membershipId: String? = null,
    val fullName: String? = null,
    val type: String? = null,
    val active: Boolean = true
)
