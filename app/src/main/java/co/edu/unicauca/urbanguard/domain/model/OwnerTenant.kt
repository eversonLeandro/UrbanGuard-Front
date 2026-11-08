package co.edu.unicauca.urbanguard.domain.model

enum class ResidentType(val label: String) {
    OWNER("Propietario"),
    TENANT("Arrendatario")
}

data class OwnerTenantDraft(
    val type: ResidentType,
    val nombres: String,
    val apellidos: String,
    val documento: String,
    val email: String,
    val telefono: String
)

data class OwnerTenant(
    val id: String,
    val unitId: String,
    val unitIdentifier: String,
    val type: ResidentType,
    val fullName: String
)
