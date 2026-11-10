package co.edu.unicauca.urbanguard.data.remote.api.dto

// Los campos obligatorios se comprueban al mapear. Gson deja en null los campos ausentes.
data class VisitorHistoryItemDto(
    val invitationId: String?,
    val creatorName: String?,
    val visitorName: String?,
    val guestCount: Int?,
    val actualEntryAt: String?
)

data class VisitorHistoryPageDto(
    val content: List<VisitorHistoryItemDto?>?,
    val page: Int?,
    val size: Int?,
    val totalElements: Long?,
    val totalPages: Int?
)
