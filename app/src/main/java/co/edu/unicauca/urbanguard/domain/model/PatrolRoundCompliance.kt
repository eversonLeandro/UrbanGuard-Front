package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant

/**
 * Modelos del detalle de cumplimiento de una ronda (nivel 3 del historial de
 * patrullaje: turno → ruta → ronda).
 *
 * Igual que en el resto del módulo, el backend entrega el veredicto (estado de
 * la ejecución y nivel de cumplimiento) y estos modelos solo lo transportan.
 * No se calcula nada en el frontend y no se infiere un escaneo ausente.
 */

/** Estado de ejecución de una ronda, calculado por el backend. */
enum class PatrolRoundExecutionStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    INCOMPLETE,
    NOT_PERFORMED;

    companion object {
        fun fromWire(value: String?): PatrolRoundExecutionStatus? =
            values().firstOrNull { it.name == value }
    }
}

/** Nivel de cumplimiento de la ronda según el backend. */
enum class PatrolComplianceLevel {
    COMPLIANT,
    PENDING,
    NON_COMPLIANT;

    companion object {
        fun fromWire(value: String?): PatrolComplianceLevel? =
            values().firstOrNull { it.name == value }
    }
}

/**
 * Un punto NFC del snapshot de la ronda: lo que se esperaba y cuándo se
 * escaneó. [scannedAt] null indica que el punto quedó omitido.
 */
data class PatrolRoundCheckpoint(
    val position: Int,
    val name: String,
    val scannedAt: Instant?
) {
    val isScanned: Boolean get() = scannedAt != null
}

/**
 * Detalle de una ronda: horario, veredicto y puntos NFC esperados vs
 * registrados. [scheduledAt] es obligatorio por contrato (toda ronda proviene
 * de una ocurrencia programada); [startedAt] null indica que no se inició.
 */
data class PatrolRoundCompliance(
    val routeName: String,
    val scheduledAt: Instant,
    val earliestStartAt: Instant?,
    val deadlineAt: Instant?,
    val status: PatrolRoundExecutionStatus,
    val compliance: PatrolComplianceLevel,
    val expectedCheckpoints: Int,
    val completedCheckpoints: Int,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val checkpoints: List<PatrolRoundCheckpoint>
)
