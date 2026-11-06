package co.edu.unicauca.urbanguard.data.remote.api.dto

import java.util.UUID

data class PackageDto(
    val id: UUID,
    val complexId: UUID,
    val residentialUnitId: UUID?,
    val residentialUnitLabel: String?,
    val residentId: UUID?,
    val recipientName: String,
    val sender: String?,
    val description: String,
    val observations: String?,
    val status: String,
    val receivedAt: String,
    val deliveredTo: String?,
    val deliveredAt: String?,
    val evidenceUrl: String? = null
)

data class RegisterPackageRequestDto(
    val residentialUnitId: String?,
    val residentialUnitLabel: String?,
    val residentId: String?,
    val recipientName: String,
    val sender: String?,
    val description: String,
    val observations: String?,
    val evidenceUrl: String? = null
)

data class DeliverPackageRequestDto(val deliveredTo: String)
