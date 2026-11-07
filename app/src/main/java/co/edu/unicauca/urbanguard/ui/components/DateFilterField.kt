package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgDisabledText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DEFAULT_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "CO"))

/**
 * Campo de fecha compartido (Visitantes e Historial de patrullaje).
 *
 * Los valores por defecto preservan el comportamiento de la pantalla de
 * visitantes: ancho fijo de 140dp, alto 50dp, placeholder "Fecha", formato largo
 * en español y botón de limpiar que aparece solo cuando hay fecha.
 *
 * Para el rango del historial se usa [modifier] (ancho a elección del llamador),
 * un [label] opcional y se omite el limpiar pasando `onClearDate = null`.
 */
@Composable
fun DateFilterField(
    date: LocalDate?,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier.width(140.dp).height(50.dp),
    onClearDate: (() -> Unit)? = null,
    placeholder: String = "Fecha",
    dateFormat: DateTimeFormatter = DEFAULT_DATE_FORMAT
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize()
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(1.dp, UgBorder, RoundedCornerShape(10.dp))
                .clickable { onShowDatePicker() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = date?.format(dateFormat) ?: placeholder,
                fontSize = 13.sp,
                color = if (date != null) UgTextPrimary else UgDisabledText,
                modifier = Modifier.weight(1f)
            )
            if (date != null && onClearDate != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Limpiar fecha",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onClearDate() }
                        .padding(4.dp),
                    tint = UgTextSecondary
                )
            }
        }
    }
}

/**
 * Diálogo de selección de fecha compartido. [initialDate] nulo abre el mes actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDateMillis = initialDate
        ?.atStartOfDay(ZoneOffset.UTC)
        ?.toInstant()
        ?.toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) {
                Text("Aceptar", color = UgNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = UgTextSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
