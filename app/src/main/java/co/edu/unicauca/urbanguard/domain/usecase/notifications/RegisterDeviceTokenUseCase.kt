package co.edu.unicauca.urbanguard.domain.usecase.notifications

import co.edu.unicauca.urbanguard.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Registra el token FCM del dispositivo en el conjunto activo del usuario
 * (upsert en el backend). Best-effort: el llamador decide si el fallo se
 * muestra o solo se registra, porque el push es informativo.
 */
class RegisterDeviceTokenUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(fcmToken: String, deviceId: String?, conjuntoId: String): Result<Unit> =
        repository.registerDeviceToken(fcmToken, deviceId, conjuntoId)
}
