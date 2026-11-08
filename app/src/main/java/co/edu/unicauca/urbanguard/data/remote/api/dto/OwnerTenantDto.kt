package co.edu.unicauca.urbanguard.data.remote.api.dto

data class CreateOwnerTenantRequestDto(
    val type: String,
    val nombres: String,
    val apellidos: String,
    val documento: String,
    val email: String,
    val telefono: String
)

data class OwnerTenantResponseDto(
    val id: String? = null,
    val unitId: String? = null,
    val unitIdentifier: String? = null,
    val type: String? = null,
    val fullName: String? = null
)
