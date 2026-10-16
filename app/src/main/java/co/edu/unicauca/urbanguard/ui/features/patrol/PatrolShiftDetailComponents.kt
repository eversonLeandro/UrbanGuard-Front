package co.edu.unicauca.urbanguard.ui.features.patrol

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftDetail
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftRound
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgIconMuted
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate

/**
 * Encabezado del detalle del turno: título, "X de Y cumplidas" y el veredicto
 * de meta. Se explica en pantalla que la meta se mide por puntos, para que no
 * sorprenda ver "2 de 3 cumplidas" junto a "Meta cumplida".
 */
@Composable
fun ShiftDetailHeader(
    detail: PatrolShiftDetail,
    shiftType: PatrolShiftType,
    date: LocalDate
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = PatrolHistoryFormat.shiftTitle(shiftType, date),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${detail.completedRounds} de ${detail.expectedRounds} rondas cumplidas",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )
            Spacer(Modifier.height(10.dp))
            StatusBadge(
                text = PatrolHistoryFormat.goalLabel(
                    detail.goalMet,
                    detail.pointsScanned,
                    detail.pointsExpected
                ),
                tone = PatrolHistoryFormat.goalTone(detail.goalMet)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Una ronda cumplida exige horario y puntos; la meta mide " +
                    "solo los puntos escaneados.",
                fontSize = 11.5.sp,
                color = UgTextSecondary
            )
        }
    }
}

/**
 * Fila de una ronda del turno. Si tiene vigilante, se puede pulsar para abrir
 * su resumen de desempeño.
 */
@Composable
fun ShiftRoundRow(
    round: PatrolShiftRound,
    onGuardClick: (String) -> Unit
) {
    val guard = round.guard
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (guard != null) Modifier.clickable { onGuardClick(guard.id) } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            RoundHeader(round)
            Spacer(Modifier.height(8.dp))
            RoundDetails(round)
            Spacer(Modifier.height(10.dp))
            GuardRow(
                text = guardText(round),
                clickable = guard != null,
                onClick = { guard?.let { onGuardClick(it.id) } }
            )
        }
    }
}

@Composable
private fun RoundHeader(round: PatrolShiftRound) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = round.routeName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = roundTimeLine(round),
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }
        StatusBadge(
            text = PatrolRoundFormat.roundStatusLabel(round.status, round.isUnscheduled),
            tone = PatrolRoundFormat.roundStatusTone(round.status, round.isUnscheduled)
        )
    }
}

@Composable
private fun RoundDetails(round: PatrolShiftRound) {
    Text(
        text = PatrolRoundFormat.pointsLabel(round.pointsScanned, round.pointsExpected),
        fontSize = 12.sp,
        color = UgTextSecondary
    )
    val reason = PatrolRoundFormat.reasonLabel(
        round.reason,
        round.scheduledAt,
        round.startedAt
    )
    if (reason != null) {
        Spacer(Modifier.height(4.dp))
        Text(text = reason, fontSize = 12.sp, color = UgErrorText)
    }
    if (round.isUnscheduled) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Ronda no programada: no cuenta en el total ni en la meta.",
            fontSize = 11.5.sp,
            color = UgTextSecondary
        )
    }
}

private fun guardText(round: PatrolShiftRound): String = when {
    round.guard != null -> "${round.guard.name} · ver resumen"
    round.startedAt != null -> "Vigilante no registrado"
    else -> "Sin vigilante"
}

@Composable
private fun GuardRow(
    text: String,
    clickable: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.then(
            if (clickable) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = UgIconMuted,
            modifier = Modifier.width(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (clickable) UgEmerald else UgTextSecondary,
            fontWeight = if (clickable) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (clickable) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = UgIconMuted
            )
        }
    }
}

private fun roundTimeLine(round: PatrolShiftRound): String {
    val scheduled = if (round.isUnscheduled) {
        "No programada"
    } else {
        "Programada ${PatrolRoundFormat.timeOrDash(round.scheduledAt)}"
    }
    val started = round.startedAt?.let {
        "Inició ${PatrolRoundFormat.timeOrDash(it)}"
    } ?: "No realizada"
    return "$scheduled · $started"
}

@Composable
fun DetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
fun DetailError(message: String, onRetryClick: () -> Unit) {
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
