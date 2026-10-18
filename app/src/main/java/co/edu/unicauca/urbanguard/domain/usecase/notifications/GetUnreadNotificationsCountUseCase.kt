package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import javax.inject.Inject

/** Cuenta los avisos no leídos para la insignia de la campana. */
class GetUnreadNotificationsCountUseCase @Inject constructor(
    private val repository: NotificationInboxRepository
) {
    suspend operator fun invoke(): Result<Int> = repository.getUnreadCount()
}
