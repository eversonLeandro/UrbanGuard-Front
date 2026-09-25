package co.edu.unicauca.urbanguard.core.common

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun formatDate(date: Date, pattern: String = "yyyy-MM-dd"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun formatFechaSolicitud(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val instant = try {
            Instant.parse(iso)
        } catch (e: DateTimeParseException) {
            try {
                Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(iso))
            } catch (e2: DateTimeParseException) {
                return iso
            }
        }
        return DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, h:mm a", Locale("es", "CO"))
            .withZone(ZoneId.systemDefault()).format(instant)
    }
}
