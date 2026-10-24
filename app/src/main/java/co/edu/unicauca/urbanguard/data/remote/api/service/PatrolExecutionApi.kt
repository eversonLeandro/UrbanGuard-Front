package co.edu.unicauca.urbanguard.data.remote.api.service

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolExecutionDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolOccurrenceDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePatrolBatchRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints remotos de ocurrencias y ejecuciones de patrullaje.
 */
interface PatrolExecutionApi {

    @POST(PatrolApiPaths.OCCURRENCES_BATCHES)
    suspend fun createBatch(
        @Path("complexId") complexId: String,
        @Body request: CreatePatrolBatchRequestDto
    ): List<PatrolOccurrenceDto>

    @GET(PatrolApiPaths.OCCURRENCES_AVAILABLE)
    suspend fun getAvailableOccurrences(
        @Path("complexId") complexId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): List<PatrolOccurrenceDto>

    @POST(PatrolApiPaths.OCCURRENCE_START)
    suspend fun startOccurrence(
        @Path("complexId") complexId: String,
        @Path("occurrenceId") occurrenceId: String
    ): PatrolExecutionDto

    @GET(PatrolApiPaths.EXECUTION_ACTIVE)
    suspend fun getActiveExecution(
        @Path("complexId") complexId: String
    ): Response<PatrolExecutionDto>

    @GET(PatrolApiPaths.EXECUTION_DETAIL)
    suspend fun getExecutionDetail(
        @Path("complexId") complexId: String,
        @Path("executionId") executionId: String
    ): PatrolExecutionDto

    @POST(PatrolApiPaths.EXECUTION_SCANS)
    suspend fun registerScan(
        @Path("complexId") complexId: String,
        @Path("executionId") executionId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CheckpointScanRequestDto
    ): PatrolExecutionDto

    @POST(PatrolApiPaths.EXECUTION_FINISH)
    suspend fun finishExecution(
        @Path("complexId") complexId: String,
        @Path("executionId") executionId: String
    ): PatrolExecutionDto
}
