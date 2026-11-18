package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCompliance

/** Detalle de cumplimiento de una ronda (nivel 3 del historial de patrullaje). */
interface PatrolComplianceRepository {

    /** Puntos NFC esperados vs registrados de una ocurrencia programada. */
    suspend fun getRoundCompliance(
        complexId: String,
        occurrenceId: String
    ): Result<PatrolRoundCompliance>
}
