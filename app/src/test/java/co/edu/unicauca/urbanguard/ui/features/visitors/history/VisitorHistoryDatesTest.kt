package co.edu.unicauca.urbanguard.ui.features.visitors.history

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone

class VisitorHistoryDatesTest {
    @Test
    fun `el ingreso cercano a medianoche conserva fecha civil de Bogota`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            val result = VisitorHistoryDates.entry(Instant.parse("2026-11-09T04:30:00Z"))
            assertEquals("8 nov 2026, 11:30 p. m.", result.replace('\u00a0', ' ').replace('\u202f', ' '))
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun `el rango resume el mismo mes e identifica ambos meses si difieren`() {
        val end = LocalDate.of(2026, 11, 9)
        assertEquals("1–9 nov 2026", VisitorHistoryDates.range(end.withDayOfMonth(1), end))
        assertEquals("31 oct 2026–9 nov 2026", VisitorHistoryDates.range(end.minusDays(9), end))
    }
}
