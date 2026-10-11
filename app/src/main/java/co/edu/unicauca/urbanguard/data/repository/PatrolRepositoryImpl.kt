package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolPointLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toRequestDto
import co.edu.unicauca.urbanguard.data.mapper.toResponseDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.PatrolRouteOrderDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolApi
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.sortedForRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRepositoryImpl @Inject constructor(
    private val api: PatrolApi,
    private val localStore: PatrolPointLocalStore
) : PatrolRepository {

    override suspend fun getPoints(complexId: String): Result<List<PatrolControlPoint>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (!PatrolConstants.PATROL_BACKEND_ENABLED) {
            return Result.success(localPoints(complexId))
        }
        return try {
            val remote = api.getPoints(complexId).map { it.toDomain(complexId) }
            localStore.save(complexId, remote.map { it.toResponseDto() })
            Result.success(remote.sortedForRoute())
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar puntos; se usa caché local", e)
            Result.success(localPoints(complexId))
        }
    }

    override suspend fun savePoint(point: PatrolControlPoint): Result<PatrolControlPoint> {
        if (point.complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        val duplicate = localPoints(point.complexId).firstOrNull {
            it.nfcTagId.equals(point.nfcTagId, ignoreCase = true) && it.id != point.id
        }
        if (duplicate != null) {
            return Result.failure(Exception(PatrolConstants.MSG_DUPLICATE_TAG))
        }
        return try {
            val saved = if (!PatrolConstants.PATROL_BACKEND_ENABLED) {
                saveLocal(point)
            } else if (point.id.isBlank()) {
                api.createPoint(point.complexId, point.toRequestDto())
                    .toDomain(point.complexId)
                    .also { refreshLocal(point.complexId) }
            } else {
                api.updatePoint(point.complexId, point.id, point.toRequestDto())
                    .toDomain(point.complexId)
                    .also { refreshLocal(point.complexId) }
            }
            Result.success(saved)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al guardar punto", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override suspend fun deletePoint(complexId: String, pointId: String): Result<Unit> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            if (PatrolConstants.PATROL_BACKEND_ENABLED) {
                api.deletePoint(complexId, pointId)
                refreshLocal(complexId)
            } else {
                val remaining = localPoints(complexId).filterNot { it.id == pointId }
                persistOrdered(complexId, remaining)
            }
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al eliminar punto", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override suspend fun movePoint(
        complexId: String,
        pointId: String,
        toOrder: Int
    ): Result<Unit> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val ordered = localPoints(complexId).toMutableList()
            val from = ordered.indexOfFirst { it.id == pointId }
            if (from == -1) {
                return Result.failure(Exception(PatrolConstants.MSG_SAVE_ERROR))
            }
            val target = toOrder.coerceIn(0, ordered.lastIndex)
            val item = ordered.removeAt(from)
            ordered.add(target, item)
            persistOrdered(complexId, ordered)
            if (PatrolConstants.PATROL_BACKEND_ENABLED) {
                api.updateOrder(
                    complexId,
                    PatrolRouteOrderDto(ordered.map { it.id })
                )
            }
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al reordenar la ruta", e)
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

    private fun localPoints(complexId: String): List<PatrolControlPoint> =
        localStore.load(complexId).map { it.toDomain(complexId) }.sortedForRoute()

    private fun persistOrdered(complexId: String, points: List<PatrolControlPoint>) {
        val ordered = points.mapIndexed { index, point -> point.copy(order = index) }
        localStore.save(complexId, ordered.map { it.toResponseDto() })
    }

    private fun saveLocal(point: PatrolControlPoint): PatrolControlPoint {
        val current = localPoints(point.complexId).toMutableList()
        val saved = if (point.id.isBlank()) {
            point.copy(id = UUID.randomUUID().toString(), order = current.size)
                .also { current.add(it) }
        } else {
            point.also { updated ->
                val index = current.indexOfFirst { it.id == updated.id }
                if (index == -1) current.add(updated) else current[index] = updated
            }
        }
        persistOrdered(point.complexId, current)
        return saved
    }

    private suspend fun refreshLocal(complexId: String) {
        try {
            val remote = api.getPoints(complexId)
            localStore.save(complexId, remote)
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo refrescar la caché local", e)
        } catch (e: HttpException) {
            Log.w(TAG, "El backend rechazó el refresco de puntos: ${e.code()}")
        }
    }

    private companion object {
        const val TAG = "PatrolRepo"
    }
}
