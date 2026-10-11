package co.edu.unicauca.urbanguard.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Ruta de patrullaje: un conjunto de puntos con sus horarios.
 *
 * La ruta referencia puntos por id en el orden de la ronda. Cada horario
 * dice a qué hora y qué días se camina; una ruta puede repetirse con varios
 * horarios (p. ej. la misma ruta a las 10:00 y a las 22:00).
 */
data class PatrolRoute(
    val id: String,
    val complexId: String,
    val name: String,
    val pointIds: List<String>,
    val schedules: List<PatrolSchedule>,
    val isActive: Boolean
)

/** Un horario de la ruta: hora y días en que se camina. */
data class PatrolSchedule(
    val time: LocalTime,
    val days: Set<DayOfWeek>
)

/** Resultado de validar el formulario de una ruta. */
enum class PatrolRouteValidation {
    VALID,
    NAME_REQUIRED,
    NAME_TOO_LONG,
    NO_POINTS,
    NO_SCHEDULES,
    SCHEDULE_WITHOUT_DAYS
}

private const val MAX_ROUTE_NAME_LENGTH = 100

/**
 * Valida el formulario: nombre, al menos un punto, al menos un horario y
 * cada horario con al menos un día.
 */
fun validatePatrolRoute(
    name: String,
    pointIds: List<String>,
    schedules: List<PatrolSchedule>
): PatrolRouteValidation {
    if (name.isBlank()) return PatrolRouteValidation.NAME_REQUIRED
    if (name.trim().length > MAX_ROUTE_NAME_LENGTH) {
        return PatrolRouteValidation.NAME_TOO_LONG
    }
    if (pointIds.isEmpty()) return PatrolRouteValidation.NO_POINTS
    if (schedules.isEmpty()) return PatrolRouteValidation.NO_SCHEDULES
    if (schedules.any { it.days.isEmpty() }) {
        return PatrolRouteValidation.SCHEDULE_WITHOUT_DAYS
    }
    return PatrolRouteValidation.VALID
}

/** Etiqueta corta del día para los chips (Lun…Dom). */
fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "Lun"
    DayOfWeek.TUESDAY -> "Mar"
    DayOfWeek.WEDNESDAY -> "Mié"
    DayOfWeek.THURSDAY -> "Jue"
    DayOfWeek.FRIDAY -> "Vie"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
}

/** Días ordenados de lunes a domingo (DayOfWeek es 1-7 ISO). */
fun weekDays(): List<DayOfWeek> =
    DayOfWeek.values().toList().sortedBy { it.value }

/**
 * Próximo horario de hoy aún no pasado, o null si ya pasaron todos (o no
 * hay horario hoy). Sirve la tarjeta "Ronda de hoy" del guardia.
 */
fun PatrolRoute.nextTimeToday(
    now: LocalTime = LocalTime.now(),
    day: DayOfWeek = LocalDate.now().dayOfWeek
): LocalTime? =
    schedules
        .filter { it.days.contains(day) }
        .map { it.time }
        .filter { !it.isBefore(now) }
        .minOrNull()

/**
 * Operaciones puras sobre los horarios en edición. Viven aquí (y no en el
 * ViewModel) para no superar el máximo de funciones por clase de detekt y
 * para poder testearse sin Android.
 */

/** Agrega una repetición vacía al final. */
fun List<PatrolSchedule>.withAddedSchedule(): List<PatrolSchedule> =
    this + PatrolSchedule(time = LocalTime.of(8, 0), days = emptySet())

/** Quita la repetición indicada (sin cambios si el índice no existe). */
fun List<PatrolSchedule>.withoutSchedule(index: Int): List<PatrolSchedule> =
    if (index in indices) toMutableList().also { it.removeAt(index) } else this

/** Fija la hora elegida en la repetición indicada. */
fun List<PatrolSchedule>.withPickedTime(
    index: Int,
    hour: Int,
    minute: Int
): List<PatrolSchedule> =
    if (index !in indices) this
    else toMutableList().also { it[index] = it[index].copy(time = LocalTime.of(hour, minute)) }

/** Marca o desmarca un día de la repetición indicada. */
fun List<PatrolSchedule>.withToggledDay(
    index: Int,
    day: DayOfWeek,
    selected: Boolean
): List<PatrolSchedule> {
    if (index !in indices) return this
    val days = this[index].days.toMutableSet()
    if (selected) days.add(day) else days.remove(day)
    return toMutableList().also { it[index] = it[index].copy(days = days) }
}

/** Ordena ids de puntos según el orden de la ronda. */
fun List<String>.sortedByPointOrder(orderById: Map<String, Int>): List<String> =
    sortedBy { orderById[it] ?: Int.MAX_VALUE }
