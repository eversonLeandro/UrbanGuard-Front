package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import javax.inject.Inject

class ReviewLinkRequestUseCase @Inject constructor(
    private val repository: LinkRequestRepository
) {
    suspend operator fun invoke(id: String, approve: Boolean): Result<LinkRequest> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de solicitud inválido"))
        }
        return repository.reviewRequest(id, approve)
    }
}
