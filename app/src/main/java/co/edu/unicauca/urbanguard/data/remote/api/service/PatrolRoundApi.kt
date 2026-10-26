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
 * Contrato legacy de rondas. El flujo actual usa [PatrolExecutionApi].
 */
interface PatrolRoundApi {

    @GET(PatrolApiPaths.ROUNDS)
    suspend fun getRounds(
        @Path("complexId") complexId: String
    ): List<PatrolRoundDto>

    @POST(PatrolApiPaths.ROUNDS)
    suspend fun startRound(
        @Path("complexId") complexId: String,
        @Body request: StartRoundRequestDto
    ): PatrolRoundDto

    @POST(PatrolApiPaths.ROUND_SCANS)
    suspend fun registerScan(
        @Path("complexId") complexId: String,
        @Path("roundId") roundId: String,
        @Body request: RegisterScanRequestDto
    ): Response<Unit>

    @PUT(PatrolApiPaths.ROUND_FINISH)
    suspend fun finishRound(
        @Path("complexId") complexId: String,
        @Path("roundId") roundId: String
    ): Response<Unit>
}
