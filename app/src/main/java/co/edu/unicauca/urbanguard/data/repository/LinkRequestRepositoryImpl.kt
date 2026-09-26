package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.LinkRequestApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import javax.inject.Inject

class LinkRequestRepositoryImpl @Inject constructor(
    private val api: LinkRequestApi
) : LinkRequestRepository {

    private companion object {
        const val REQUEST_NOT_FOUND_MESSAGE = "Esta solicitud de vinculación ya no existe o fue retirada."
    }

    override suspend fun getRequests(): Result<List<LinkRequest>> = try {
        val requests = listOf("PENDING", "APPROVED", "REJECTED")
            .flatMap { api.getLinkRequests(it) }
            .distinctBy { it.id }
            .map { it.toDomain() }
        Result.success(requests)
    } catch (e: Exception) {
        Result.failure(NetworkErrorMapper.toAppException(e))
    }

    override suspend fun getRequestById(id: String): Result<LinkRequest> = try {
        // El fallo se propaga: antes un error de red terminaba en una lista vacía y el
        // usuario veía "No se encontró la solicitud" aunque la solicitud sí existía.
        val request = getRequests().getOrElse { return Result.failure(it) }
            .firstOrNull { it.id == id }
            ?: return Result.failure(AppException(AppError.NotFound(REQUEST_NOT_FOUND_MESSAGE)))
        Result.success(request)
    } catch (e: Exception) {
        Result.failure(NetworkErrorMapper.toAppException(e))
    }

    override suspend fun reviewRequest(id: String, approve: Boolean): Result<LinkRequest> = try {
        if (approve) {
            Result.success(api.approveLinkRequest(id).toDomain())
        } else {
            // El endpoint de rechazo no devuelve la entidad actualizada, así que se
            // recupera antes de cambiarla. Si esa lectura falla, se informa el motivo real.
            val current = getRequests().getOrElse { return Result.failure(it) }
                .firstOrNull { it.id == id }
                ?: return Result.failure(AppException(AppError.NotFound(REQUEST_NOT_FOUND_MESSAGE)))
            api.rejectLinkRequest(id)
            Result.success(current.copy(estado = "REJECTED"))
        }
    } catch (e: Exception) {
        Result.failure(NetworkErrorMapper.toAppException(e))
    }
}
