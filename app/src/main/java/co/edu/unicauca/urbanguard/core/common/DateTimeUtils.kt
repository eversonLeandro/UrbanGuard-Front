package co.edu.unicauca.urbanguard.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun formatDate(date: Date, pattern: String = "yyyy-MM-dd"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }
}
