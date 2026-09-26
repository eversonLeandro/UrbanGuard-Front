package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import javax.inject.Inject

class GetLinkRequestDetailUseCase @Inject constructor(
    private val repository: LinkRequestRepository
) {
    suspend operator fun invoke(id: String): Result<LinkRequest> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de solicitud inválido"))
        }
        return repository.getRequestById(id)
    }
}
