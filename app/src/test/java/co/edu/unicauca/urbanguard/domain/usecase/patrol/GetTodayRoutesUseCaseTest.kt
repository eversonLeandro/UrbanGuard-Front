package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePatrolBatchRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolExecutionDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolOccurrenceDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class GetTodayRoutesUseCaseTest {
    @Test
    fun includesOccurrencesThroughTheNextSevenDaysAndKeepsYesterdayForOvernightRounds() = runTest {
        val today = LocalDate.now(ZoneId.of("America/Bogota"))
        val api = FakePatrolExecutionApi(
            listOf(
                occurrence("yesterday", today.minusDays(1), "IN_PROGRESS"),
                occurrence("today", today, "SCHEDULED"),
                occurrence("tomorrow", today.plusDays(1), "SCHEDULED"),
                occurrence("next-week", today.plusDays(7), "SCHEDULED"),
                occurrence("completed", today.plusDays(2), "COMPLETED")
            )
        )
        val useCase = GetTodayRoutesUseCase(FakePatrolRouteRepository(), api)

        val result = useCase("complex-id").getOrThrow()

        assertEquals(
            listOf("yesterday", "today", "tomorrow", "next-week"),
            result.map { it.occurrenceId }
        )
        assertEquals(today.minusDays(1).toString(), api.requestedFrom)
        assertEquals(today.plusDays(7).toString(), api.requestedTo)
    }

    private class FakePatrolExecutionApi(
        private val occurrences: List<PatrolOccurrenceDto>
    ) : PatrolExecutionApi {
        var requestedFrom: String? = null
        var requestedTo: String? = null

        override suspend fun createBatch(
            complexId: String,
            request: CreatePatrolBatchRequestDto
        ): List<PatrolOccurrenceDto> = error("Not used")

        override suspend fun getAvailableOccurrences(
            complexId: String,
            from: String?,
            to: String?
        ): List<PatrolOccurrenceDto> {
            requestedFrom = from
            requestedTo = to
            return occurrences
        }

        override suspend fun startOccurrence(
            complexId: String,
            occurrenceId: String
        ): PatrolExecutionDto = error("Not used")

        override suspend fun getActiveExecution(complexId: String): Response<PatrolExecutionDto> =
            error("Not used")

        override suspend fun registerScan(
            complexId: String,
            executionId: String,
            idempotencyKey: String,
            request: CheckpointScanRequestDto
        ): PatrolExecutionDto = error("Not used")

        override suspend fun finishExecution(
            complexId: String,
            executionId: String
        ): PatrolExecutionDto = error("Not used")

        override suspend fun getMyExecutions(complexId: String): List<PatrolExecutionDto> =
            error("Not used")

        override suspend fun getExecutionDetail(
            complexId: String,
            executionId: String
        ): PatrolExecutionDto = error("Not used")
    }

    private class FakePatrolRouteRepository : PatrolRouteRepository {
        override suspend fun getRoutes(complexId: String): Result<List<PatrolRoute>> =
            Result.success(emptyList())

        override suspend fun saveRoute(route: PatrolRoute): Result<PatrolRoute> =
            Result.success(route)

        override suspend fun deleteRoute(complexId: String, routeId: String): Result<Unit> =
            Result.success(Unit)
    }

    private companion object {
        private fun occurrence(id: String, day: LocalDate, status: String) = PatrolOccurrenceDto(
            occurrenceId = id,
            routeId = "route-$id",
            routeName = id,
            scheduledAt = day.atTime(8, 0).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            earliestStartAt = day.atTime(7, 50).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            deadlineAt = day.atTime(11, 0).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            pointsExpected = 2,
            status = status,
            available = false
        )
    }
}
