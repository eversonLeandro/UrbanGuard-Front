package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.ConjuntoSearch
import co.edu.unicauca.urbanguard.domain.repository.LinkByCodeRepository
import javax.inject.Inject

class SearchConjuntoUseCase @Inject constructor(
    private val repository: LinkByCodeRepository
) {
    suspend operator fun invoke(code: String): Result<ConjuntoSearch> {
        if (code.isBlank()) {
            return Result.failure(IllegalArgumentException("Ingresa el código del conjunto"))
        }
        if (code.length < 5) {
            return Result.failure(IllegalArgumentException("El código ingresado no es válido"))
        }
        return repository.searchConjunto(code.trim().uppercase())
    }
}
