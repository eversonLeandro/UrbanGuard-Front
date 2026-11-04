package co.edu.unicauca.urbanguard.domain.model

import java.time.OffsetDateTime

data class News(
    val announcementId: String,
    val title: String,
    val body: String,
    val publishedAt: OffsetDateTime,
    val attachment: NewsAttachment?
)
