package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundReason
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftRound
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Textos y tonos de las rondas dentro del detalle de un turno.
 *
 * Separado de [PatrolHistoryFormat] para no superar el máximo de funciones por
 * objeto de detekt y para poder testear la traducción sin Android.
 *
 * Las horas se muestran en formato de 12 horas ("08:45 a. m.") en la zona del
 * condominio. Como un turno nocturno cruza la medianoche, una hora de
 * madrugada (00:00–05:59) pertenece al día siguiente del turno: en ese caso la
 * línea muestra la fecha civil completa para que no quede ambigua.
 */
object PatrolRoundFormat {

    private val zone: ZoneId = PatrolHistoryConstants.CONDOMINIUM_ZONE
    private val locale: Locale = Locale.forLanguageTag("es-CO")

    /** Hora corta con designador ("08:45 a. m."), en la zona del condominio. */
    private val timeFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", locale).withZone(zone)

    /** Fecha y hora locales completas para rondas que cruzan medianoche. */
    private val fullDateTimeFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, hh:mm a", locale).withZone(zone)

    /** "3 de 4 puntos" de una ronda. */
    fun pointsLabel(scanned: Int, expected: Int): String = "$scanned de $expected puntos"

    fun incidentCategoryLabel(category: String): String = when (category) {
        "SECURITY" -> "Seguridad"
        "INFRASTRUCTURE" -> "Infraestructura"
        "LIGHTING" -> "Alumbrado"
        "CLEANING" -> "Aseo"
        "OTHER" -> "Otro"
        else -> category
    }

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

    /**
     * Hora programada de la ronda, con la fecha civil si cae fuera del día del
     * turno. "No programada" para una ronda sin horario.
     */
    fun scheduledLine(round: PatrolShiftRound, shiftDate: LocalDate): String =
        when (val instant = round.scheduledAt) {
            null -> "No programada"
            else -> "Programada ${labeledTime(instant, shiftDate)}"
        }

    /** Hora de inicio, o "No realizada" si la ronda no se llevó a cabo. */
    fun startedLine(round: PatrolShiftRound, shiftDate: LocalDate): String =
        when (val instant = round.startedAt) {
            null -> "No realizada"
            else -> "Inició ${labeledTime(instant, shiftDate)}"
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

    /** Hora local o fecha y hora completas si la ronda cae en otro día civil. */
    private fun labeledTime(instant: Instant, shiftDate: LocalDate): String {
        val civilDate = LocalDate.ofInstant(instant, zone)
        return if (civilDate != shiftDate) normalize(fullDateTimeFormat.format(instant))
        else shortTime(instant)
    }

    /** Hora con designador; normaliza el espacio estrecho del locale a espacio normal. */
    private fun shortTime(instant: Instant): String = normalize(timeFormat.format(instant))

    private fun normalize(value: String): String =
        value.replace('\u202f', ' ').replace('\u00a0', ' ')
}
