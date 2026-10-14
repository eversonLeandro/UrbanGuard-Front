package co.edu.unicauca.urbanguard.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Ocurrencias programadas: cada horario genera una cita por día.
 *
 * Reglas acordadas: la ronda puede iniciarse 30 min antes (`earlyMinutes`);
 * debe cerrarse antes de la siguiente cita o queda incompleta por tiempo;
 * sin cita cercana es ronda libre (sin vencimiento).
 */

/** Citas de la ruta en una fecha (solo días con horario). */
fun occurrenceDateTimes(
    route: PatrolRoute,
    date: LocalDate
): List<LocalDateTime> {
    if (!route.isActive) return emptyList()
    return route.schedules
        .filter { it.days.contains(date.dayOfWeek) }
        .map { LocalDateTime.of(date, it.time) }
        .sorted()
}

/**
 * Próxima cita estrictamente posterior a [after], en cualquier ruta activa
 * (busca hasta 8 días para cubrir la semana completa).
 */
fun nextOccurrenceAfter(
    routes: List<PatrolRoute>,
    after: LocalDateTime
): LocalDateTime? {
    var date = after.toLocalDate()
    repeat(8) {
        routes.forEach { route ->
            occurrenceDateTimes(route, date)
                .firstOrNull { it.isAfter(after) }
                ?.let { return it }
        }
        date = date.plusDays(1)
    }
    return null
}

/**
 * Cita que cubre un inicio a [now]: la última cuya ventana
 * (`cita − earlyMinutes`) ya abrió. Null si aún no abre ninguna (muy
 * temprano): entonces arranca como ronda libre.
 */
fun coveringOccurrence(
    route: PatrolRoute,
    now: LocalDateTime,
    earlyMinutes: Long
): LocalDateTime? {
    var date = now.toLocalDate().minusDays(1)
    var best: LocalDateTime? = null
    repeat(3) {
        occurrenceDateTimes(route, date)
            .filter { !it.minusMinutes(earlyMinutes).isAfter(now) }
            .maxOrNull()
            ?.let { candidate ->
                if (best == null || candidate.isAfter(best)) best = candidate
            }
        date = date.plusDays(1)
    }
    return best
}

/**
 * Desde qué hora de hoy se puede iniciar la ruta (primer horario menos la
 * ventana), o null si hoy no tiene horarios.
 */
fun PatrolRoute.availableFrom(
    today: LocalDate,
    earlyMinutes: Long
): LocalTime? =
    occurrenceDateTimes(this, today)
        .minOrNull()
        ?.toLocalTime()
        ?.minusMinutes(earlyMinutes)
