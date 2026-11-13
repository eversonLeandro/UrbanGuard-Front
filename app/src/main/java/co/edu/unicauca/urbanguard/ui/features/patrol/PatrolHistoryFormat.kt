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
 *
 * Jerarquía visual (día → turno): el listado identifica cada día con fecha
 * completa y cada tarjeta con el tipo de turno y su ventana horaria.
 */
object PatrolHistoryFormat {

    private val locale: Locale = Locale.forLanguageTag("es-CO")

    /** Fecha completa en español de Colombia ("13 de enero de 2026"). */
    private val fullDateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", locale)

    /** Encabezado de sección por día civil en el listado. */
    fun dayHeader(date: LocalDate): String = fullDateFormat.format(date)

    /** Título del turno para el detalle ("Turno del 13 de enero de 2026"). */
    fun shiftTitle(type: PatrolShiftType, date: LocalDate): String =
        "Turno del ${fullDateFormat.format(date)}"

    /** Título de la tarjeta en el listado: franja horaria ("6:00 a. m. – 6:00 p. m."). */
    fun shiftCardTitle(type: PatrolShiftType): String = shiftWindow(type)

    /** Ventana horaria del turno, fija por tipo de negocio ("18:00 – 6:00 a. m."). */
    fun shiftWindow(type: PatrolShiftType): String = when (type) {
        PatrolShiftType.DAY -> "6:00 a. m. – 6:00 p. m."
        PatrolShiftType.NIGHT -> "18:00 – 6:00 a. m."
    }

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
