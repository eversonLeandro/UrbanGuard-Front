package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import java.time.LocalDate

/**
 * Historial de patrullaje por turno (HU-034 / HU-032).
 *
 * Todo lo calcula el backend: el repositorio solo traduce el contrato. `from` y
 * `to` se envían siempre; los demás filtros solo cuando tienen valor.
 */
interface PatrolShiftRepository {

    /** Turnos del conjunto en el rango, en el orden que devuelve el backend. */
    suspend fun getShiftSummaries(
        complexId: String,
        from: LocalDate,
        to: LocalDate,
        guardId: String?,
        onlyIncomplete: Boolean,
        status: PatrolShiftStatus? = null
    ): Result<List<PatrolShiftSummary>>

    /** Detalle de un turno concreto (incluye el veredicto de meta del backend). */
    suspend fun getShiftDetail(
        complexId: String,
        shiftType: PatrolShiftType,
        date: LocalDate
    ): Result<PatrolShiftDetail>

    /** Resumen de desempeño de un vigilante en el rango. */
    suspend fun getGuardSummary(
        complexId: String,
        guardId: String,
        from: LocalDate,
        to: LocalDate
    ): Result<PatrolGuardSummary>
}
