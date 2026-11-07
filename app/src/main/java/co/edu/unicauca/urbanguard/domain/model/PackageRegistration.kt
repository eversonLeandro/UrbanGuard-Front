package co.edu.unicauca.urbanguard.domain.model

data class PackageRegistration(
    val residentialUnitId: String?,
    val residentialUnitLabel: String?,
    val residentId: String?,
    val recipientName: String,
    val sender: String?,
    val description: String,
    val observations: String?,
    val photoPath: String?
)
