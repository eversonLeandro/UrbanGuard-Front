package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/** Ronda abierta del conjunto, o null si no hay ninguna. */
class GetActiveRoundUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(complexId: String): PatrolRound? {
        return repository.activeRound(complexId)
    }
}
