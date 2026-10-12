package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/** Rondas del conjunto, más recientes primero ("Mis rondas"). */
class GetMyRoundsUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(complexId: String): Result<List<PatrolRound>> {
        return repository.getRounds(complexId)
    }
}
