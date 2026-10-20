package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolLocalMigration
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolPointLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toCreateDto
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toPatchBody
import co.edu.unicauca.urbanguard.data.mapper.toResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.ReplaceCheckpointTagRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolApi
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.normalizeNfcUid
import co.edu.unicauca.urbanguard.domain.model.sortedForRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRepositoryImpl @Inject constructor(
    private val api: PatrolApi,
    private val localStore: PatrolPointLocalStore,
    private val migration: PatrolLocalMigration
) : PatrolRepository {

    override suspend fun getPoints(complexId: String): Result<List<PatrolControlPoint>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (!PatrolConstants.PATROL_POINTS_REMOTE) {
            return Result.success(localPoints(complexId))
        }
        return try {
            val remote = api.getPoints(complexId).map { it.toDomain(complexId) }
            localStore.save(complexId, remote.map { it.toResponseDto() })
            migration.migrate(complexId)
            Result.success(remote.sortedForRoute())
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar checkpoints; se usa caché local", e)
            Result.success(localPoints(complexId))
        }
    }

    override suspend fun savePoint(point: PatrolControlPoint): Result<PatrolControlPoint> {
        if (point.complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        val desired = point.normalized()
        val duplicate = localPoints(desired.complexId).firstOrNull {
            normalizeNfcUid(it.nfcTagId) == desired.nfcTagId && it.id != desired.id
        }
        if (duplicate != null) {
            return Result.failure(Exception(PatrolConstants.MSG_DUPLICATE_TAG))
        }
        if (!PatrolConstants.PATROL_POINTS_REMOTE) {
            return Result.success(saveLocal(desired))
        }
        return try {
            val saved = if (desired.id.isBlank()) {
                api.createPoint(desired.complexId, desired.toCreateDto())
                    .toDomain(desired.complexId)
            } else {
                updateRemote(desired)
            }
            cache(desired.complexId, saved)
            Result.success(saved)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al guardar checkpoint", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override suspend fun setPointActive(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit> {
        val current = localPoints(complexId).firstOrNull { it.id == pointId }
            ?: return Result.failure(Exception(PatrolConstants.MSG_SAVE_ERROR))
        return savePoint(current.copy(isActive = isActive)).map { }
    }

    private suspend fun updateRemote(desired: PatrolControlPoint): PatrolControlPoint {
        val current = localPoints(desired.complexId).firstOrNull { it.id == desired.id }
            ?: api.getPoint(desired.complexId, desired.id).toDomain(desired.complexId)
        val patch = desired.toPatchBody(current)
        var saved = current
        if (patch.size() > 0) {
            saved = api.updatePoint(desired.complexId, desired.id, patch)
                .toDomain(desired.complexId)
        }
        if (normalizeNfcUid(desired.nfcTagId) != normalizeNfcUid(current.nfcTagId)) {
            saved = api.replaceTag(
                desired.complexId,
                desired.id,
                ReplaceCheckpointTagRequestDto(uid = desired.nfcTagId)
            ).toDomain(desired.complexId)
        }
        return saved
    }

    private fun localPoints(complexId: String): List<PatrolControlPoint> =
        localStore.load(complexId).map { it.toDomain(complexId) }.sortedForRoute()

    private fun cache(complexId: String, point: PatrolControlPoint) {
        val current = localStore.load(complexId).toMutableList()
        val index = current.indexOfFirst { it.checkpointId == point.id }
        val dto = point.toResponseDto()
        if (index == -1) current.add(dto) else current[index] = dto
        localStore.save(complexId, current)
    }

    private fun saveLocal(point: PatrolControlPoint): PatrolControlPoint {
        val saved = if (point.id.isBlank()) {
            point.copy(id = UUID.randomUUID().toString())
        } else {
            point
        }
        cache(point.complexId, saved)
        return saved
    }

    private fun PatrolControlPoint.normalized(): PatrolControlPoint = copy(
        name = name.trim(),
        location = location.trim(),
        description = description.trim(),
        nfcTagId = normalizeNfcUid(nfcTagId)
    )

    private companion object {
        const val TAG = "PatrolRepo"
    }
}
