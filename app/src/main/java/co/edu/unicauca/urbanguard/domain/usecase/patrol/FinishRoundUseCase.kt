package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/** Cierra la ronda abierta y la devuelve para el resumen. */
class FinishRoundUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(complexId: String): Result<PatrolRound> {
        return repository.finishRound(complexId)
    }
}
