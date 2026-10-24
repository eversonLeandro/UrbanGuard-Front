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
                syncOccurrencesForSchedules(route.complexId, created.id, route.schedules)
                created
            } else {
                runCatching {
                    val patchObj = com.google.gson.JsonObject().apply {
                        addProperty("active", route.isActive)
                    }
                    api.patchRoute(route.complexId, route.id, patchObj)
                }
                syncOccurrencesForSchedules(route.complexId, route.id, route.schedules)
                route
            }
            Result.success(saved)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al guardar ruta", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    /**
     * Sincroniza las ocurrencias programadas para la semana actual y la próxima
     * en el backend (zona Bogotá UTC-5).
     */
    private suspend fun syncOccurrencesForSchedules(
        complexId: String,
        routeId: String,
        schedules: List<co.edu.unicauca.urbanguard.domain.model.PatrolSchedule>
    ) {
        if (schedules.isEmpty()) return
        runCatching {
            val zone = ZoneId.of("America/Bogota")
            val today = LocalDate.now(zone)
            val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weeksToSchedule = listOf(currentMonday, currentMonday.plusWeeks(1))

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
                        entries.add(
                            BatchOccurrenceEntryDto(
                                routeId = routeId,
                                scheduledAt = offsetTime.toString()
                            )
                        )
                    }
                }
                if (entries.isNotEmpty()) {
                    executionApi.createBatch(
                        complexId = complexId,
                        request = CreatePatrolBatchRequestDto(
                            weekStartDate = weekMonday.toString(),
                            occurrences = entries
                        )
                    )
                }
            }
        }.onFailure { ex ->
            Log.w(TAG, "No se pudo sincronizar el lote de ocurrencias en el backend", ex)
        }
    }

    override suspend fun deleteRoute(complexId: String, routeId: String): Result<Unit> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            api.deleteRoute(complexId, routeId)
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
