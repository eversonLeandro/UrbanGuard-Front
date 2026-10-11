package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import javax.inject.Inject

/** Activa o desactiva un punto sin sacarlo de la ruta. */
class SetPatrolPointActiveUseCase @Inject constructor(
    private val repository: PatrolRepository
) {
    suspend operator fun invoke(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit> {
        return repository.setPointActive(complexId, pointId, isActive)
    }
}
