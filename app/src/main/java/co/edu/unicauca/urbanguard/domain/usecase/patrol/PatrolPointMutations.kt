package co.edu.unicauca.urbanguard.domain.usecase.patrol

import javax.inject.Inject

/**
 * Mutaciones de la lista de puntos.
 *
 * El orden pertenece a cada ruta y el backend no expone borrado de
 * checkpoints, así que solo queda activar/desactivar.
 */
class PatrolPointMutations @Inject constructor(
    private val setPatrolPointActiveUseCase: SetPatrolPointActiveUseCase
) {
    suspend fun setActive(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit> =
        setPatrolPointActiveUseCase(complexId, pointId, isActive)
}
