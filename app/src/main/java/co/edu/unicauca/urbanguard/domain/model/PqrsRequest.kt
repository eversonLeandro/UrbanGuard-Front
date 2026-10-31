package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

enum class PqrsStatus(val spanishLabel: String) {
    SUBMITTED("Radicada"),
    IN_REVIEW("En revisión"),
    IN_PROGRESS("En proceso"),
    RESOLVED("Resuelta"),
    REJECTED("Rechazada"),
    CLOSED("Cerrada");

    val isTerminal: Boolean
        get() = this == RESOLVED || this == REJECTED || this == CLOSED

    companion object {
        fun fromString(value: String?): PqrsStatus = entries.firstOrNull {
            it.name.equals(value?.trim(), ignoreCase = true)
        } ?: SUBMITTED
    }
}

enum class PqrsType(val spanishLabel: String) {
    PETITION("Petición"),
    COMPLAINT("Queja"),
    CLAIM("Reclamo"),
    SUGGESTION("Sugerencia");

    companion object {
        fun fromString(value: String?): PqrsType = entries.firstOrNull {
            it.name.equals(value?.trim(), ignoreCase = true)
        } ?: PETITION
    }
}

data class PqrsSummary(
    val id: String,
    val type: PqrsType,
    val subject: String,
    val description: String,
    val status: PqrsStatus,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String,
    val adminResponse: String?,
    val respondedAt: String?,
    val resolvedAt: String?
)

data class PqrsHistoryEntry(
    val id: String,
    val fromStatus: PqrsStatus,
    val toStatus: PqrsStatus,
    val response: String,
    val respondedBy: String?,
    val respondedByName: String,
    val createdAt: String
)

data class PqrsDetail(
    val id: String,
    val type: PqrsType,
    val subject: String,
    val description: String,
    val status: PqrsStatus,
    val authorId: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String,
    val adminResponse: String?,
    val respondedBy: String?,
    val respondedByName: String?,
    val respondedAt: String?,
    val resolvedAt: String?,
    val history: List<PqrsHistoryEntry>
)

