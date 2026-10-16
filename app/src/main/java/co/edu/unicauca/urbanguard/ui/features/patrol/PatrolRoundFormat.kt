package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundReason
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundStatus
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Textos y tonos de las rondas dentro del detalle de un turno.
 *
 * Separado de [PatrolHistoryFormat] para no superar el máximo de funciones por
 * objeto de detekt y para poder testear la traducción sin Android.
 */
object PatrolRoundFormat {

    private val timeFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale("es", "CO"))
            .withZone(PatrolHistoryConstants.CONDOMINIUM_ZONE)

    /** "3 de 4 puntos" de una ronda. */
    fun pointsLabel(scanned: Int, expected: Int): String = "$scanned de $expected puntos"

    /** Etiqueta del estado de una ronda, o "No programada" si no tenía horario. */
    fun roundStatusLabel(status: PatrolRoundStatus?, isUnscheduled: Boolean): String = when {
        isUnscheduled -> "No programada"
        status == PatrolRoundStatus.COMPLETED -> "CUMPLIDA"
        status == PatrolRoundStatus.INCOMPLETE -> "INCOMPLETA"
        status == PatrolRoundStatus.PENDING -> "PENDIENTE"
        else -> "SIN ESTADO"
    }

    /** Tono del badge de una ronda. */
    fun roundStatusTone(status: PatrolRoundStatus?, isUnscheduled: Boolean): StatusTone = when {
        isUnscheduled -> StatusTone.NEUTRAL
        status == PatrolRoundStatus.COMPLETED -> StatusTone.SUCCESS
        status == PatrolRoundStatus.INCOMPLETE -> StatusTone.ERROR
        status == PatrolRoundStatus.PENDING -> StatusTone.WARNING
        else -> StatusTone.NEUTRAL
    }

    /**
     * Motivo de incumplimiento, o null si no aplica. En "fuera de horario" se
     * acompaña la desviación en minutos respecto del horario programado
     * (presentación, no cálculo de cumplimiento).
     */
    fun reasonLabel(
        reason: PatrolRoundReason?,
        scheduledAt: Instant?,
        startedAt: Instant?
    ): String? = when (reason) {
        PatrolRoundReason.MISSING_POINTS -> "Faltaron puntos"
        PatrolRoundReason.NOT_PERFORMED -> "No se realizó"
        PatrolRoundReason.OUT_OF_SCHEDULE -> outOfScheduleLabel(scheduledAt, startedAt)
        null -> null
    }

    private fun outOfScheduleLabel(scheduledAt: Instant?, startedAt: Instant?): String {
        val base = "Se inició fuera de horario"
        if (scheduledAt == null || startedAt == null) return base
        val minutes = ChronoUnit.MINUTES.between(scheduledAt, startedAt)
        return when {
            minutes < 0 -> "$base · ${-minutes} min antes"
            minutes > 0 -> "$base · $minutes min después"
            else -> base
        }
    }

    /** Hora programada o "—" si la ronda no tenía horario. */
    fun timeOrDash(instant: Instant?): String =
        instant?.let { timeFormat.format(it) } ?: "—"
}
