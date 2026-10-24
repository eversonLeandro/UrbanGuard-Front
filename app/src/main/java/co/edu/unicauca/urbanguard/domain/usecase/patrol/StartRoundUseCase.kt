package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import java.time.Instant
import javax.inject.Inject

/** Abre una ronda para la ruta indicada. */
class StartRoundUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(
        complexId: String,
        routeId: String,
        routeName: String,
        points: List<PatrolControlPoint>,
        scheduledAt: Instant?,
        dueAt: Instant?,
        occurrenceId: String? = null
    ): PatrolRound {
        return repository.startRound(
            complexId,
            routeId,
            routeName,
            points,
            scheduledAt,
            dueAt,
            occurrenceId
        )
    }
}
