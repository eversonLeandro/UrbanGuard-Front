package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.repository.PatrolRoundRepository
import javax.inject.Inject

/**
 * Cierra como vencidas las abiertas que pasaron su límite. Devuelve la más
 * reciente cerrada (para mostrar su resumen) o null.
 */
class CloseOverdueRoundsUseCase @Inject constructor(
    private val repository: PatrolRoundRepository
) {
    suspend operator fun invoke(complexId: String): PatrolRound? {
        return repository.closeOverdueRounds(complexId)
    }
}
