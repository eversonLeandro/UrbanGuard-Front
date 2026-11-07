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

data class PackageMetricsDto(
    val receivedCount: Long,
    val deliveredCount: Long,
    val pendingCount: Long,
    val returnedCount: Long,
    val canceledCount: Long,
    val averageDeliveryHours: Double?,
    val byStatus: List<PackageStatusMetricDto>,
    val trend: List<PackageTrendMetricDto>
)

data class PackageStatusMetricDto(val status: String, val count: Long)

data class PackageTrendMetricDto(
    val date: String,
    val receivedCount: Long,
    val deliveredCount: Long
)
