package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import javax.inject.Inject

/** Puntos de control del conjunto, ordenados para la ronda. */
class GetPatrolPointsUseCase @Inject constructor(
    private val repository: PatrolRepository
) {
    suspend operator fun invoke(complexId: String): Result<List<PatrolControlPoint>> {
        return repository.getPoints(complexId)
    }
}
