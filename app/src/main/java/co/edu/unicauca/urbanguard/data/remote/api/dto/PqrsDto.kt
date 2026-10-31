package co.edu.unicauca.urbanguard.data.remote.api.dto


data class PqrsSummaryDto(
    val id: String,
    val type: String?,
    val subject: String,
    val description: String,
    val status: String?,
    val authorId: String?,
    val authorName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val adminResponse: String?,
    val respondedAt: String?,
    val resolvedAt: String?
)

data class PqrsDetailDto(
    val id: String,
    val type: String?,
    val subject: String,
    val description: String,
    val status: String?,
    val authorId: String?,
    val authorName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val adminResponse: String?,
    val respondedBy: String?,
    val respondedByName: String?,
    val respondedAt: String?,
    val resolvedAt: String?,
    val history: List<PqrsHistoryDto> = emptyList()
)

data class PqrsHistoryDto(
    val id: String?,
    val fromStatus: String?,
    val toStatus: String?,
    val response: String?,
    val respondedBy: String?,
    val respondedByName: String?,
    val createdAt: String?
)

data class UpdatePqrsStatusRequestDto(
    val status: String,
    val response: String
)

