package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCompliance
import co.edu.unicauca.urbanguard.domain.repository.PatrolComplianceRepository
import javax.inject.Inject

/**
 * Detalle de una ronda dentro de un turno (nivel 3 del historial de
 * patrullaje): puntos NFC esperados vs registrados. El backend entrega el
 * veredicto; este caso de uso solo delega.
 */
class GetRoundComplianceUseCase @Inject constructor(
    private val repository: PatrolComplianceRepository
) {
    suspend operator fun invoke(
        complexId: String,
        occurrenceId: String
    ): Result<PatrolRoundCompliance> =
        repository.getRoundCompliance(complexId, occurrenceId)
}
