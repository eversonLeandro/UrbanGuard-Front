package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.LinkRequestApi
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import javax.inject.Inject

class LinkRequestRepositoryImpl @Inject constructor(
    private val api: LinkRequestApi
) : LinkRequestRepository {

    override suspend fun getRequests(): Result<List<LinkRequest>> = try {
        val requests = listOf("PENDING", "APPROVED", "REJECTED")
            .flatMap { api.getLinkRequests(it) }
            .distinctBy { it.id }
            .map { it.toDomain() }
        Result.success(requests)
    } catch (e: Exception) {
        Result.failure(Exception(NetworkErrorMapper.map(e)))
    }

    override suspend fun getRequestById(id: String): Result<LinkRequest> = try {
        val request = getRequests().getOrElse { emptyList() }.firstOrNull { it.id == id }
            ?: return Result.failure(Exception("No se encontró la solicitud solicitada"))
        Result.success(request)
    } catch (e: Exception) {
        Result.failure(Exception(NetworkErrorMapper.map(e)))
    }

    override suspend fun reviewRequest(id: String, approve: Boolean): Result<LinkRequest> = try {
        val dto = if (approve) {
            api.approveLinkRequest(id)
        } else {
            val current = getRequests().getOrElse { emptyList() }.firstOrNull { it.id == id }
                ?: return Result.failure(Exception("No se encontró la solicitud solicitada"))
            api.rejectLinkRequest(id)
            return Result.success(current.copy(estado = "REJECTED"))
        }
        Result.success(dto.toDomain())
    } catch (e: Exception) {
        Result.failure(Exception(NetworkErrorMapper.map(e)))
    }
}
