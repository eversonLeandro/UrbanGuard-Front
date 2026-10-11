package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolPointLocalStore
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolRoundLocalStore
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolRouteLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.StartRoundRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolRoundApi
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolScan
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.model.matchScan
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRoundRepositoryImpl @Inject constructor(
    private val api: PatrolRoundApi,
    private val localStore: PatrolRoundLocalStore,
    private val pointStore: PatrolPointLocalStore,
    private val routeStore: PatrolRouteLocalStore
) : PatrolRoundRepository {

    private var lastFinished: PatrolRound? = null

    override suspend fun startRound(
        complexId: String,
        routeId: String,
        routeName: String,
        totalPoints: Int
    ): PatrolRound {
        val round = PatrolRound(
            id = UUID.randomUUID().toString(),
            complexId = complexId,
            routeId = routeId,
            routeName = routeName,
            startedAt = Instant.now(),
            totalPoints = totalPoints
        )
        if (PatrolConstants.PATROL_BACKEND_ENABLED) {
            return try {
                api.startRound(complexId, StartRoundRequestDto(routeId))
                    .toDomain(complexId)
                    .also { persist(it) }
            } catch (e: IOException) {
                Log.w(TAG, "Sin conexión al abrir ronda; se guarda local", e)
                persist(round)
                round
            } catch (e: HttpException) {
                Log.w(TAG, "El backend rechazó abrir la ronda: ${e.code()}")
                persist(round)
                round
            }
        }
        persist(round)
        return round
    }

    override suspend fun activeRound(complexId: String): PatrolRound? =
        localRounds(complexId).firstOrNull { !it.isFinished() }

    override suspend fun registerScan(
        complexId: String,
        tagId: String
    ): Result<Pair<PatrolRound, PatrolControlPoint?>> {
        val round = activeRound(complexId)
            ?: return Result.failure(Exception("No hay una ronda abierta."))
        val normalized = tagId.trim().uppercase()
        if (normalized.isEmpty()) {
            return Result.failure(Exception("El tag leído está vacío."))
        }
        val points = pointStore.load(complexId).map { it.toDomain(complexId) }
        val match = matchScan(normalized, points)
        val scan = PatrolScan(
            pointId = match?.id,
            tagId = normalized,
            scannedAt = Instant.now(),
            known = match != null
        )
        val updated = round.copy(scans = round.scans + scan)
        persist(updated)
        if (PatrolConstants.PATROL_BACKEND_ENABLED) {
            sendScanBestEffort(complexId, updated.id, scan)
        }
        return Result.success(updated to match)
    }

    override suspend fun finishRound(complexId: String): Result<PatrolRound> {
        val round = activeRound(complexId)
            ?: return Result.failure(Exception("No hay una ronda abierta."))
        val finished = round.copy(finishedAt = Instant.now())
        persist(finished)
        lastFinished = finished
        if (PatrolConstants.PATROL_BACKEND_ENABLED) {
            finishRemoteBestEffort(complexId, finished.id)
        }
        return Result.success(finished)
    }

    override suspend fun getRounds(complexId: String): Result<List<PatrolRound>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (!PatrolConstants.PATROL_BACKEND_ENABLED) {
            return Result.success(localRounds(complexId))
        }
        return try {
            val remote = api.getRounds(complexId)
                .map { it.toDomain(complexId) }
                .map { backfillTotal(complexId, it, routeStore) }
            localStore.save(complexId, remote.map { it.toDto() })
            Result.success(remote.sortedByDescending { it.startedAt })
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar rondas; se usa caché local", e)
            Result.success(localRounds(complexId))
        }
    }

    override fun lastFinishedRound(): PatrolRound? = lastFinished

    private fun localRounds(complexId: String): List<PatrolRound> =
        localStore.load(complexId)
            .map { it.toDomain(complexId) }
            .map { backfillTotal(complexId, it, routeStore) }
            .sortedByDescending { it.startedAt }

    private fun persist(round: PatrolRound) {
        val current = localRounds(round.complexId).toMutableList()
        val index = current.indexOfFirst { it.id == round.id }
        if (index == -1) current.add(0, round) else current[index] = round
        localStore.save(round.complexId, current.map { it.toDto() })
    }

    private suspend fun sendScanBestEffort(complexId: String, roundId: String, scan: PatrolScan) {
        try {
            api.registerScan(
                complexId,
                roundId,
                RegisterScanRequestDto(
                    pointId = scan.pointId,
                    tagId = scan.tagId,
                    scannedAt = scan.scannedAt.toString()
                )
            )
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo enviar la lectura; queda local", e)
        } catch (e: HttpException) {
            Log.w(TAG, "El backend rechazó la lectura: ${e.code()}")
        }
    }

    private suspend fun finishRemoteBestEffort(complexId: String, roundId: String) {
        try {
            api.finishRound(complexId, roundId)
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo cerrar la ronda en el backend", e)
        } catch (e: HttpException) {
            Log.w(TAG, "El backend rechazó el cierre: ${e.code()}")
        }
    }

    private companion object {
        const val TAG = "PatrolRoundRepo"
    }
}

/**
 * Rondas cerradas de antes del contador: se comparan contra los puntos que
 * la ruta tiene hoy. Si la ruta ya no existe, quedan como estaban.
 *
 * Función de nivel superior para no superar el máximo de funciones por
 * clase de detekt.
 */
private fun backfillTotal(
    complexId: String,
    round: PatrolRound,
    routeStore: PatrolRouteLocalStore
): PatrolRound {
    if (!round.isFinished() || round.totalPoints > 0) return round
    val count = routeStore.load(complexId)
        .firstOrNull { it.id == round.routeId }
        ?.pointIds
        ?.size
        ?: return round
    return round.copy(totalPoints = count)
}
