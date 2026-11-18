package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftRound
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftIncident
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgIconMuted
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate

/**
 * Sección de rondas de una misma ruta dentro del detalle de un turno.
 *
 * La ruta es la unidad que el vigilante reconoce, así que las rondas se agrupan
 * siempre por ella (aunque tenga una sola ocurrencia): el turno puede repetir
 * una ruta varias veces y en la lista plana anterior esa relación se perdía.
 */
data class RoundRouteGroup(
    val routeName: String,
    val rounds: List<PatrolShiftRound>
)

/** Agrupa las rondas por ruta conservando el orden del backend. */
internal fun groupRoundsByRoute(rounds: List<PatrolShiftRound>): List<RoundRouteGroup> =
    rounds.groupBy { it.routeName }.map { (route, list) -> RoundRouteGroup(route, list) }

/** Encabezado de la ruta y sus rondas, en el orden entregado por el backend. */
@Composable
internal fun RouteGroupSection(
    group: RoundRouteGroup,
    shiftDate: LocalDate,
    onRoundClick: (String) -> Unit,
    onGuardClick: (String) -> Unit,
    onIncidentClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = group.routeName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = roundsWord(group.rounds.size),
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }
        Spacer(Modifier.height(2.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(UgInputBg)
        )
        Spacer(Modifier.height(6.dp))
        group.rounds.forEach { round ->
            ShiftRoundRow(
                round = round,
                shiftDate = shiftDate,
                onRoundClick = onRoundClick,
                onGuardClick = onGuardClick,
                onIncidentClick = onIncidentClick
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun roundsWord(count: Int): String =
    if (count == 1) "1 ronda" else "$count rondas"

/**
 * Fila de una ronda de la ruta. Si el backend entregó [PatrolShiftRound
 * .occurrenceId], se puede pulsar para abrir el detalle de puntos NFC; si no,
 * la fila se muestra solo informativa.
 */
@Composable
private fun ShiftRoundRow(
    round: PatrolShiftRound,
    shiftDate: LocalDate,
    onRoundClick: (String) -> Unit,
    onGuardClick: (String) -> Unit,
    onIncidentClick: (String) -> Unit
) {
    val clickable = round.occurrenceId != null
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickable) {
                    Modifier.clickable { round.occurrenceId?.let(onRoundClick) }
                } else {
                    Modifier
                }
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = PatrolRoundFormat.scheduledLine(round, shiftDate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = UgTextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = PatrolRoundFormat.startedLine(round, shiftDate),
                        fontSize = 12.sp,
                        color = UgTextSecondary
                    )
                }
                StatusBadge(
                    text = PatrolRoundFormat.roundStatusLabel(round.status, round.isUnscheduled),
                    tone = PatrolRoundFormat.roundStatusTone(round.status, round.isUnscheduled)
                )
            }
            Spacer(Modifier.height(6.dp))
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
            Spacer(Modifier.height(8.dp))
            GuardRow(
                round = round,
                clickable = clickable,
                onGuardClick = onGuardClick
            )
            round.incidents.forEach { incident ->
                Spacer(Modifier.height(8.dp))
                ShiftIncidentRow(incident = incident, onClick = onIncidentClick)
            }
        }
    }
}

@Composable
private fun ShiftIncidentRow(
    incident: PatrolShiftIncident,
    onClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(incident.reportId) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Novedad · ${PatrolRoundFormat.incidentCategoryLabel(incident.category)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = incident.description,
                fontSize = 12.sp,
                color = UgTextPrimary
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = buildString {
                    append(PatrolRoundDetailFormat.fullDateTimeOrDash(incident.reportedAt))
                    incident.guardName?.takeIf(String::isNotBlank)?.let {
                        append(" · ")
                        append(it)
                    }
                },
                fontSize = 11.sp,
                color = UgTextSecondary
            )
        }
    }
}

@Composable
private fun GuardRow(
    round: PatrolShiftRound,
    clickable: Boolean,
    onGuardClick: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = UgIconMuted,
            modifier = Modifier.width(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = guardText(round),
            fontSize = 12.sp,
            color = if (round.guard != null) UgEmerald else UgTextSecondary,
            fontWeight = if (round.guard != null) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier
                .weight(1f)
                .then(
                    round.guard?.let { guard ->
                        Modifier.clickable { onGuardClick(guard.id) }
                    } ?: Modifier
                )
        )
        if (clickable) {
            Text(
                text = "Ver puntos",
                fontSize = 12.sp,
                color = UgEmerald,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = UgIconMuted
            )
        }
    }
}

private fun guardText(round: PatrolShiftRound): String = when {
    round.guard != null -> round.guard.name
    round.startedAt != null -> "Vigilante no registrado"
    else -> "Sin vigilante"
}
