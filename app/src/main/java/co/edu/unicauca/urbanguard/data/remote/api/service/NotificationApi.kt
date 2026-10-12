package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.DeregisterDeviceTokenRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterDeviceTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

/**
 * Registro de tokens FCM (notification-service vía API Gateway).
 *
 * La BASE_URL ya apunta al gateway, así que el path coincide con la ruta
 * `notification-service` del gateway, que solo acepta POST y DELETE.
 * Ambas responden 204 sin cuerpo: se devuelve Unit y los errores HTTP llegan
 * como `HttpException` al [NetworkErrorMapper].
 *
 * Retrofit no admite @Body en @DELETE, por eso la baja usa @HTTP con cuerpo.
 */
interface NotificationApi {

    /**
     * Registra (upsert) el token en el conjunto activo. El backend resuelve
     * usuario/rol del lado servidor; el `conjuntoId` solo es un selector.
     * Errores: 400 (body inválido), 401 (sin JWT), 403 (sin rol de negocio o
     * sin membresía en el conjunto), 409 (carrera de registro), 429 y 5xx.
     */
    @POST("api/notifications/device-token")
    suspend fun registerDeviceToken(@Body body: RegisterDeviceTokenRequestDto)

    /**
     * Desactiva el token del usuario. 204 incluso si no existe o es ajeno.
     */
    @HTTP(method = "DELETE", path = "api/notifications/device-token", hasBody = true)
    suspend fun deregisterDeviceToken(@Body body: DeregisterDeviceTokenRequestDto)
}
