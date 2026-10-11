package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.repository.PatrolRepository
import javax.inject.Inject

/** Elimina un punto de control y compacta la ruta. */
class DeletePatrolPointUseCase @Inject constructor(
    private val repository: PatrolRepository
) {
    suspend operator fun invoke(complexId: String, pointId: String): Result<Unit> {
        return repository.deletePoint(complexId, pointId)
    }
}
