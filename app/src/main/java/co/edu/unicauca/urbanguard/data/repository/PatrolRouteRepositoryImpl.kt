package co.edu.unicauca.urbanguard.data.repository

import android.util.Log
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.local.patrol.PatrolRouteLocalStore
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.mapper.toDto
import co.edu.unicauca.urbanguard.data.mapper.toRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PatrolApi
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.repository.PatrolRouteRepository
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import retrofit2.HttpException

class PatrolRouteRepositoryImpl @Inject constructor(
    private val api: PatrolApi,
    private val localStore: PatrolRouteLocalStore
) : PatrolRouteRepository {

    override suspend fun getRoutes(complexId: String): Result<List<PatrolRoute>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (!PatrolConstants.PATROL_BACKEND_ENABLED) {
            return Result.success(localRoutes(complexId))
        }
        return try {
            val remote = api.getRoutes(complexId).map { it.toDomain(complexId) }
            localStore.save(complexId, remote.map { it.toDto() })
            Result.success(remote)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Log.w(TAG, "Sin conexión al cargar rutas; se usa caché local", e)
            Result.success(localRoutes(complexId))
        }
    }

    override suspend fun saveRoute(route: PatrolRoute): Result<PatrolRoute> {
        if (route.complexId.isBlank()) {
            return Result.failure(Exception(PatrolConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val saved = if (!PatrolConstants.PATROL_BACKEND_ENABLED) {
                saveLocal(route)
            } else if (route.id.isBlank()) {
                api.createRoute(route.complexId, route.toRequestDto())
                    .toDomain(route.complexId)
                    .also { refreshLocal(route.complexId) }
            } else {
                api.updateRoute(route.complexId, route.id, route.toRequestDto())
                    .toDomain(route.complexId)
                    .also { refreshLocal(route.complexId) }
            }
            Result.success(saved)
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
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
            if (PatrolConstants.PATROL_BACKEND_ENABLED) {
                api.deleteRoute(complexId, routeId)
                refreshLocal(complexId)
            } else {
                localStore.save(
                    complexId,
                    localRoutes(complexId).filterNot { it.id == routeId }.map { it.toDto() }
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

    private fun localRoutes(complexId: String): List<PatrolRoute> =
        localStore.load(complexId).map { it.toDomain(complexId) }

    private fun saveLocal(route: PatrolRoute): PatrolRoute {
        val current = localRoutes(route.complexId).toMutableList()
        val saved = if (route.id.isBlank()) {
            route.copy(id = UUID.randomUUID().toString()).also { current.add(it) }
        } else {
            route.also { updated ->
                val index = current.indexOfFirst { it.id == updated.id }
                if (index == -1) current.add(updated) else current[index] = updated
            }
        }
        localStore.save(route.complexId, current.map { it.toDto() })
        return saved
    }

    private suspend fun refreshLocal(complexId: String) {
        try {
            localStore.save(complexId, api.getRoutes(complexId))
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo refrescar la caché de rutas", e)
        } catch (e: HttpException) {
            Log.w(TAG, "El backend rechazó el refresco de rutas: ${e.code()}")
        }
    }

    private companion object {
        const val TAG = "PatrolRouteRepo"
    }
}
