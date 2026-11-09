package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateGuardRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateGuardResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.GuardMembershipDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit para el alta de vigilantes (HU-009), vía API Gateway.
 *
 * La BASE_URL ya apunta al gateway, así que el path coincide con la ruta
 * `community-service-guards` del gateway (application.yaml:153-172), que solo
 * acepta POST y no aplica Retry por diseño.
 *
 * Se devuelve el DTO directamente y no `Response<T>`: así los errores HTTP
 * llegan al repositorio como `HttpException` y los traduce
 * `NetworkErrorMapper`, que ya distingue 403, 404, 409 y los 5xx de este flujo.
 */
interface GuardApi {

    /**
     * Crea la cuenta de un vigilante y lo agrega como GUARD del conjunto.
     *
     * Éxito: 201 Created con `{membershipId, userId, email, nombres, role}`.
     * Errores: 400 (validación por campo), 401 (token vencido), 403 (el usuario
     * no es ADMIN de ese conjunto), 404 (el conjunto no existe), 409 (ya es
     * personal de este conjunto), 429 (rate limit del gateway) y
     * 502/503/504 (identity no disponible).
     */
    @POST("api/complexes/{complexId}/guards")
    suspend fun createGuard(
        @Path("complexId") complexId: String,
        @Body body: CreateGuardRequestDto
    ): CreateGuardResponseDto

    @GET("api/complexes/{complexId}/guards")
    suspend fun getGuards(
        @Path("complexId") complexId: String,
        @Query("estado") status: String
    ): List<GuardMembershipDto>

    @DELETE("api/complexes/{complexId}/guards/{membershipId}")
    suspend fun deactivateGuard(
        @Path("complexId") complexId: String,
        @Path("membershipId") membershipId: String
    )

    @POST("api/complexes/{complexId}/guards/{membershipId}/reactivate")
    suspend fun reactivateGuard(
        @Path("complexId") complexId: String,
        @Path("membershipId") membershipId: String
    )
}
