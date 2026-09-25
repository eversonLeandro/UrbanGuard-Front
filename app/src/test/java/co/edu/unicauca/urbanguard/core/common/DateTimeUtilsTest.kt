package co.edu.unicauca.urbanguard.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class DateTimeUtilsTest {

    @Test
    fun formatFechaSolicitud_formateaInstanteConZ() {
        val result = DateTimeUtils.formatFechaSolicitud("2026-09-24T20:42:00Z")
        // Se espera la fecha legible en hora local del dispositivo (AM/PM en es-CO).
        val local = java.time.Instant.parse("2026-09-24T20:42:00Z")
            .atZone(ZoneId.systemDefault())
        val expected = java.time.format.DateTimeFormatter
            .ofPattern("d 'de' MMMM 'de' yyyy, h:mm a", java.util.Locale("es", "CO"))
            .format(local)
        assertEquals(expected, result)
    }

    @Test
    fun formatFechaSolicitud_formateaInstanteConOffset() {
        val result = DateTimeUtils.formatFechaSolicitud("2026-09-24T15:45:30-05:00")
        val local = java.time.Instant.from(
            java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2026-09-24T15:45:30-05:00")
        ).atZone(ZoneId.systemDefault())
        val expected = java.time.format.DateTimeFormatter
            .ofPattern("d 'de' MMMM 'de' yyyy, h:mm a", java.util.Locale("es", "CO"))
            .format(local)
        assertEquals(expected, result)
    }

    @Test
    fun formatFechaSolicitud_devuelveVacioSiNullOBlank() {
        assertEquals("", DateTimeUtils.formatFechaSolicitud(null))
        assertEquals("", DateTimeUtils.formatFechaSolicitud(""))
        assertEquals("", DateTimeUtils.formatFechaSolicitud("   "))
    }

    @Test
    fun formatFechaSolicitud_devuelveOriginalSiNoParseable() {
        assertEquals("texto-no-fecha", DateTimeUtils.formatFechaSolicitud("texto-no-fecha"))
        assertEquals("2026/09/24", DateTimeUtils.formatFechaSolicitud("2026/09/24"))
    }
}