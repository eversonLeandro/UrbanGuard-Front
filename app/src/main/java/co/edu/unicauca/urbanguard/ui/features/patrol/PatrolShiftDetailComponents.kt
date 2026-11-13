package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.LocalDate

/**
 * Encabezado del detalle del turno: título con fecha completa, ventana horaria
 * del turno, "X de Y cumplidas" y el veredicto de meta. La ventana deja
 * explícito qué bloque del día representa el turno (día 6:00–18:00, noche
 * 18:00–6:00). Se explica que la meta se mide por puntos, para que no
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
            Spacer(Modifier.height(4.dp))
            Text(
                text = PatrolHistoryFormat.shiftWindow(shiftType),
                fontSize = 13.sp,
                color = UgTextSecondary
            )
            Spacer(Modifier.height(10.dp))
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
