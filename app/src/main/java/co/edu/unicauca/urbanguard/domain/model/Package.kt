package co.edu.unicauca.urbanguard.domain.model

data class Package(
    val id: String,
    val complexId: String,
    val residentialUnitId: String?,
    val residentialUnitLabel: String?,
    val residentId: String?,
    val recipientName: String,
    val sender: String?,
    val description: String,
    val observations: String?,
    val status: PackageStatus,
    val receivedAt: String,
    val deliveredTo: String?,
    val deliveredAt: String?,
    val evidenceUrl: String? = null
)

enum class PackageStatus { RECIBIDO, ENTREGADO, DEVUELTO, CANCELADO }
