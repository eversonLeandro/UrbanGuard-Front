package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import java.time.DayOfWeek

/**
 * Estado del formulario de ruta (crear y editar).
 *
 * [availablePointIds] trae los checkpoints para el selector; [selectedIds]
 * define el orden de la ronda y se guarda tal cual.
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

/** Punto disponible en el selector. */
data class PatrolPointOption(
    val id: String,
    val name: String
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
