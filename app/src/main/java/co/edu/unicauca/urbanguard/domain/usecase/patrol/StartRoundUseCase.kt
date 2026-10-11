package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/** Abre una ronda para la ruta indicada. */
class StartRoundUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(
        complexId: String,
        routeId: String,
        routeName: String,
        totalPoints: Int
    ): PatrolRound {
        return repository.startRound(complexId, routeId, routeName, totalPoints)
    }
}
