package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.ui.components.DateFilterField
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CARD_CORNER = 16.dp

/**
 * Tarjeta de un turno del historial: fecha, "X de Y cumplidas" y veredicto de
 * meta. Pulsarla abre el detalle.
 */
@Composable
fun ShiftSummaryCard(
    shift: PatrolShiftSummary,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(CARD_CORNER),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ShiftCardHeader(shift)
            Spacer(Modifier.height(12.dp))
            StatusBadge(
                text = PatrolHistoryFormat.goalLabel(
                    shift.goalMet,
                    shift.pointsScanned,
                    shift.pointsExpected
                ),
                tone = PatrolHistoryFormat.goalTone(shift.goalMet)
            )
        }
    }
}

@Composable
private fun ShiftCardHeader(shift: PatrolShiftSummary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(44.dp)
                .background(UgNavyGradient, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = PatrolHistoryFormat.shiftTitle(shift.shiftType, shift.date),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = UgNavy
            )
            Text(
                text = PatrolHistoryFormat.roundsCount(shift),
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }
        StatusBadge(
            text = PatrolHistoryFormat.shiftStatusLabel(shift.status),
            tone = PatrolHistoryFormat.shiftStatusTone(shift.status)
        )
    }
}

/**
 * Filtros de la lista: rango de fechas y "solo incumplimientos".
 *
 * [onFromClick]/[onToClick] abren el selector de fecha; la lista recarga al
 * cambiar el rango o el interruptor (el backend resuelve el filtrado).
 */
@Composable
fun HistoryFilters(
    from: LocalDate,
    to: LocalDate,
    onlyIncomplete: Boolean,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onOnlyIncompleteChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RangeField(
                label = "Desde",
                date = from,
                onClick = onFromClick,
                modifier = Modifier.weight(1f)
            )
            RangeField(
                label = "Hasta",
                date = to,
                onClick = onToClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = onlyIncomplete,
                onCheckedChange = onOnlyIncompleteChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = UgNavy)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Solo incumplimientos",
                fontSize = 13.sp,
                color = UgTextPrimary
            )
        }
    }
}

@Composable
private fun RangeField(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = UgTextSecondary)
        Spacer(Modifier.height(4.dp))
        DateFilterField(
            date = date,
            onShowDatePicker = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            dateFormat = DateTimeFormatter.ISO_LOCAL_DATE
        )
    }
}

@Composable
fun HistoryLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
fun HistoryEmpty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(72.dp)
                .background(UgInputBg, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = UgTextSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Sin patrullajes programados en este rango",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Ajusta las fechas o desactiva el filtro de incumplimientos.",
            fontSize = 13.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HistoryError(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetryClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy)
        ) {
            Text("Reintentar", fontWeight = FontWeight.Medium)
        }
    }
}
