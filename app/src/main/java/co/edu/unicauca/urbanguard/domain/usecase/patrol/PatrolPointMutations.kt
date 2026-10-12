package co.edu.unicauca.urbanguard.domain.usecase.patrol

import javax.inject.Inject

/**
 * Mutaciones de la ruta desde la lista (eliminar, mover, activar).
 *
 * Agrupa los casos de uso para que el ViewModel no supere el máximo de
 * parámetros del constructor de detekt.
 */
class PatrolPointMutations @Inject constructor(
    private val deletePatrolPointUseCase: DeletePatrolPointUseCase,
    private val movePatrolPointUseCase: MovePatrolPointUseCase,
    private val setPatrolPointActiveUseCase: SetPatrolPointActiveUseCase
) {
    suspend fun delete(complexId: String, pointId: String): Result<Unit> =
        deletePatrolPointUseCase(complexId, pointId)

    suspend fun move(complexId: String, pointId: String, toOrder: Int): Result<Unit> =
        movePatrolPointUseCase(complexId, pointId, toOrder)

    suspend fun setActive(
        complexId: String,
        pointId: String,
        isActive: Boolean
    ): Result<Unit> =
        setPatrolPointActiveUseCase(complexId, pointId, isActive)
}
