package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

class RegenerateConjuntoCodeUseCase @Inject constructor(
    private val repository: ConjuntoCodeRepository
) {
    suspend operator fun invoke(conjuntoId: String): Result<ConjuntoCode> {
        // Misma lógica que generate, pero el backend invalida el código anterior.
        // Se separa en un UseCase propio para que la UI pueda distinguir
        // semánticamente "primera generación" de "regeneración con consecuencias".
        return repository.regenerateCode(conjuntoId)
    }
}
