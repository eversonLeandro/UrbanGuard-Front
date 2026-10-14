package co.edu.unicauca.urbanguard.domain.model

import co.edu.unicauca.urbanguard.core.common.PatrolHistoryConstants
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Filtro de consulta del historial de patrullaje por turno.
 *
 * Concentra las reglas de validez del rango (HU-034) para que el ViewModel no
 * las replique y para poder testearlas sin Android. La UI **no** ajusta el
 * rango elegido por el usuario: si [validate] devuelve un mensaje, se muestra
 * en pantalla y no se llama al backend.
 */
data class PatrolHistoryFilter(
    val from: LocalDate,
    val to: LocalDate,
    val onlyIncomplete: Boolean = false,
    val guardId: String? = null
) {

    /**
     * Devuelve el mensaje de validación, o `null` si el rango es válido.
     *
     * El máximo se cuenta con ambos extremos incluidos (31 días = 30 de
     * diferencia), igual que lo hace el backend.
     */
    fun validate(): String? = when {
        from.isAfter(to) -> PatrolHistoryConstants.MSG_RANGE_INVALID
        daysBetween() > (PatrolHistoryConstants.MAX_RANGE_DAYS - 1) ->
            PatrolHistoryConstants.MSG_RANGE_TOO_LONG
        else -> null
    }

    private fun daysBetween(): Long = ChronoUnit.DAYS.between(from, to)

    companion object {
        /** Rango por defecto de 7 días (incluyendo hoy) según la zona/reloj dados. */
        fun default(clock: Clock): PatrolHistoryFilter {
            val to = LocalDate.now(clock)
            val from = to.minusDays((PatrolHistoryConstants.DEFAULT_RANGE_DAYS - 1).toLong())
            return PatrolHistoryFilter(from = from, to = to)
        }
    }
}
