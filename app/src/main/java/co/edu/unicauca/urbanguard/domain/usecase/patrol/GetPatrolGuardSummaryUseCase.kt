package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resumen de desempeño de un vigilante en un rango (parte del flujo de HU-034).
 *
 * Todos los agregados los calcula el backend.
 */
class GetPatrolGuardSummaryUseCase @Inject constructor(
    private val repository: PatrolShiftRepository
) {
    suspend operator fun invoke(
        complexId: String,
        guardId: String,
        from: LocalDate,
        to: LocalDate
    ): Result<PatrolGuardSummary> =
        repository.getGuardSummary(complexId, guardId, from, to)
}
