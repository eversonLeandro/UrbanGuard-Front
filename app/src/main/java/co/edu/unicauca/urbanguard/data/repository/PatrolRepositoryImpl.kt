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
    private val api: PatrolApi
) : PatrolRepository {

    override suspend fun getPoints(complexId: String): Result<List<PatrolControlPoint>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val remote = api.getPoints(complexId).map { it.toDomain(complexId) }
            Result.success(remote.sortedForRoute())
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar checkpoints", e)
            Result.failure(Exception("Sin conexión al cargar puntos de control", e))
        }
    }

    override suspend fun savePoint(point: PatrolControlPoint): Result<PatrolControlPoint> {
        if (point.complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        val desired = point.normalized()
        return try {
            val saved = if (desired.id.isBlank()) {
                api.createPoint(desired.complexId, desired.toCreateDto())
                    .toDomain(desired.complexId)
            } else {
                updateRemote(desired)
            }
            Result.success(saved)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al guardar checkpoint", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun setPointActive(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit> {
        return try {
            val current = api.getPoint(complexId, pointId).toDomain(complexId)
            savePoint(current.copy(isActive = isActive)).map { }
        } catch (e: Exception) {
            Result.failure(Exception(PatrolConstants.MSG_SAVE_ERROR, e))
        }
    }

    private suspend fun updateRemote(desired: PatrolControlPoint): PatrolControlPoint {
        val current = api.getPoint(desired.complexId, desired.id).toDomain(desired.complexId)
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
