package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePatrolBatchRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolExecutionDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolOccurrenceDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolPointViewDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class PatrolRoundRepositoryImplTest {
    @Test
    fun `both rejected NFC codes become the same typed point error`() = runTest {
        listOf(404 to "UNKNOWN_NFC_TAG", 409 to "CHECKPOINT_OUT_OF_ORDER").forEach { (status, code) ->
            val api = FakePatrolExecutionApi(httpException(status, code))
            val result = PatrolRoundRepositoryImpl(api).registerScan("c1", "AA:BB:CC:DD")

            assertTrue(result.isFailure)
            val error = (result.exceptionOrNull() as AppException).error
            assertTrue(error is AppError.UnexpectedPatrolPoint)
            assertEquals(AppError.UnexpectedPatrolPoint.MESSAGE, error.message)
            assertEquals("AABBCCDD", api.lastUid)
            assertEquals("r1", api.lastExecutionId)
        }
    }

    @Test
    fun `unrelated HTTP error keeps its own friendly meaning`() = runTest {
        val api = FakePatrolExecutionApi(httpException(503, "SERVICE_UNAVAILABLE"))

        val result = PatrolRoundRepositoryImpl(api).registerScan("c1", "AA:BB:CC:DD")

        val error = (result.exceptionOrNull() as AppException).error
        assertTrue(error is AppError.ServerError)
        assertEquals(AppError.ServerError.MESSAGE, error.message)
    }

    private fun httpException(status: Int, code: String): HttpException {
        val body = """{"code":"$code","message":"Service Unavailable"}"""
            .toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(status, body))
    }

    private class FakePatrolExecutionApi(private val scanError: HttpException) : PatrolExecutionApi {
        var lastUid: String? = null
        var lastExecutionId: String? = null
        private val execution = PatrolExecutionDto(
            executionId = "r1", occurrenceId = "o1", routeId = "route1", routeName = "Ronda A",
            scheduledAt = "2026-11-16T12:00:00Z", earliestStartAt = "2026-11-16T11:50:00Z",
            deadlineAt = "2026-11-16T15:00:00Z", startedAt = "2026-11-16T12:00:00Z",
            status = "IN_PROGRESS", pointsCompleted = 0, pointsExpected = 1,
            checkpoints = listOf(PatrolPointViewDto("p1", 1, "Entrada"))
        )

        override suspend fun getActiveExecution(complexId: String): Response<PatrolExecutionDto> =
            Response.success(execution)

        override suspend fun registerScan(
            complexId: String, executionId: String, idempotencyKey: String, request: CheckpointScanRequestDto
        ): PatrolExecutionDto {
            lastUid = request.uid
            lastExecutionId = executionId
            throw scanError
        }

        override suspend fun createBatch(
            complexId: String, request: CreatePatrolBatchRequestDto
        ): List<PatrolOccurrenceDto> = error("No usado")
        override suspend fun getAvailableOccurrences(
            complexId: String, from: String?, to: String?
        ): List<PatrolOccurrenceDto> = error("No usado")
        override suspend fun startOccurrence(complexId: String, occurrenceId: String): PatrolExecutionDto =
            error("No usado")
        override suspend fun getMyExecutions(complexId: String): List<PatrolExecutionDto> = error("No usado")
        override suspend fun getExecutionDetail(complexId: String, executionId: String): PatrolExecutionDto =
            error("No usado")
        override suspend fun finishExecution(complexId: String, executionId: String): PatrolExecutionDto =
            error("No usado")
    }
}
