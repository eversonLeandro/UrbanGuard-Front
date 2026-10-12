package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Desactiva el token FCM al cerrar sesión para dejar de recibir pushes de
 * esta cuenta en el dispositivo. El backend responde 204 incluso si el token
 * ya no existe, así que este caso nunca falla por ese motivo.
 */
class UnregisterDeviceTokenUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(fcmToken: String): Result<Unit> =
        repository.deregisterDeviceToken(fcmToken)
}
