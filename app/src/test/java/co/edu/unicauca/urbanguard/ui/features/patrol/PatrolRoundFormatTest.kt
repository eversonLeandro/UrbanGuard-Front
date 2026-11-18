package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolRoundReason
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundStatus
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftRound

/**
 * Traducción de las rondas del detalle: estado, puntos y motivo con desviación.
 */
class PatrolRoundFormatTest {

    private fun instant(value: String) = OffsetDateTime.parse(value).toInstant()

    @Test
    fun `ronda no programada usa etiqueta neutra`() {
        assertEquals("No programada", PatrolRoundFormat.roundStatusLabel(null, isUnscheduled = true))
        assertEquals(StatusTone.NEUTRAL, PatrolRoundFormat.roundStatusTone(null, isUnscheduled = true))
    }

    @Test
    fun `estado de ronda cumple`() {
        assertEquals(
            "CUMPLIDA",
            PatrolRoundFormat.roundStatusLabel(PatrolRoundStatus.COMPLETED, isUnscheduled = false)
        )
        assertEquals(
            StatusTone.SUCCESS,
            PatrolRoundFormat.roundStatusTone(PatrolRoundStatus.COMPLETED, isUnscheduled = false)
        )
    }

    @Test
    fun `motivo faltaron puntos`() {
        assertEquals(
            "Faltaron puntos",
            PatrolRoundFormat.reasonLabel(PatrolRoundReason.MISSING_POINTS, null, null)
        )
    }

    @Test
    fun `motivo no se realizo`() {
        assertEquals(
            "No se realizó",
            PatrolRoundFormat.reasonLabel(PatrolRoundReason.NOT_PERFORMED, null, null)
        )
    }

    @Test
    fun `motivo fuera de horario con desviacion despues`() {
        val scheduled = instant("2026-10-11T22:00:00-05:00")
        val started = instant("2026-10-11T22:05:00-05:00")

        assertEquals(
            "Se inició fuera de horario · 5 min después",
            PatrolRoundFormat.reasonLabel(PatrolRoundReason.OUT_OF_SCHEDULE, scheduled, started)
        )
    }

    @Test
    fun `motivo fuera de horario con desviacion antes`() {
        val scheduled = instant("2026-10-11T22:00:00-05:00")
        val started = instant("2026-10-11T21:50:00-05:00")

        assertEquals(
            "Se inició fuera de horario · 10 min antes",
            PatrolRoundFormat.reasonLabel(PatrolRoundReason.OUT_OF_SCHEDULE, scheduled, started)
        )
    }

    @Test
    fun `motivo fuera de horario sin horarios queda solo el texto base`() {
        assertEquals(
            "Se inició fuera de horario",
            PatrolRoundFormat.reasonLabel(PatrolRoundReason.OUT_OF_SCHEDULE, null, null)
        )
    }

    @Test
    fun `sin motivo no hay etiqueta`() {
        assertNull(PatrolRoundFormat.reasonLabel(null, null, null))
    }

    @Test
    fun `puntos de ronda`() {
        assertEquals("3 de 4 puntos", PatrolRoundFormat.pointsLabel(3, 4))
    }

    @Test
    fun `hora local incluye fecha larga cuando la ronda cruza medianoche`() {
        val round = PatrolShiftRound(
            routeName = "Ruta",
            guard = null,
            scheduledAt = instant("2026-01-14T05:30:00Z"),
            startedAt = null,
            status = PatrolRoundStatus.PENDING,
            reason = null,
            pointsScanned = 0,
            pointsExpected = 2
        )

        assertEquals(
            "Programada 14 de enero de 2026, 12:30 a. m.",
            PatrolRoundFormat.scheduledLine(round, LocalDate.of(2026, 1, 13))
        )
    }
}
