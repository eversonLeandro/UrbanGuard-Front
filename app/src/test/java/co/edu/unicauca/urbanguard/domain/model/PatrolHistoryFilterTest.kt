package co.edu.unicauca.urbanguard.domain.model

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * El filtro del historial concentra las reglas de validez del rango. La UI no
 * debe reordenar ni recortar el rango elegido: si el filtro es inválido no se
 * consulta y se muestra el mensaje.
 */
class PatrolHistoryFilterTest {

    private val clock: Clock =
        Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `default entrega 7 dias incluyendo hoy`() {
        val filter = PatrolHistoryFilter.default(clock)

        assertEquals(LocalDate.of(2026, 10, 14), filter.to)
        assertEquals(LocalDate.of(2026, 10, 8), filter.from)
        assertNull(filter.validate())
    }

    @Test
    fun `default calcula el dia civil usando zona del condominio`() {
        val bogotaClock = Clock.fixed(
            Instant.parse("2026-01-14T04:30:00Z"),
            ZoneId.of("America/Bogota")
        )

        val filter = PatrolHistoryFilter.default(bogotaClock)

        assertEquals(LocalDate.of(2026, 1, 13), filter.to)
        assertEquals(LocalDate.of(2026, 1, 7), filter.from)
    }

    @Test
    fun `from posterior a to es invalido`() {
        val filter = PatrolHistoryFilter(
            from = LocalDate.of(2026, 10, 20),
            to = LocalDate.of(2026, 10, 10)
        )

        assertEquals(PatrolHistoryConstants.MSG_RANGE_INVALID, filter.validate())
    }

    @Test
    fun `un rango de 31 dias contando extremos es valido`() {
        val filter = PatrolHistoryFilter(
            from = LocalDate.of(2026, 1, 1),
            to = LocalDate.of(2026, 1, 31)
        )

        assertNull(filter.validate())
    }

    @Test
    fun `un rango de mas de 31 dias es invalido`() {
        val filter = PatrolHistoryFilter(
            from = LocalDate.of(2026, 1, 1),
            to = LocalDate.of(2026, 2, 1)
        )

        assertEquals(PatrolHistoryConstants.MSG_RANGE_TOO_LONG, filter.validate())
    }

    @Test
    fun `un mismo dia es valido`() {
        val filter = PatrolHistoryFilter(
            from = LocalDate.of(2026, 10, 14),
            to = LocalDate.of(2026, 10, 14)
        )

        assertNull(filter.validate())
    }
}
