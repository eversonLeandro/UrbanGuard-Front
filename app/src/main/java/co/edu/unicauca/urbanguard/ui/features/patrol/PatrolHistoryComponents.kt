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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftSummary
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private val CARD_CORNER = 16.dp

/**
 * Encabezado de sección por día civil: deja explícito que un día puede
 * contener dos turnos (día y noche) y evita repetir la fecha en cada tarjeta.
 */
@Composable
fun DaySectionHeader(date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgNavy
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(UgInputBg)
        )
    }
}

/**
 * Tarjeta de un turno: tipo con su ventana horaria, "X de Y cumplidas" y
 * veredicto de meta. La fecha vive en el encabezado del día ([DaySectionHeader]).
 * Pulsarla abre el detalle.
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
                imageVector = shiftIcon(shift.shiftType),
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = PatrolHistoryFormat.shiftCardTitle(shift.shiftType),
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

private fun shiftIcon(type: PatrolShiftType): ImageVector = when (type) {
    PatrolShiftType.DAY -> Icons.Outlined.LightMode
    PatrolShiftType.NIGHT -> Icons.Outlined.DarkMode
}

@Composable
fun HistoryLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

/**
 * Estado vacío que distingue por qué no hay resultados: sin turnos, sin
 * incumplimientos con el filtro activo o sin turnos del vigilante elegido.
 */
@Composable
fun HistoryEmpty(
    onlyIncomplete: Boolean,
    guardName: String?,
    status: PatrolShiftStatus? = null
) {
    val title = when {
        onlyIncomplete -> "Ningún turno cerrado con meta incumplida en este rango"
        status == PatrolShiftStatus.CLOSED -> "Sin turnos cerrados en este rango"
        status == PatrolShiftStatus.PENDING -> "Sin turnos pendientes en este rango"
        guardName != null -> "Sin turnos de $guardName en este rango"
        else -> "Sin patrullajes programados en este rango"
    }
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
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = UgTextSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Ajusta las fechas o revisa los filtros aplicados.",
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
