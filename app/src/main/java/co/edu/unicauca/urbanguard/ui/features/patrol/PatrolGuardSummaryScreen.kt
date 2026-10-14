package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardSummary
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.util.Locale

/**
 * Resumen de desempeño de un vigilante (parte del flujo de HU-034).
 *
 * Todos los agregados llegan calculados del backend; aquí solo se muestran.
 */
@Composable
fun PatrolGuardSummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: PatrolGuardSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Resumen del vigilante",
            onNavigateBack = onNavigateBack
        )

        val error = uiState.errorMessage
        val summary = uiState.summary
        when {
            uiState.isLoading -> HistoryLoading()
            error != null -> HistoryError(message = error, onRetryClick = viewModel::onRetry)
            summary != null -> GuardSummaryCard(summary = summary)
            else -> HistoryError(
                message = "No hay información del vigilante.",
                onRetryClick = viewModel::onRetry
            )
        }
    }
}

@Composable
private fun GuardSummaryCard(summary: PatrolGuardSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = summary.guardName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )
            Spacer(Modifier.height(14.dp))

            if (summary.roundsPerformed == 0) {
                Text(
                    text = "Sin participación en este rango.",
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
                Spacer(Modifier.height(8.dp))
            }

            StatRow("Rondas realizadas", summary.roundsPerformed.toString())
            StatRow("Turnos con participación", summary.shiftsWithParticipation.toString())
            StatRow("Promedio de rondas por turno", formatAverage(summary.averageRoundsPerShift))

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Rutas atendidas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy
            )
            Spacer(Modifier.height(8.dp))
            if (summary.routes.isEmpty()) {
                Text(
                    text = "Sin rutas registradas en el rango.",
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
            } else {
                summary.routes.forEach { route -> RouteRow(route) }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = UgTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary
        )
    }
}

@Composable
private fun RouteRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = UgEmerald,
            modifier = Modifier.width(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = name, fontSize = 13.sp, color = UgTextPrimary)
    }
}

private fun formatAverage(value: Double): String =
    String.format(Locale("es", "CO"), "%.1f", value)
