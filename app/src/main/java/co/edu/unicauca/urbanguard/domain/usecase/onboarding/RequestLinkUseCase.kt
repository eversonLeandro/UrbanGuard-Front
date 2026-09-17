package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.repository.LinkByCodeRepository
import javax.inject.Inject

class RequestLinkUseCase @Inject constructor(
    private val repository: LinkByCodeRepository
) {
    suspend operator fun invoke(conjuntoId: String, unidad: String): Result<LinkRequest> {
        return repository.requestLink(conjuntoId, unidad)
    }
}