package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import javax.inject.Inject

/** Marca todos los avisos del usuario como leídos. */
class MarkAllNotificationsReadUseCase @Inject constructor(
    private val repository: NotificationInboxRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.markAllRead()
}
