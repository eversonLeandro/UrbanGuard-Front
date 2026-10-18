package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.repository.NotificationInboxRepository
import javax.inject.Inject

/** Marca un aviso como leído. */
class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationInboxRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.markRead(id)
}
