package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolHistoryFilter
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import java.time.LocalDate

/**
 * Estado de la lista de turnos (HU-034): turnos del rango, filtros y su estado
 * funcional de carga/error. El backend entrega los turnos ya ordenados.
 *
 * [rangeError] es la validación del filtro (por ejemplo "Desde" posterior a
 * "Hasta"): cuando está presente no se consulta al backend y se muestra el
 * mensaje, en vez de alterar el rango elegido por el usuario.
 */
data class PatrolHistoryUiState(
    val filter: PatrolHistoryFilter,
    val isLoading: Boolean = false,
    val shifts: List<PatrolShiftSummary> = emptyList(),
    val errorMessage: String? = null,
    val rangeError: String? = null
) {
    val from: LocalDate get() = filter.from
    val to: LocalDate get() = filter.to
    val onlyIncomplete: Boolean get() = filter.onlyIncomplete

    val isEmpty: Boolean =
        shifts.isEmpty() && !isLoading && errorMessage == null && rangeError == null
}
