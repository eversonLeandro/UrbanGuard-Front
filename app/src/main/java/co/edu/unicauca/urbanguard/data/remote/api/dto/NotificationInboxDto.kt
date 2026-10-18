package co.edu.unicauca.urbanguard.data.remote.api.dto

/**
 * Aviso de la bandeja in-app tal como lo entrega el notification-service.
 *
 * `deepLink` puede ser null (avisos sin acción de navegación) y `readAt` es
 * null mientras el aviso no se haya leído.
 */
data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val channel: String,
    val priority: String,
    val conjuntoId: String,
    val createdAt: String,
    val readAt: String? = null
)

/** Página de la bandeja in-app. */
data class NotificationPageDto(
    val items: List<NotificationDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

/** Conteo de avisos no leídos. */
data class UnreadCountDto(
    val unreadCount: Long
)
