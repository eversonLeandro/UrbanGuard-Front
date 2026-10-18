package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.model.NotificationPage
import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import javax.inject.Inject

/** Lista paginada de la bandeja in-app del usuario. */
class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationInboxRepository
) {
    suspend operator fun invoke(page: Int, size: Int, unreadOnly: Boolean): Result<NotificationPage> =
        repository.getNotifications(page, size, unreadOnly)
}
