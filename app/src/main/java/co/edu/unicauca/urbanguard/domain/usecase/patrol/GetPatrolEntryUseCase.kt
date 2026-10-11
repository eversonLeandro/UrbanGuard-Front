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
    private val getMyRoundsUseCase: GetMyRoundsUseCase
) {
    suspend operator fun invoke(complexId: String): PatrolEntry {
        val today = getTodayRoutesUseCase(complexId).getOrDefault(emptyList())
        val active = runCatching { getActiveRoundUseCase(complexId) }.getOrNull()
        val recent = getMyRoundsUseCase(complexId)
            .getOrDefault(emptyList())
            .distinctBy { it.id }
            .take(5)
        return PatrolEntry(
            todayRoutes = today,
            activeRound = active,
            recentRounds = recent
        )
    }
}
