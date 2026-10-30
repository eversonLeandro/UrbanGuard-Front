package co.edu.unicauca.urbanguard.data.repository

import co.edu.unicauca.urbanguard.core.network.apiCall
import co.edu.unicauca.urbanguard.data.mapper.toResidentDomain
import co.edu.unicauca.urbanguard.data.remote.api.dto.CreatePqrsRequestDto
import co.edu.unicauca.urbanguard.data.remote.api.service.PqrsResidentApi
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDetail
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsDraft
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary
import co.edu.unicauca.urbanguard.domain.repository.ResidentPqrsRepository
import javax.inject.Inject

class ResidentPqrsRepositoryImpl @Inject constructor(
    private val api: PqrsResidentApi
) : ResidentPqrsRepository {
    override suspend fun create(
        complexId: String,
        idempotencyKey: String,
        draft: ResidentPqrsDraft
    ): Result<ResidentPqrsDetail> {
        if (complexId.isBlank() || idempotencyKey.isBlank()) return missingContext()
        return apiCall {
            api.create(
                complexId,
                idempotencyKey,
                CreatePqrsRequestDto(draft.type.name, draft.subject, draft.description)
            ).toResidentDomain()
        }
    }

    override suspend fun getMine(
        complexId: String,
        status: PqrsStatus?,
        type: PqrsType?
    ): Result<List<ResidentPqrsSummary>> {
        if (complexId.isBlank()) return missingContext()
        return apiCall { api.getMine(complexId, status?.name, type?.name).map { it.toResidentDomain() } }
    }

    override suspend fun getOwnDetail(complexId: String, pqrsId: String): Result<ResidentPqrsDetail> {
        if (complexId.isBlank() || pqrsId.isBlank()) return missingContext()
        return apiCall { api.getOwnDetail(complexId, pqrsId).toResidentDomain() }
    }

    private fun <T> missingContext(): Result<T> = Result.failure(
        AppException(AppError.Validation("No tienes un conjunto activo seleccionado."))
    )
}
