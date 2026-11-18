package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolGuardRef
import co.edu.unicauca.urbanguard.domain.model.PatrolHistoryFilter
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import java.time.LocalDate

/**
 * Estado de la lista de turnos (HU-034): turnos del rango agrupados por día
 * civil, filtro de vigilante y estado funcional de carga/error.
 *
 * El backend entrega los turnos ya ordenados (fecha DESC, noche primero); el
 * agrupamiento por día es solo presentación y conserva ese orden.
 *
 * [rangeError] es la validación del filtro (por ejemplo "Desde" posterior a
 * "Hasta"): cuando está presente no se consulta al backend y se muestra el
 * mensaje, en vez de alterar el rango elegido por el usuario.
 *
 * [guards] alimenta el filtro de vigilante. [guardFilterError] informa si el
 * listado de vigilantes no pudo cargarse sin bloquear el historial.
 */
data class PatrolHistoryUiState(
    val filter: PatrolHistoryFilter,
    val isLoading: Boolean = false,
    val shifts: List<PatrolShiftSummary> = emptyList(),
    val guards: List<PatrolGuardRef> = emptyList(),
    val guardFilterError: String? = null,
    val errorMessage: String? = null,
    val rangeError: String? = null
) {
    val from: LocalDate get() = filter.from
    val to: LocalDate get() = filter.to
    val onlyIncomplete: Boolean get() = filter.onlyIncomplete
    val status: PatrolShiftStatus? get() = filter.status
    val selectedGuard: PatrolGuardRef?
        get() = guards.firstOrNull { it.id == filter.guardId }

    /** Días con turnos, en el orden que entrega el backend (fecha DESC). */
    val days: List<ShiftDayGroup> by lazy {
        shifts.groupBy { it.date }.map { (date, list) -> ShiftDayGroup(date, list) }
    }

    val isEmpty: Boolean =
        shifts.isEmpty() && !isLoading && errorMessage == null && rangeError == null
}

/** Un día civil del listado con sus turnos (hasta dos: día y noche). */
data class ShiftDayGroup(
    val date: LocalDate,
    val shifts: List<PatrolShiftSummary>
)
