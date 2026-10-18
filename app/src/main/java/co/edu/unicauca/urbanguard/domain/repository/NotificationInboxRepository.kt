package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.NotificationPage

/**
 * Bandeja de notificaciones in-app del usuario autenticado. El backend acota
 * cada operación a las notificaciones del dueño del token.
 */
interface NotificationInboxRepository {

    suspend fun getNotifications(page: Int, size: Int, unreadOnly: Boolean): Result<NotificationPage>

    suspend fun getUnreadCount(): Result<Int>

    suspend fun markRead(id: String): Result<Unit>

    suspend fun markAllRead(): Result<Unit>
}
