package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.PasswordChangeTicketResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.UserResponseDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de usuario administrados por identity-service a traves del API Gateway.
 */
interface UserApi {

    /**
     * Solicita al backend la generacion de un ticket seguro de cambio de contrasena en Auth0.
     * Requiere que el token enviado tenga un auth_time fresco (menor a 5 minutos).
     */
    @POST("api/users/me/password-change")
    suspend fun createPasswordChangeTicket(): PasswordChangeTicketResponseDto

    /**
     * Listado administrativo de cuentas (HU-006) con su filtro de estado:
     * `activos` (default), `inactivos` o `todos`.
     *
     * Solo SUPERADMIN (claim `https://urbanguard.com/staff`): un ADMIN de
     * conjunto recibe 403, ya mapeado por
     * [co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper] como
     * [co.edu.unicauca.urbanguard.domain.model.AppError.Forbidden].
     * [estado] llega del ViewModel con la constante del repositorio; el valor
     * lo valida el backend (400 si no es reconocido).
     */
    @GET("api/users")
    suspend fun listUsers(
        @Query("estado", encoded = true) estado: String? = null
    ): List<UserResponseDto>

    /**
     * Desactiva (soft delete) la cuenta identificada por su `sub` de Auth0.
     * Exito: 204 sin cuerpo. El backend rechaza con 409 si el actor del JWT es
     * el mismo objetivo (guardia anti-autodesactivacion, mensaje de negocio en
     * espanol); se mantiene como red de seguridad detras del bloqueo de la UI.
     */
    @DELETE("api/users/{userId}")
    suspend fun deactivateUser(@Path("userId") userId: String)

    /**
     * Reactiva una cuenta desactivada. Exito: 200 con el estado actualizado.
     * Solo SUPERADMIN; el M2M con `write:users` no pasa (el backend responde 403).
     * Auth0 es la fuente de verdad: el desbloqueo (`blocked=false`) corre siempre
     * (PATCH idempotente) y la DB solo se sincroniza despues (espejo).
     */
    @POST("api/users/{userId}/reactivate")
    suspend fun reactivateUser(@Path("userId") userId: String): UserResponseDto
}
