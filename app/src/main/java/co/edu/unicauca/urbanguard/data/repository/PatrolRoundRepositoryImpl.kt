package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.CheckpointScanRequestDto
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.matchScan
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRoundRepositoryImpl @Inject constructor(
    private val executionApi: co.edu.unicauca.urbanguard.data.remote.api.service.PatrolExecutionApi
) : PatrolRoundRepository {

    private var lastFinished: PatrolRound? = null

    override suspend fun startRound(
        complexId: String,
        routeId: String,
        routeName: String,
        points: List<PatrolControlPoint>,
        scheduledAt: Instant?,
        dueAt: Instant?,
        occurrenceId: String?
    ): PatrolRound {
        // Si ya tenemos el occurrenceId de la tarjeta seleccionada, lo usamos directamente.
        // Solo buscamos si no se proporcionó (compatibilidad con modo local/legado).
        val targetOccurrenceId: String = if (!occurrenceId.isNullOrBlank()) {
            occurrenceId
        } else {
            val zone = ZoneId.of("America/Bogota")
            val today = LocalDate.now(zone)
            val from = today.minusDays(1).toString()
            val to   = today.plusDays(2).toString()
            val occurrences = executionApi.getAvailableOccurrences(
                complexId = complexId,
                from = from,
                to   = to
            )
            occurrences.firstOrNull { it.routeId == routeId && it.available }?.occurrenceId
                ?: occurrences.firstOrNull { it.routeId == routeId }?.occurrenceId
                ?: occurrences.firstOrNull { it.available }?.occurrenceId
                ?: error("No hay ocurrencias disponibles para iniciar en este momento.")
        }

        val executionDto = executionApi.startOccurrence(complexId, targetOccurrenceId)
        val domainRound = executionDto.toDomain(complexId).let { r ->
            if (r.points.isEmpty() || r.points.all { it.nfcTagId.isEmpty() }) {
                val pointsMap = points.associateBy { it.id }
                val enrichedPoints = if (r.points.isNotEmpty()) {
                    r.points.map { pt -> pointsMap[pt.id] ?: pt }
                } else points
                r.copy(points = enrichedPoints)
            } else r
        }
        return domainRound
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun activeRound(complexId: String): PatrolRound? {
        try {
            val response = executionApi.getActiveExecution(complexId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return body.toDomain(complexId)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error consultando ejecución activa remota", e)
        }
        return null
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun registerScan(
        complexId: String,
        tagId: String
    ): Result<Pair<PatrolRound, PatrolControlPoint?>> {
        val round = activeRound(complexId)
            ?: return Result.failure(Exception("No hay una ronda abierta."))
        val normalized = normalizeNfcUid(tagId)
        if (normalized.isEmpty()) {
            return Result.failure(Exception("El tag leído está vacío."))
        }

        // Intentar match local por nfcTagId (puede ser null si no se cargaron los tags)
        val localMatch = matchScan(normalized, round.points)

        return try {
            val requestDto = CheckpointScanRequestDto(uid = normalized)
            // IDs de puntos ya marcados ANTES del escaneo
            val doneBeforeScan = round.scans
                .filter { it.known }
                .mapNotNull { it.pointId }
                .toSet()

            val executionDto = executionApi.registerScan(
                complexId = complexId,
                executionId = round.id,
                idempotencyKey = UUID.randomUUID().toString(),
                request = requestDto
            )
            val updatedRemote = executionDto.toDomain(complexId).let { r ->
                if (r.points.isEmpty() || r.points.all { it.nfcTagId.isEmpty() }) {
                    r.copy(points = round.points)
                } else r
            }

            // Si no se pudo hacer match local, deducir el punto recién marcado
            // comparando el estado antes y después: el nuevo checkpoint con scannedAt
            // que NO estaba antes es el que se acaba de escanear.
            val resolvedMatch = localMatch ?: run {
                val newlyDoneId = updatedRemote.scans
                    .filter { it.known }
                    .mapNotNull { it.pointId }
                    .firstOrNull { it !in doneBeforeScan }
                newlyDoneId?.let { id -> updatedRemote.points.firstOrNull { it.id == id } }
            }

            Result.success(updatedRemote to resolvedMatch)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.w(TAG, "El backend rechazó el escaneo (${e.code()}): $errorBody")
            Result.failure(Exception("Error al registrar escaneo (${e.code()}): ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al registrar escaneo", e)
            Result.failure(e)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun finishRound(complexId: String): Result<PatrolRound> {
        val round = activeRound(complexId)
            ?: return Result.failure(Exception("No hay una ronda abierta."))

        return try {
            val executionDto = executionApi.finishExecution(complexId, round.id)
            val finishedRemote = executionDto.toDomain(complexId).let { r ->
                if (r.points.isEmpty() || r.points.all { it.nfcTagId.isEmpty() }) {
                    r.copy(points = round.points)
                } else r
            }
            lastFinished = finishedRemote
            Result.success(finishedRemote)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.w(TAG, "El backend rechazó finalizar la ronda (${e.code()}): $errorBody")
            Result.failure(Exception("Error al finalizar ronda (${e.code()}): ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al finalizar ronda", e)
            Result.failure(e)
        }
    }

    override suspend fun closeOverdueRounds(complexId: String): PatrolRound? {
        return null
    }

    override suspend fun getRounds(complexId: String): Result<List<PatrolRound>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return Result.success(emptyList())
    }

    override fun lastFinishedRound(): PatrolRound? = lastFinished

    private companion object {
        const val TAG = "PatrolRoundRepo"
    }
}
