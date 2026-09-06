package co.edu.unicauca.urbanguard.domain.usecase.onboarding

import co.edu.unicauca.urbanguard.domain.model.ConjuntoCode
import co.edu.unicauca.urbanguard.domain.repository.ConjuntoCodeRepository
import javax.inject.Inject

class GenerateConjuntoCodeUseCase @Inject constructor(
    private val repository: ConjuntoCodeRepository
) {
    suspend operator fun invoke(): Result<ConjuntoCode> {
        return repository.generateCode()
    }
}