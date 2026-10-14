package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Traducción de los turnos del historial: títulos, conteo y veredicto de meta.
 */
class PatrolHistoryFormatTest {

    @Test
    fun `titulo de turno de noche`() {
        assertEquals(
            "Noche del domingo 11",
            PatrolHistoryFormat.shiftTitle(PatrolShiftType.NIGHT, LocalDate.of(2026, 10, 11))
        )
    }

    @Test
    fun `titulo de turno de dia`() {
        assertEquals(
            "Día del lunes 12",
            PatrolHistoryFormat.shiftTitle(PatrolShiftType.DAY, LocalDate.of(2026, 10, 12))
        )
    }

    @Test
    fun `conteo de rondas`() {
        val summary = summary(completed = 2, expected = 3, goalMet = true)

        assertEquals("2 de 3 rondas cumplidas", PatrolHistoryFormat.roundsCount(summary))
    }

    @Test
    fun `meta cumplida explica en puntos`() {
        assertEquals(
            "Meta cumplida · 11 de 12 puntos",
            PatrolHistoryFormat.goalLabel(goalMet = true, pointsScanned = 11, pointsExpected = 12)
        )
        assertEquals(StatusTone.SUCCESS, PatrolHistoryFormat.goalTone(true))
    }

    @Test
    fun `meta no cumplida explica en puntos`() {
        assertEquals(
            "Meta no cumplida · 8 de 12 puntos",
            PatrolHistoryFormat.goalLabel(goalMet = false, pointsScanned = 8, pointsExpected = 12)
        )
        assertEquals(StatusTone.ERROR, PatrolHistoryFormat.goalTone(false))
    }

    @Test
    fun `sin veredicto se muestra pendiente`() {
        assertEquals(
            "Pendiente",
            PatrolHistoryFormat.goalLabel(goalMet = null, pointsScanned = 0, pointsExpected = 12)
        )
        assertEquals(StatusTone.WARNING, PatrolHistoryFormat.goalTone(null))
    }

    @Test
    fun `etiquetas y tonos de estado del turno`() {
        assertEquals("CERRADO", PatrolHistoryFormat.shiftStatusLabel(PatrolShiftStatus.CLOSED))
        assertEquals("PENDIENTE", PatrolHistoryFormat.shiftStatusLabel(PatrolShiftStatus.PENDING))
        assertEquals(StatusTone.SUCCESS, PatrolHistoryFormat.shiftStatusTone(PatrolShiftStatus.CLOSED))
        assertEquals(StatusTone.WARNING, PatrolHistoryFormat.shiftStatusTone(PatrolShiftStatus.PENDING))
    }

    private fun summary(completed: Int, expected: Int, goalMet: Boolean?) = PatrolShiftSummary(
        shiftType = PatrolShiftType.NIGHT,
        date = LocalDate.of(2026, 10, 11),
        status = PatrolShiftStatus.CLOSED,
        expectedRounds = expected,
        completedRounds = completed,
        pointsExpected = 12,
        pointsScanned = 11,
        goalMet = goalMet
    )
}
