package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/**
 * Operaciones de la ronda activa (leer, registrar, cerrar).
 *
 * Agrupa los casos de uso para que el ViewModel no supere el máximo de
 * parámetros del constructor de detekt.
 */
class RoundActions @Inject constructor(
    private val getActiveRoundUseCase: GetActiveRoundUseCase,
    private val registerScanUseCase: RegisterScanUseCase,
    private val finishRoundUseCase: FinishRoundUseCase,
    private val closeOverdueRoundsUseCase: CloseOverdueRoundsUseCase
) {
    suspend fun activeRound(complexId: String): PatrolRound? =
        getActiveRoundUseCase(complexId)

    suspend fun registerScan(
        complexId: String,
        tagId: String
    ): Result<Pair<PatrolRound, PatrolControlPoint?>> =
        registerScanUseCase(complexId, tagId)

    suspend fun finish(complexId: String): Result<PatrolRound> =
        finishRoundUseCase(complexId)

    suspend fun closeOverdue(complexId: String): PatrolRound? =
        closeOverdueRoundsUseCase(complexId)
}
