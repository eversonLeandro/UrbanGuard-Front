package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Textos y tonos de los turnos del historial de patrullaje.
 *
 * Vive aparte de las pantallas para no superar el máximo de funciones por
 * archivo de detekt y para poder testear la traducción sin Android. Los textos
 * de las rondas dentro del turno están en [PatrolRoundFormat].
 */
object PatrolHistoryFormat {

    /** Día de la semana y día del mes ("domingo 11"). */
    private val titleFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE d", Locale("es", "CO"))

    /** Título del turno: "Noche del domingo 11". */
    fun shiftTitle(type: PatrolShiftType, date: LocalDate): String =
        "${shiftWord(type)} del ${titleFormat.format(date)}"

    private fun shiftWord(type: PatrolShiftType): String = when (type) {
        PatrolShiftType.DAY -> "Día"
        PatrolShiftType.NIGHT -> "Noche"
    }

    /** "2 de 3 rondas cumplidas". */
    fun roundsCount(summary: PatrolShiftSummary): String =
        "${summary.completedRounds} de ${summary.expectedRounds} rondas cumplidas"

    private fun completedOfExpected(done: Int, expected: Int, noun: String): String =
        "$done de $expected $noun"

    /** Etiqueta del estado del turno para el badge. */
    fun shiftStatusLabel(status: PatrolShiftStatus): String = when (status) {
        PatrolShiftStatus.CLOSED -> "CERRADO"
        PatrolShiftStatus.PENDING -> "PENDIENTE"
    }

    /** Tono del badge del estado del turno. */
    fun shiftStatusTone(status: PatrolShiftStatus): StatusTone = when (status) {
        PatrolShiftStatus.CLOSED -> StatusTone.SUCCESS
        PatrolShiftStatus.PENDING -> StatusTone.WARNING
    }

    /**
     * Línea de meta. Se explica en puntos porque la meta se mide solo por puntos
     * escaneados. Sin veredicto (turno pendiente) se muestra "Pendiente".
     */
    fun goalLabel(goalMet: Boolean?, pointsScanned: Int, pointsExpected: Int): String =
        when (goalMet) {
            true -> "Meta cumplida · ${completedOfExpected(pointsScanned, pointsExpected, "puntos")}"
            false ->
                "Meta no cumplida · ${completedOfExpected(pointsScanned, pointsExpected, "puntos")}"
            null -> "Pendiente"
        }

    /** Tono de la línea de meta. */
    fun goalTone(goalMet: Boolean?): StatusTone = when (goalMet) {
        true -> StatusTone.SUCCESS
        false -> StatusTone.ERROR
        null -> StatusTone.WARNING
    }
}
