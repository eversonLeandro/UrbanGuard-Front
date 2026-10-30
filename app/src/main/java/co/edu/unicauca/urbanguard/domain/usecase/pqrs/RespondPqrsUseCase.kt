package co.edu.unicauca.urbanguard.domain.usecase.pqrs

import co.edu.unicauca.urbanguard.core.common.PqrsConstants
import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.repository.PqrsRepository
import javax.inject.Inject

class RespondPqrsUseCase @Inject constructor(private val repository: PqrsRepository) {
    suspend operator fun invoke(
        complexId: String,
        pqrsId: String,
        status: PqrsStatus,
        response: String
    ): Result<PqrsDetail> {
        if (response.trim().isBlank()) {
            return Result.failure(IllegalArgumentException(PqrsConstants.RESPONSE_REQUIRED))
        }
        if (response.trim().length > PqrsConstants.RESPONSE_MAX_LENGTH) {
            return Result.failure(IllegalArgumentException(PqrsConstants.RESPONSE_TOO_LONG))
        }
        return repository.updateStatus(complexId, pqrsId, status, response)
    }
}

