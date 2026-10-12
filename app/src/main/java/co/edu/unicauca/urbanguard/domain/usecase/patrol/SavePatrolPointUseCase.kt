package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import javax.inject.Inject

/** Crea (id en blanco) o actualiza un punto de control. */
class SavePatrolPointUseCase @Inject constructor(
    private val repository: PatrolRepository
) {
    suspend operator fun invoke(point: PatrolControlPoint): Result<PatrolControlPoint> {
        return repository.savePoint(point)
    }
}
