package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkRequestRepository
import javax.inject.Inject

class GetLinkRequestsUseCase @Inject constructor(
    private val repository: LinkRequestRepository
) {
    suspend operator fun invoke(): Result<List<LinkRequest>> {
        return repository.getRequests()
    }
}
