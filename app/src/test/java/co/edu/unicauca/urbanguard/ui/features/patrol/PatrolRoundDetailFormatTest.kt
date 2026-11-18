package co.edu.unicauca.urbanguard.ui.features.patrol

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PatrolRoundDetailFormatTest {

    @Test
    fun `convierte instante UTC a fecha y hora de Bogota en espanol`() {
        assertEquals(
            "13 de enero de 2026, 08:45 a. m.",
            PatrolRoundDetailFormat.fullDateTimeOrDash(Instant.parse("2026-01-13T13:45:00Z"))
        )
    }

    @Test
    fun `mantiene el dia local al cruzar medianoche UTC`() {
        assertEquals(
            "13 de enero de 2026, 11:30 p. m.",
            PatrolRoundDetailFormat.fullDateTimeOrDash(Instant.parse("2026-01-14T04:30:00Z"))
        )
    }

    @Test
    fun `punto sin escaneo se identifica explicitamente como omitido`() {
        assertEquals("Sin escanear", PatrolRoundDetailFormat.checkpointScannedAt(null))
    }
}
