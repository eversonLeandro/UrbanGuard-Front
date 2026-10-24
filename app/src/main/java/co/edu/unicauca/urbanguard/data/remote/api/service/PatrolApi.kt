package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreateCheckpointRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ReplaceCheckpointTagRequestDto
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Checkpoints NFC y rutas de patrullaje.
 *
 * Los puntos usan el contrato real de `security-service`. Las rutas todavía
 * no tienen endpoint: su repositorio solo llama estos métodos si
 * [PatrolConstants.PATROL_ROUTES_REMOTE] se enciende en el futuro.
 */
interface PatrolApi {

    @GET(PatrolApiPaths.POINTS)
    suspend fun getPoints(
        @Path("complexId") complexId: String
    ): List<CheckpointResponseDto>

    @GET(PatrolApiPaths.POINT)
    suspend fun getPoint(
        @Path("complexId") complexId: String,
        @Path("checkpointId") checkpointId: String
    ): CheckpointResponseDto

    @POST(PatrolApiPaths.POINTS)
    suspend fun createPoint(
        @Path("complexId") complexId: String,
        @Body request: CreateCheckpointRequestDto
    ): CheckpointResponseDto

    @PATCH(PatrolApiPaths.POINT)
    suspend fun updatePoint(
        @Path("complexId") complexId: String,
        @Path("checkpointId") checkpointId: String,
        @Body request: JsonObject
    ): CheckpointResponseDto

    @POST(PatrolApiPaths.TAG_ASSIGNMENTS)
    suspend fun replaceTag(
        @Path("complexId") complexId: String,
        @Path("checkpointId") checkpointId: String,
        @Body request: ReplaceCheckpointTagRequestDto
    ): CheckpointResponseDto

    // ── Rutas programadas (sin backend todavía) ─────────────────────────

    @GET(PatrolApiPaths.ROUTES)
    suspend fun getRoutes(
        @Path("complexId") complexId: String
    ): List<PatrolRouteDto>

    @POST(PatrolApiPaths.ROUTES)
    suspend fun createRoute(
        @Path("complexId") complexId: String,
        @Body request: PatrolRouteRequestDto
    ): PatrolRouteDto

    @PUT(PatrolApiPaths.ROUTE)
    suspend fun updateRoute(
        @Path("complexId") complexId: String,
        @Path("routeId") routeId: String,
        @Body request: PatrolRouteRequestDto
    ): PatrolRouteDto

    @PATCH(PatrolApiPaths.ROUTE)
    suspend fun patchRoute(
        @Path("complexId") complexId: String,
        @Path("routeId") routeId: String,
        @Body request: JsonObject
    ): PatrolRouteDto

    @DELETE(PatrolApiPaths.ROUTE)
    suspend fun deleteRoute(
        @Path("complexId") complexId: String,
        @Path("routeId") routeId: String
    ): Response<Unit>
}
