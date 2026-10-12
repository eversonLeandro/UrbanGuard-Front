package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.remote.api.dto.DeregisterDeviceTokenRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterDeviceTokenRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.NotificationApi
import co.edu.unicauca.urbanguard.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override suspend fun registerDeviceToken(
        fcmToken: String,
        deviceId: String?,
        conjuntoId: String
    ): Result<Unit> = apiCall {
        notificationApi.registerDeviceToken(RegisterDeviceTokenRequestDto(fcmToken, deviceId, conjuntoId))
    }

    override suspend fun deregisterDeviceToken(fcmToken: String): Result<Unit> = apiCall {
        notificationApi.deregisterDeviceToken(DeregisterDeviceTokenRequestDto(fcmToken))
    }
}
