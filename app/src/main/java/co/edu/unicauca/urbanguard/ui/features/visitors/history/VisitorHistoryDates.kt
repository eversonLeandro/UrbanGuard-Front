package co.edu.unicauca.urbanguard.ui.features.visitors.history

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object VisitorHistoryDates {
    private val locale = Locale.forLanguageTag("es-CO")
    val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    private val entryFormat = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", locale)
        .withZone(ZoneId.of("America/Bogota"))

    fun entry(instant: Instant): String = entryFormat.format(instant)

    fun range(from: LocalDate, to: LocalDate): String {
        val start = if (from.year == to.year && from.month == to.month) {
            from.dayOfMonth.toString()
        } else {
            dateFormat.format(from)
        }
        return "$start–${dateFormat.format(to)}"
    }
}
