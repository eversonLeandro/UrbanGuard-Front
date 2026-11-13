package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Turnos del conjunto en un rango de fechas (lista de HU-034).
 *
 * El backend entrega la lista ya ordenada y con los conteos y el veredicto de
 * meta resueltos; el caso de uso solo delega.
 */
class GetPatrolHistoryUseCase @Inject constructor(
    private val repository: PatrolShiftRepository
) {
    suspend operator fun invoke(
        complexId: String,
        from: LocalDate,
        to: LocalDate,
        guardId: String?,
        onlyIncomplete: Boolean,
        status: PatrolShiftStatus?
    ): Result<List<PatrolShiftSummary>> =
        repository.getShiftSummaries(complexId, from, to, guardId, onlyIncomplete, status)
}
