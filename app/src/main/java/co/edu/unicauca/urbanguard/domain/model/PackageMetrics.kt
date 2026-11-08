package co.edu.unicauca.urbanguard.domain.model

data class PackageMetrics(
    val receivedCount: Long,
    val deliveredCount: Long,
    val pendingCount: Long,
    val returnedCount: Long,
    val canceledCount: Long,
    val averageDeliveryHours: Double?,
    val byStatus: Map<String, Long>,
    val trend: List<PackageTrendMetric>
)

data class PackageTrendMetric(
    val date: String,
    val receivedCount: Long,
    val deliveredCount: Long
)
