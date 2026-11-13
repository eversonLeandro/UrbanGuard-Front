package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.RoundStatus
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.status
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgAmberSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val finishTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault())

/**
 * Confirmación inequívoca al cerrar una ronda: check de éxito (o bandera
 * ámbar si quedó incompleta), nombre de la ronda, hora de finalización y
 * resultado del recorrido. Cerrar regresa a la lista de rondas.
 */
@Composable
fun RoundFinishConfirmDialog(
    round: PatrolRound,
    onViewDetail: () -> Unit,
    onBackToRounds: () -> Unit
) {
    val completed = round.status() == RoundStatus.COMPLETED
    val done = round.donePointIds().size

    AlertDialog(
        onDismissRequest = onViewDetail,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (completed) UgEmerald.copy(alpha = 0.15f) else UgAmberSoft,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (completed) Icons.Default.Check else Icons.Default.Flag,
                        contentDescription = null,
                        tint = if (completed) UgEmerald else UgAmber,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (completed) {
                        "¡Ronda finalizada exitosamente!"
                    } else {
                        "Ronda finalizada (incompleta)"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (completed) UgEmerald else UgAmber
                )
                Spacer(Modifier.height(14.dp))
                SummaryInfoRow("Ronda", round.routeName)
                round.finishedAt?.let {
                    SummaryInfoRow("Finalizada", finishTimeFormat.format(it))
                }
                SummaryInfoRow("Resultado", "$done de ${round.totalPoints} puntos")
                if (!completed) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "La ronda quedó registrada como incompleta.",
                        fontSize = 12.sp,
                        color = UgTextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBackToRounds,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Volver a rondas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onViewDetail,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ver detalle",
                    fontWeight = FontWeight.SemiBold,
                    color = UgTextSecondary
                )
            }
        }
    )
}

@Composable
private fun SummaryInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = UgTextSecondary
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgTextPrimary,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
