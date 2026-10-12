package co.edu.unicauca.urbanguard.domain.repository

/**
 * Registro del token FCM del dispositivo en el backend.
 */
interface NotificationRepository {

    suspend fun registerDeviceToken(fcmToken: String, deviceId: String?, conjuntoId: String): Result<Unit>

    suspend fun deregisterDeviceToken(fcmToken: String): Result<Unit>
}
