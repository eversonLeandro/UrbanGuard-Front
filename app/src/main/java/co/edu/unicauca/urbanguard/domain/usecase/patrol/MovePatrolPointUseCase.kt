package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import javax.inject.Inject

/** Mueve un punto a una nueva posición de la ruta. */
class MovePatrolPointUseCase @Inject constructor(
    private val repository: PatrolRepository
) {
    suspend operator fun invoke(
        complexId: String,
        pointId: String,
        toOrder: Int
    ): Result<Unit> {
        return repository.movePoint(complexId, pointId, toOrder)
    }
}
