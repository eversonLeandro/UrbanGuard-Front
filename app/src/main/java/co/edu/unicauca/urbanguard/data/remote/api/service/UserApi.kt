package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.PasswordChangeTicketResponseDto
import retrofit2.http.POST

/**
 * Endpoints de usuario administrados por identity-service a través del API Gateway.
 */
interface UserApi {

    /**
     * Solicita al backend la generación de un ticket seguro de cambio de contraseña en Auth0.
     * Requiere que el token enviado tenga un auth_time fresco (menor a 5 minutos).
     */
    @POST("api/users/me/password-change")
    suspend fun createPasswordChangeTicket(): PasswordChangeTicketResponseDto
}
