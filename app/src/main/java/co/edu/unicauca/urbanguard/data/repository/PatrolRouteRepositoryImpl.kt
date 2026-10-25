package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolLocalMigration
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolRouteLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toDto
import co.edu.unicauca.urbanguard.data.mapper.toRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolApi
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import java.io.IOException
import co.edu.unicauca.urbanguard.data.remote.api.dto.BatchOccurrenceEntryDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePatrolBatchRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRouteScheduleSyncException(
    val savedRoute: PatrolRoute,
    cause: Throwable
) : Exception(
    "La ruta se guardó, pero no se pudieron programar sus rondas. " +
        "Reintenta guardar para sincronizar la programación.",
    cause
)

class PatrolRouteRepositoryImpl @Inject constructor(
    private val api: PatrolApi,
    private val executionApi: PatrolExecutionApi
) : PatrolRouteRepository {

    override suspend fun getRoutes(complexId: String): Result<List<PatrolRoute>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val remote = api.getRoutes(complexId).map { it.toDomain(complexId) }
            Result.success(remote)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar rutas", e)
            Result.failure(Exception("Sin conexión al cargar rutas", e))
        }
    }

    override suspend fun saveRoute(route: PatrolRoute): Result<PatrolRoute> {
        if (route.complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val saved = if (route.id.isBlank()) {
                val created = api.createRoute(route.complexId, route.toRequestDto())
                    .toDomain(route.complexId)
                if (created.isActive) {
                    syncOccurrencesForSavedRoute(created, route.schedules)
                }
                created
            } else {
                val updated = api.updateRoute(
                    route.complexId,
                    route.id,
                    route.toRequestDto()
                ).toDomain(route.complexId)
                if (updated.isActive) {
                    syncOccurrencesForSavedRoute(updated, route.schedules)
                }
                updated
            }
            Result.success(saved)
        } catch (e: PatrolRouteScheduleSyncException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al guardar ruta", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    private suspend fun syncOccurrencesForSavedRoute(
        savedRoute: PatrolRoute,
        schedules: List<co.edu.unicauca.urbanguard.domain.model.PatrolSchedule>
    ) {
        try {
            syncOccurrencesForSchedules(savedRoute.complexId, savedRoute.id, schedules)
        } catch (e: HttpException) {
            throw PatrolRouteScheduleSyncException(savedRoute, e)
        } catch (e: IOException) {
            throw PatrolRouteScheduleSyncException(savedRoute, e)
        }
    }

    /**
     * Sincroniza cuatro semanas de ocurrencias en la zona horaria del backend.
     */
    private suspend fun syncOccurrencesForSchedules(
        complexId: String,
        routeId: String,
        schedules: List<co.edu.unicauca.urbanguard.domain.model.PatrolSchedule>
    ) {
        val zone = ZoneId.of("America/Bogota")
        val today = LocalDate.now(zone)
        val now = java.time.OffsetDateTime.now(zone)
        val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weeksToSchedule = (0L..3L).map(currentMonday::plusWeeks)

        for (weekMonday in weeksToSchedule) {
            val entries = mutableListOf<BatchOccurrenceEntryDto>()
            for (schedule in schedules) {
                for (day in schedule.days) {
                    val daysToAdd = (day.value - DayOfWeek.MONDAY.value).toLong()
                    val occurrenceDate = weekMonday.plusDays(daysToAdd)
                    val offsetTime = java.time.OffsetDateTime.of(
                        occurrenceDate,
                        schedule.time,
                        zone.rules.getOffset(java.time.Instant.now())
                    )
                    if (!offsetTime.isBefore(now)) {
                        entries.add(
                            BatchOccurrenceEntryDto(
                                routeId = routeId,
                                scheduledAt = offsetTime.toString()
                            )
                        )
                    }
                }
            }
            executionApi.createBatch(
                complexId = complexId,
                request = CreatePatrolBatchRequestDto(
                    weekStartDate = weekMonday.toString(),
                    occurrences = entries,
                    routeIds = listOf(routeId)
                )
            )
        }
    }

    override suspend fun deleteRoute(complexId: String, routeId: String): Result<Unit> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val response = api.deleteRoute(complexId, routeId)
            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("No se pudo archivar la ruta (HTTP ${response.code()}).")
                )
            }
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al eliminar ruta", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    private companion object {
        const val TAG = "PatrolRouteRepo"
    }
}
