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
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRouteRepositoryImpl @Inject constructor(
    private val api: PatrolApi
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
                api.createRoute(route.complexId, route.toRequestDto()).toDomain(route.complexId)
            } else {
                api.updateRoute(
                    route.complexId,
                    route.id,
                    route.toRequestDto()
                ).toDomain(route.complexId)
            }
            Result.success(saved)
        } catch (e: HttpException) {
            Result.failure(NetworkErrorMapper.toAppException(e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al guardar ruta", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
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
            Result.failure(NetworkErrorMapper.toAppException(e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al eliminar ruta", e)
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    private companion object {
        const val TAG = "PatrolRouteRepo"
    }
}
