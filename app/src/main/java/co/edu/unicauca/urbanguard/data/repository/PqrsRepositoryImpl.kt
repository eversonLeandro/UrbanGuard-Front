package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.UpdatePqrsStatusRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PqrsApi
import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsSummary
import co.edu.unicauca.urbanguard.domain.repository.PqrsRepository
import javax.inject.Inject

class PqrsRepositoryImpl @Inject constructor(
    private val api: PqrsApi
) : PqrsRepository {
    override suspend fun getPqrs(
        complexId: String,
        status: PqrsStatus?,
        type: String?
    ): Result<List<PqrsSummary>> = apiCall {
        api.getPqrs(complexId, status?.name, type).map { it.toDomain() }
    }

    override suspend fun getPqrsDetail(
        complexId: String,
        pqrsId: String
    ): Result<PqrsDetail> = apiCall {
        api.getPqrsDetail(complexId, pqrsId).toDomain()
    }

    override suspend fun updateStatus(
        complexId: String,
        pqrsId: String,
        status: PqrsStatus,
        response: String
    ): Result<PqrsDetail> = apiCall {
        api.updatePqrsStatus(
            complexId,
            pqrsId,
            UpdatePqrsStatusRequestDto(status.name, response.trim())
        ).toDomain()
    }
}

