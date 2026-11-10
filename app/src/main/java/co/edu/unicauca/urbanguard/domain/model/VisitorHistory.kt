package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

data class VisitorHistoryItem(
    val invitationId: String,
    val creatorName: String,
    val visitorName: String,
    val guestCount: Int,
    val actualEntryAt: Instant
)

data class VisitorHistoryPage(
    val items: List<VisitorHistoryItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
