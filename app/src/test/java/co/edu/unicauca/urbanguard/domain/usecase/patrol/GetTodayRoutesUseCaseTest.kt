package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePatrolBatchRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolExecutionDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolOccurrenceDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi
import co.edu.unicauca.urbanguard.data.repository.PatrolRoundRepositoryImpl
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
    fun requestsOnlyTodayAndKeepsTheExistingStatusFilter() = runTest {
        val today = LocalDate.now(ZoneId.of("America/Bogota"))
        val api = FakePatrolExecutionApi(
            listOf(
                occurrence("today", today, "SCHEDULED"),
                occurrence("in-progress", today, "IN_PROGRESS"),
                occurrence("completed", today, "COMPLETED"),
                occurrence("not-performed", today, "NOT_PERFORMED"),
                occurrence("incomplete", today, "INCOMPLETE")
            )
        )
        val useCase = GetTodayRoutesUseCase(FakePatrolRouteRepository(), api)

        val result = useCase("complex-id").getOrThrow()

        assertEquals(
            listOf("today", "in-progress"),
            result.map { it.occurrenceId }
        )
        assertEquals(today.toString(), api.requestedFrom)
        assertEquals(today.toString(), api.requestedTo)
    }

    @Test
    fun keepsAllRoundsScheduledTodayInTimeOrder() = runTest {
        val today = LocalDate.now(ZoneId.of("America/Bogota"))
        val api = FakePatrolExecutionApi(
            listOf(
                occurrence("evening", today, "SCHEDULED", 18),
                occurrence("morning", today, "SCHEDULED", 9),
                occurrence("midday", today, "SCHEDULED", 12)
            )
        )

        val result = GetTodayRoutesUseCase(FakePatrolRouteRepository(), api)("complex-id").getOrThrow()

        assertEquals(listOf("morning", "midday", "evening"), result.map { it.occurrenceId })
        assertEquals(today.toString(), api.requestedFrom)
        assertEquals(today.toString(), api.requestedTo)
    }

    @Test
    fun emptyTodayStillLoadsYesterdayActiveRoundAndMyRoundsSeparately() = runTest {
        val today = LocalDate.now(ZoneId.of("America/Bogota"))
        val active = execution("active-yesterday", today.minusDays(1), "IN_PROGRESS")
        val completed = execution("completed-yesterday", today.minusDays(1), "COMPLETED")
            .copy(completedAt = today.minusDays(1).atTime(23, 50)
                .atZone(ZoneId.of("America/Bogota")).toInstant().toString())
        val api = FakePatrolExecutionApi(emptyList(), active, listOf(completed))
        val roundRepository = PatrolRoundRepositoryImpl(api)
        val entry = GetPatrolEntryUseCase(
            GetTodayRoutesUseCase(FakePatrolRouteRepository(), api),
            GetActiveRoundUseCase(roundRepository),
            GetMyRoundsUseCase(roundRepository),
            CloseOverdueRoundsUseCase(roundRepository)
        )("complex-id")

        assertEquals(emptyList<PatrolRoute>(), entry.todayRoutes)
        assertEquals("active-yesterday", entry.activeRound?.id)
        assertEquals(listOf("completed-yesterday"), entry.recentRounds.map { it.id })
        assertEquals(today.toString(), api.requestedFrom)
        assertEquals(today.toString(), api.requestedTo)
        assertEquals("complex-id", api.requestedActiveComplexId)
        assertEquals("complex-id", api.requestedMineComplexId)
    }

    private class FakePatrolExecutionApi(
        private val occurrences: List<PatrolOccurrenceDto>,
        private val activeExecution: PatrolExecutionDto? = null,
        private val myExecutions: List<PatrolExecutionDto> = emptyList()
    ) : PatrolExecutionApi {
        var requestedFrom: String? = null
        var requestedTo: String? = null
        var requestedActiveComplexId: String? = null
        var requestedMineComplexId: String? = null

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

        override suspend fun getActiveExecution(complexId: String): Response<PatrolExecutionDto> {
            requestedActiveComplexId = complexId
            return Response.success(activeExecution ?: error("Not used"))
        }

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

        override suspend fun getMyExecutions(complexId: String): List<PatrolExecutionDto> {
            requestedMineComplexId = complexId
            return myExecutions
        }

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
        private fun occurrence(id: String, day: LocalDate, status: String, hour: Int = 8) = PatrolOccurrenceDto(
            occurrenceId = id,
            routeId = "route-$id",
            routeName = id,
            scheduledAt = day.atTime(hour, 0).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            earliestStartAt = day.atTime(hour, 0).minusMinutes(10)
                .atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            deadlineAt = day.atTime(hour, 0).plusHours(3)
                .atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            pointsExpected = 2,
            status = status,
            available = false
        )

        private fun execution(id: String, day: LocalDate, status: String) = PatrolExecutionDto(
            executionId = id,
            occurrenceId = "occurrence-$id",
            routeId = "route-$id",
            routeName = id,
            scheduledAt = day.atTime(23, 30).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            earliestStartAt = day.atTime(23, 20).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            deadlineAt = day.plusDays(2).atTime(2, 0)
                .atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            startedAt = day.atTime(23, 25).atZone(ZoneId.of("America/Bogota")).toInstant().toString(),
            status = status,
            pointsCompleted = 0,
            pointsExpected = 1
        )
    }
}
