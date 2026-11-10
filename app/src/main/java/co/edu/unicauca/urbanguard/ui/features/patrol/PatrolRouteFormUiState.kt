package co.edu.unicauca.urbanguard.ui.features.patrol

import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Estado del formulario de ruta (crear y editar).
 *
 * [availablePointIds] trae los checkpoints para el selector; [selectedIds]
 * define el orden de la ronda y se guarda tal cual.
 * [selectedDays] aplica a todos los horarios de la ronda.
 * [editingTimeIndex] abre el reloj para ese horario (null = cerrado).
 */
data class PatrolRouteFormUiState(
    val name: String = "",
    val availablePointIds: List<PatrolPointOption> = emptyList(),
    val selectedIds: List<String> = emptyList(),
    val schedules: List<PatrolSchedule> = emptyList(),
    val selectedDays: Set<DayOfWeek> = defaultWorkDays(),
    val isActive: Boolean = true,
    val editingTimeIndex: Int? = null,
    val nameError: String? = null,
    val formError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val routeChangeBlockedMessage: String? = null
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

/** Primer horario al crear una ruta: hoy y diez minutos desde la hora local de Bogotá. */
fun defaultPatrolSchedule(
    now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Bogota"))
): PatrolSchedule {
    val scheduledAt = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(10)
    return PatrolSchedule(
        time = scheduledAt.toLocalTime(),
        days = defaultWorkDays() + scheduledAt.dayOfWeek
    )
}

/** Repite la selección de días de la ronda en todos sus horarios. */
fun List<PatrolSchedule>.withSharedDays(days: Set<DayOfWeek>): List<PatrolSchedule> =
    map { it.copy(days = days) }

/** Actualiza el conjunto compartido de días de la ronda. */
fun Set<DayOfWeek>.withToggledDay(day: DayOfWeek, selected: Boolean): Set<DayOfWeek> =
    toMutableSet().also { days ->
        if (selected) days.add(day) else days.remove(day)
    }
