package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolEntry
import javax.inject.Inject

/**
 * Carga conjunta de la entrada de patrullaje: rutas de hoy, ronda abierta
 * y recientes. Un solo caso de uso para no superar el máximo de parámetros
 * del ViewModel.
 */
class GetPatrolEntryUseCase @Inject constructor(
    private val getTodayRoutesUseCase: GetTodayRoutesUseCase,
    private val getActiveRoundUseCase: GetActiveRoundUseCase,
    private val getMyRoundsUseCase: GetMyRoundsUseCase,
    private val closeOverdueRoundsUseCase: CloseOverdueRoundsUseCase
) {
    suspend operator fun invoke(complexId: String): PatrolEntry {
        closeOverdueRoundsUseCase(complexId)
        val today = getTodayRoutesUseCase(complexId).getOrThrow()
        val active = getActiveRoundUseCase(complexId)
        val recentResult = getMyRoundsUseCase(complexId)
        val recent = recentResult.getOrDefault(emptyList())
            .distinctBy { it.id }
            .take(5)
        return PatrolEntry(
            todayRoutes = today,
            activeRound = active,
            recentRounds = recent,
            recentRoundsError = recentResult.exceptionOrNull()?.message
        )
    }
}
