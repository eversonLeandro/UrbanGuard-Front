package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toNotificationPage
import co.edu.unicauca.urbanguard.data.remote.api.service.NotificationInboxApi
import co.edu.unicauca.urbanguard.domain.model.NotificationPage
import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import javax.inject.Inject

class NotificationInboxRepositoryImpl @Inject constructor(
    private val api: NotificationInboxApi
) : NotificationInboxRepository {

    override suspend fun getNotifications(
        page: Int,
        size: Int,
        unreadOnly: Boolean
    ): Result<NotificationPage> = apiCall {
        api.getNotifications(page, size, unreadOnly.takeIf { it }).toNotificationPage()
    }

    override suspend fun getUnreadCount(): Result<Int> = apiCall {
        api.getUnreadCount().unreadCount.toInt().coerceAtLeast(0)
    }

    override suspend fun markRead(id: String): Result<Unit> = apiCall {
        api.markRead(id)
    }

    override suspend fun markAllRead(): Result<Unit> = apiCall {
        api.markAllRead()
    }
}
