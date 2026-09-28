package co.edu.unicauca.urbanguard.ui.features.visitors

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Reglas de fecha del selector de vigencia: el residente solo puede elegir
 * el día de hoy o un día posterior, y es el backend quien valida que el pase
 * no supere las 24 horas del día seleccionado.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal class TodayForwardSelectableDates : SelectableDates {

    private val todayUtcMillis: Long = LocalDate.now().toUtcMillis()

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtcMillis

    override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
}

internal fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun fromUtcMillis(utcMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
