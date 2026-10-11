package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteOrderDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Puntos de control NFC de la ruta de patrullaje (UG-40 / UG-93).
 *
 * Contrato propuesto para el backend. Mientras
 * [PatrolConstants.PATROL_BACKEND_ENABLED] esté apagado el repositorio no
 * invoca estos métodos y trabaja contra el almacén local.
 */
interface PatrolApi {

    @GET("api/complexes/{complexId}/patrol-points")
    suspend fun getPoints(
        @Path("complexId") complexId: String
    ): List<PatrolPointResponseDto>

    @POST("api/complexes/{complexId}/patrol-points")
    suspend fun createPoint(
        @Path("complexId") complexId: String,
        @Body request: PatrolPointRequestDto
    ): PatrolPointResponseDto

    @PUT("api/complexes/{complexId}/patrol-points/{pointId}")
    suspend fun updatePoint(
        @Path("complexId") complexId: String,
        @Path("pointId") pointId: String,
        @Body request: PatrolPointRequestDto
    ): PatrolPointResponseDto

    @DELETE("api/complexes/{complexId}/patrol-points/{pointId}")
    suspend fun deletePoint(
        @Path("complexId") complexId: String,
        @Path("pointId") pointId: String
    ): Response<Unit>

    @PUT("api/complexes/{complexId}/patrol-points/order")
    suspend fun updateOrder(
        @Path("complexId") complexId: String,
        @Body request: PatrolRouteOrderDto
    ): Response<Unit>

    // ── Rutas programadas (contrato propuesto) ──────────────────────────

    @GET("api/complexes/{complexId}/patrol-routes")
    suspend fun getRoutes(
        @Path("complexId") complexId: String
    ): List<PatrolRouteDto>

    @POST("api/complexes/{complexId}/patrol-routes")
    suspend fun createRoute(
        @Path("complexId") complexId: String,
        @Body request: PatrolRouteRequestDto
    ): PatrolRouteDto

    @PUT("api/complexes/{complexId}/patrol-routes/{routeId}")
    suspend fun updateRoute(
        @Path("complexId") complexId: String,
        @Path("routeId") routeId: String,
        @Body request: PatrolRouteRequestDto
    ): PatrolRouteDto

    @DELETE("api/complexes/{complexId}/patrol-routes/{routeId}")
    suspend fun deleteRoute(
        @Path("complexId") complexId: String,
        @Path("routeId") routeId: String
    ): Response<Unit>
}
