package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRoundDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.StartRoundRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Rondas de patrullaje del guardia (UG-41 / UG-95).
 *
 * Contrato propuesto para el backend. Mientras
 * [PatrolConstants.PATROL_BACKEND_ENABLED] esté apagado el repositorio no
 * invoca estos métodos y trabaja contra el almacén local.
 */
interface PatrolRoundApi {

    @GET("api/complexes/{complexId}/patrol-rounds")
    suspend fun getRounds(
        @Path("complexId") complexId: String
    ): List<PatrolRoundDto>

    @POST("api/complexes/{complexId}/patrol-rounds")
    suspend fun startRound(
        @Path("complexId") complexId: String,
        @Body request: StartRoundRequestDto
    ): PatrolRoundDto

    @POST("api/complexes/{complexId}/patrol-rounds/{roundId}/scans")
    suspend fun registerScan(
        @Path("complexId") complexId: String,
        @Path("roundId") roundId: String,
        @Body request: RegisterScanRequestDto
    ): Response<Unit>

    @PUT("api/complexes/{complexId}/patrol-rounds/{roundId}/finish")
    suspend fun finishRound(
        @Path("complexId") complexId: String,
        @Path("roundId") roundId: String
    ): Response<Unit>
}
