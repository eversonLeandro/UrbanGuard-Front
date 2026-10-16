package co.edu.unicauca.urbanguard.domain.usecase.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.repository.PatrolShiftRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Detalle y veredicto de cumplimiento de un turno (HU-032).
 *
 * El cálculo de la meta vive en el backend: `PatrolShiftDetail` ya trae
 * `goalMet`, `pointsExpected` y `pointsScanned`. Este caso de uso solo pide ese
 * detalle; el frontend no compara escaneos contra la meta.
 */
class CheckShiftComplianceUseCase @Inject constructor(
    private val repository: PatrolShiftRepository
) {
    suspend operator fun invoke(
        complexId: String,
        shiftType: PatrolShiftType,
        date: LocalDate
    ): Result<PatrolShiftDetail> =
        repository.getShiftDetail(complexId, shiftType, date)
}
