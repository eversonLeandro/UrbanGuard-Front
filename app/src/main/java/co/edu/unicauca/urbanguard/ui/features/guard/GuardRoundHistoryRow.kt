package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.status
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.format.DateTimeFormatter
import java.time.ZoneId

private val historyDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault())

/**
 * Fila del historial de rondas con semáforo de estado.
 *
 * Las rondas terminadas (completada o incompleta) son clicables para volver
 * a consultar su resumen; las en curso no lo son (se continúan desde el
 * banner de ronda activa).
 *
 * Archivo propio para no superar el máximo de funciones de detekt en la
 * pantalla de entrada (y reutilizable en el futuro historial del admin).
 */
@Composable
fun RecentRoundRow(
    round: PatrolRound,
    onClick: (() -> Unit)? = null
) {
    val status = round.status()
    val badgeColor = when (status) {
        RoundStatus.IN_PROGRESS -> UgNavy
        RoundStatus.COMPLETED -> UgEmerald
        RoundStatus.INCOMPLETE -> Color(0xFFF57C00)
    }
    val badgeText = when (status) {
        RoundStatus.IN_PROGRESS -> "EN CURSO"
        RoundStatus.COMPLETED -> "COMPLETADA"
        RoundStatus.INCOMPLETE -> "INCOMPLETA"
    }
    val clickable = onClick != null && status != RoundStatus.IN_PROGRESS
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable { onClick?.invoke() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = UgNavy,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = round.routeName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = UgTextPrimary
                )
                Text(
                    text = roundHistoryLabel(round),
                    fontSize = 12.sp,
                    color = UgTextSecondary
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status == RoundStatus.COMPLETED) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                }
                Text(
                    text = badgeText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}

private fun roundHistoryLabel(round: PatrolRound): String {
    val date = historyDateFormat.format(round.startedAt)
    val done = round.donePointIds().size
    return if (round.totalPoints > 0) {
        "$date · $done/${round.totalPoints} puntos"
    } else {
        "$date · ${round.scans.size} lecturas"
    }
}
