package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolLocalMigration
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolRoundLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.RegisterScanRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.StartRoundRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolRoundApi
import co.edu.unicauca.urbanguard.domain.model.IncompleteReason
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.PatrolScan
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.isFinished
import co.edu.unicauca.urbanguard.domain.model.isOverdue
import co.edu.unicauca.urbanguard.domain.model.matchScan
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRoundRepositoryImpl @Inject constructor(
    private val api: PatrolRoundApi,
    private val localStore: PatrolRoundLocalStore,
    private val migration: PatrolLocalMigration
) : PatrolRoundRepository {

    private var lastFinished: PatrolRound? = null

    override suspend fun startRound(
        complexId: String,
        routeId: String,
        routeName: String,
        points: List<PatrolControlPoint>,
        scheduledAt: Instant?,
        dueAt: Instant?
    ): PatrolRound {
        val round = PatrolRound(
            id = UUID.randomUUID().toString(),
            complexId = complexId,
            routeId = routeId,
            routeName = routeName,
            startedAt = Instant.now(),
            points = points,
            scheduledAt = scheduledAt,
            dueAt = dueAt
        )
        if (PatrolConstants.PATROL_ROUNDS_REMOTE) {
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

    override suspend fun activeRound(complexId: String): PatrolRound? {
        migration.migrate(complexId)
        return localRounds(complexId).firstOrNull { !it.isFinished() }
    }

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
        val match = matchScan(normalized, round.points)
        val scan = PatrolScan(
            pointId = match?.id,
            tagId = normalized,
            scannedAt = Instant.now(),
            known = match != null
        )
        val updated = round.copy(scans = round.scans + scan)
        persist(updated)
        if (PatrolConstants.PATROL_ROUNDS_REMOTE) {
            syncBestEffort("enviar la lectura") {
                api.registerScan(
                    complexId,
                    updated.id,
                    RegisterScanRequestDto(
                        pointId = scan.pointId,
                        tagId = scan.tagId,
                        scannedAt = scan.scannedAt.toString()
                    )
                )
            }
        }
        return Result.success(updated to match)
    }

    override suspend fun finishRound(complexId: String): Result<PatrolRound> {
        val round = activeRound(complexId)
            ?: return Result.failure(Exception("No hay una ronda abierta."))
        val reason = if (round.totalPoints > 0 &&
            round.donePointIds().size < round.totalPoints
        ) {
            IncompleteReason.POINTS_MISSING
        } else {
            null
        }
        val finished = round.copy(finishedAt = Instant.now(), incompleteReason = reason)
        persist(finished)
        lastFinished = finished
        if (PatrolConstants.PATROL_ROUNDS_REMOTE) {
            syncBestEffort("cerrar la ronda en el backend") {
                api.finishRound(complexId, finished.id)
            }
        }
        return Result.success(finished)
    }

    override suspend fun closeOverdueRounds(complexId: String): PatrolRound? {
        val now = Instant.now()
        val overdue = localRounds(complexId).filter { it.isOverdue(now) }
        if (overdue.isEmpty()) return null
        var latest: PatrolRound? = null
        overdue.forEach { round ->
            val finished = round.copy(
                finishedAt = now,
                incompleteReason = IncompleteReason.TIME_OUT
            )
            persist(finished)
            if (latest == null || finished.startedAt.isAfter(latest.startedAt)) {
                latest = finished
            }
            if (PatrolConstants.PATROL_ROUNDS_REMOTE) {
                syncBestEffort("cerrar la ronda en el backend") {
                    api.finishRound(complexId, finished.id)
                }
            }
        }
        latest?.let { lastFinished = it }
        return latest
    }

    override suspend fun getRounds(complexId: String): Result<List<PatrolRound>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        migration.migrate(complexId)
        if (!PatrolConstants.PATROL_ROUNDS_REMOTE) {
            return Result.success(localRounds(complexId))
        }
        return try {
            val remote = api.getRounds(complexId)
                .map { it.toDomain(complexId) }
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
            .sortedByDescending { it.startedAt }

    private fun persist(round: PatrolRound) {
        val current = localRounds(round.complexId).toMutableList()
        val index = current.indexOfFirst { it.id == round.id }
        if (index == -1) current.add(0, round) else current[index] = round
        localStore.save(round.complexId, current.map { it.toDto() })
    }

    /**
     * Llamada remota que puede fallar sin romper el flujo local: registra el
     * aviso y sigue. [action] describe la acción en el mensaje de log.
     */
    private suspend fun syncBestEffort(action: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo $action; queda local", e)
        } catch (e: HttpException) {
            Log.w(TAG, "El backend rechazó la acción (${e.code()}): $action")
        }
    }

    private companion object {
        const val TAG = "PatrolRoundRepo"
    }
}
