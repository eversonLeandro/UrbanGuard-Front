package co.edu.unicauca.urbanguard.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Modelos del historial de patrullaje por turno (HU-034 / HU-032).
 *
 * El frontend NO calcula turnos, cumplimiento ni meta: el backend entrega ya el
 * veredicto y estos modelos solo lo transportan con nombres legibles.
 *
 * Se evita reutilizar el enum [RoundStatus] de las rondas del guardia para no
 * mezclar el contrato del vigilante con el del administrador.
 */

/** Tipo de turno: día (06:00-18:00) o noche (18:00-06:00). */
enum class PatrolShiftType {
    DAY,
    NIGHT;

    companion object {
        fun fromWire(value: String?): PatrolShiftType? =
            values().firstOrNull { it.name == value }
    }
}

/** Estado del turno calculado por el backend. */
enum class PatrolShiftStatus {
    CLOSED,
    PENDING;

    companion object {
        fun fromWire(value: String?): PatrolShiftStatus? =
            values().firstOrNull { it.name == value }
    }
}

/** Estado de una ronda esperada dentro del turno. */
enum class PatrolRoundStatus {
    COMPLETED,
    INCOMPLETE,
    PENDING;

    companion object {
        fun fromWire(value: String?): PatrolRoundStatus? =
            values().firstOrNull { it.name == value }
    }
}

/** Motivo por el que una ronda quedó incompleta. */
enum class PatrolRoundReason {
    MISSING_POINTS,
    OUT_OF_SCHEDULE,
    NOT_PERFORMED;

    companion object {
        fun fromWire(value: String?): PatrolRoundReason? =
            values().firstOrNull { it.name == value }
    }
}

/**
 * Turno del listado con sus conteos y el veredicto de meta.
 *
 * [goalMet] es null mientras el turno está [PatrolShiftStatus.PENDING].
 */
data class PatrolShiftSummary(
    val shiftType: PatrolShiftType,
    val date: LocalDate,
    val status: PatrolShiftStatus,
    val expectedRounds: Int,
    val completedRounds: Int,
    val pointsExpected: Int,
    val pointsScanned: Int,
    val goalMet: Boolean?
)

/** Detalle de un turno: conteos, veredicto y sus rondas. */
data class PatrolShiftDetail(
    val status: PatrolShiftStatus,
    val expectedRounds: Int,
    val completedRounds: Int,
    val pointsExpected: Int,
    val pointsScanned: Int,
    val goalMet: Boolean?,
    val rounds: List<PatrolShiftRound>
)

/**
 * Una ronda dentro del detalle del turno.
 *
 * [scheduledAt] null indica una ronda NO programada: [status] y [reason] llegan
 * null y no suma en "X de Y" ni en la meta. [startedAt] null (y [guard] null)
 * significa que la ronda no se realizó.
 */
data class PatrolShiftRound(
    val routeName: String,
    val guard: PatrolGuardRef?,
    val scheduledAt: Instant?,
    val startedAt: Instant?,
    val status: PatrolRoundStatus?,
    val reason: PatrolRoundReason?,
    val pointsScanned: Int,
    val pointsExpected: Int
) {
    /** Ronda realizada sin horario programado (no cuenta para la meta). */
    val isUnscheduled: Boolean get() = scheduledAt == null
}

/** Referencia mínima al vigilante de una ronda. */
data class PatrolGuardRef(
    val id: String,
    val name: String
)

/** Resumen de desempeño de un vigilante en el rango consultado. */
data class PatrolGuardSummary(
    val guardName: String,
    val roundsPerformed: Int,
    val shiftsWithParticipation: Int,
    val averageRoundsPerShift: Double,
    val routes: List<String>
)
