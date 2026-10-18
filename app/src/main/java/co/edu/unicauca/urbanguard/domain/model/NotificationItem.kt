package co.edu.unicauca.urbanguard.domain.model

/**
 * Aviso de la bandeja in-app. `createdAt` viaja como ISO-8601 y se formatea en
 * la UI; `readAt` es null mientras no se haya leído.
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val deepLink: String?,
    val channel: String,
    val priority: String,
    val conjuntoId: String,
    val createdAt: String,
    val readAt: String?
) {
    val isRead: Boolean
        get() = readAt != null
}

/** Página del listado de avisos. */
data class NotificationPage(
    val items: List<NotificationItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    /** True si existe una página posterior a [page]. */
    val hasMore: Boolean
        get() = page + 1 < totalPages
}
