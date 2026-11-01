package co.edu.unicauca.urbanguard.domain.model

data class ResidentialResident(
    val userId: String,
    val fullName: String
)

data class ResidentialUnit(
    val id: String,
    val identifier: String,
    val residents: List<ResidentialResident>
)
