package co.edu.unicauca.urbanguard.domain.model

data class ResidentPqrsSummary(
    val id: String,
    val type: PqrsType,
    val subject: String,
    val status: PqrsStatus,
    val createdAt: String
)

data class ResidentPqrsDetail(
    val id: String,
    val type: PqrsType,
    val subject: String,
    val description: String,
    val status: PqrsStatus,
    val createdAt: String,
    val history: List<ResidentPqrsHistoryEntry> = emptyList()
)

data class ResidentPqrsHistoryEntry(
    val id: String,
    val fromStatus: PqrsStatus,
    val toStatus: PqrsStatus,
    val response: String,
    val createdAt: String
)

data class ResidentPqrsDraft(
    val type: PqrsType,
    val subject: String,
    val description: String
)
