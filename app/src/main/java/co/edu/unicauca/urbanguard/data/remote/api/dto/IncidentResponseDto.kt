package co.edu.unicauca.urbanguard.data.remote.api.dto

data class IncidentResponseDto(
    val reportId: String,
    val complexId: String,
    val guardId: String,
    val category: String,
    val description: String,
    val reportedAt: String,
    val executionId: String? = null,
    val checkpointId: String? = null,
    val hasImage: Boolean = true
)

data class IncidentImageAccessDto(
    val url: String,
    val expiresAt: String
)

data class CreateIncidentRequestDto(
    val description: String,
    val category: String,
    val executionId: String? = null,
    val checkpointId: String? = null
)
