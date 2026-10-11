package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import java.time.DayOfWeek

/**
 * Estado del formulario de ruta (crear y editar).
 *
 * [availablePointIds] trae los puntos en orden de ronda para el selector;
 * [selectedIds] conserva el orden de la ronda al guardar.
 * [editingTimeIndex] abre el reloj para ese horario (null = cerrado).
 */
data class PatrolRouteFormUiState(
    val name: String = "",
    val availablePointIds: List<PatrolPointOption> = emptyList(),
    val selectedIds: List<String> = emptyList(),
    val schedules: List<PatrolSchedule> = emptyList(),
    val isActive: Boolean = true,
    val editingTimeIndex: Int? = null,
    val nameError: String? = null,
    val formError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

/** Punto disponible en el selector, con su posición en la ronda. */
data class PatrolPointOption(
    val id: String,
    val name: String,
    val order: Int
)

/** Días por defecto al crear la primera repetición: lunes a viernes. */
fun defaultWorkDays(): Set<DayOfWeek> =
    setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )
