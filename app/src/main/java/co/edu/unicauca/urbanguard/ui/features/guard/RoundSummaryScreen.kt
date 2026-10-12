package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import co.edu.unicauca.urbanguard.domain.model.PatrolRound
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorActionButton
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val summaryTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/**
 * Resumen de la ronda cerrada: puntos marcados con su hora, duración y
 * botón para volver.
 */
@Composable
fun RoundSummaryScreen(
    onBackClick: () -> Unit,
    viewModel: RoundSummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val round = uiState.round

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Ronda terminada",
            onNavigateBack = onBackClick
        )

        when {
            round == null -> SummaryError(
                message = uiState.errorMessage ?: "No hay resumen disponible.",
                onBackClick = onBackClick
            )
            else -> SummaryContent(
                uiState = uiState,
                round = round,
                onBackClick = onBackClick
            )
        }
    }
}

private fun durationLabel(round: PatrolRound): String {
    val end = round.finishedAt ?: return "—"
    val minutes = Duration.between(round.startedAt, end).toMinutes().coerceAtLeast(0)
    return if (minutes < 1) "Menos de un minuto" else "$minutes min"
}

@Composable
private fun SummaryContent(
    uiState: RoundSummaryUiState,
    round: PatrolRound,
    onBackClick: () -> Unit
) {
    val doneIds = round.donePointIds()
    val scans = round.scans.filter { it.known }
    val missingIds = uiState.pointOrder.filter { !doneIds.contains(it) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UgNavyGradient, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = round.routeName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${doneIds.size} puntos · ${durationLabel(round)}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        items(scans, key = { it.scannedAt.toString() + it.tagId }) { scan ->
            SummaryScanRow(
                pointName = scan.pointId?.let { uiState.pointNames[it] } ?: "Tag ${scan.tagId}",
                time = summaryTimeFormat.format(scan.scannedAt),
                done = true
            )
        }

        if (missingIds.isNotEmpty()) {
            item(key = "missing-title") {
                Text(
                    text = "No marcados (${missingIds.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgErrorText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(missingIds, key = { "missing-$it" }) { pointId ->
                SummaryScanRow(
                    pointName = uiState.pointNames[pointId] ?: "Punto",
                    time = "Sin marcar",
                    done = false
                )
            }
        }

        item(key = "back") {
            VisitorActionButton(text = "Volver", onClick = onBackClick)
        }
    }
}

@Composable
private fun SummaryScanRow(pointName: String, time: String, done: Boolean) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (done) UgEmerald else UgTextSecondary.copy(alpha = 0.25f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = pointName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = time,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (done) UgNavy else UgErrorText
            )
        }
    }
}

@Composable
private fun SummaryError(message: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = UgErrorText
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onBackClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Volver", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
