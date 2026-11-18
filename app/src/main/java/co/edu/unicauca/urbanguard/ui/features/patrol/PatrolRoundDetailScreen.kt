package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Nfc
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
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCheckpoint
import co.edu.unicauca.urbanguard.domain.model.PatrolRoundCompliance
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Detalle de una ronda (nivel 3 del historial): horario, veredicto de
 * cumplimiento y puntos NFC esperados vs registrados.
 *
 * Los datos llegan calculados del backend (`patrol-compliance`); aquí solo se
 * traducen. Un punto sin `scannedAt` se muestra como "Sin escanear": no se
 * infiere ningún escaneo ausente.
 */
@Composable
fun PatrolRoundDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PatrolRoundDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Detalle de la ronda",
            onNavigateBack = onNavigateBack
        )

        RoundDetailContent(
            uiState = uiState,
            onRetry = viewModel::onRetry
        )
    }
}

@Composable
private fun RoundDetailContent(
    uiState: PatrolRoundDetailUiState,
    onRetry: () -> Unit
) {
    val error = uiState.errorMessage
    val detail = uiState.detail
    when {
        uiState.isLoading -> DetailLoading()
        error != null -> DetailError(message = error, onRetryClick = onRetry)
        detail == null -> DetailError(
            message = "No hay información de la ronda.",
            onRetryClick = onRetry
        )
        else -> RoundDetailBody(
            detail = detail,
            guardName = uiState.guardName
        )
    }
}

@Composable
private fun RoundDetailBody(
    detail: PatrolRoundCompliance,
    guardName: String?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") { RoundDetailHeader(detail = detail, guardName = guardName) }

        item(key = "points-title") {
            Text(
                text = "Puntos NFC esperados vs registrados",
                fontSize = 15.sp,
                color = UgNavy
            )
        }

        items(detail.checkpoints, key = { it.position }) { checkpoint ->
            CheckpointRow(checkpoint = checkpoint)
        }

        item(key = "points-bottom") { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun RoundDetailHeader(
    detail: PatrolRoundCompliance,
    guardName: String?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.routeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgNavy
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Puntos: ${detail.completedCheckpoints} de " +
                            "${detail.expectedCheckpoints}",
                        fontSize = 13.sp,
                        color = UgTextSecondary
                    )
                }
                StatusBadge(
                    text = PatrolRoundDetailFormat.statusLabel(detail.status),
                    tone = PatrolRoundDetailFormat.statusTone(detail.status)
                )
            }
            Spacer(Modifier.height(10.dp))
            StatusBadge(
                text = PatrolRoundDetailFormat.complianceLabel(detail.compliance),
                tone = PatrolRoundDetailFormat.complianceTone(detail.compliance)
            )
            Spacer(Modifier.height(12.dp))
            ScheduleLine(
                label = "Programada",
                value = PatrolRoundDetailFormat.fullDateTimeOrDash(detail.scheduledAt)
            )
            ScheduleLine(
                label = "Ventana",
                value = "${PatrolRoundDetailFormat.shortDateTimeOrDash(detail.earliestStartAt)} " +
                    "– ${PatrolRoundDetailFormat.shortDateTimeOrDash(detail.deadlineAt)}"
            )
            ScheduleLine(
                label = "Inició",
                value = PatrolRoundDetailFormat.fullDateTimeOrDash(detail.startedAt)
            )
            ScheduleLine(
                label = "Finalizó",
                value = PatrolRoundDetailFormat.fullDateTimeOrDash(detail.completedAt)
            )
            if (!guardName.isNullOrBlank()) {
                ScheduleLine(label = "Vigilante", value = guardName)
            }
        }
    }
}

@Composable
private fun ScheduleLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = UgTextSecondary,
            modifier = Modifier.weight(0.28f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = UgTextPrimary,
            modifier = Modifier.weight(0.72f)
        )
    }
}

@Composable
private fun CheckpointRow(checkpoint: PatrolRoundCheckpoint) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Nfc,
                    contentDescription = null,
                    tint = if (checkpoint.isScanned) UgEmerald else UgTextSecondary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${checkpoint.position}. ${checkpoint.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgTextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = PatrolRoundDetailFormat.checkpointScannedAt(checkpoint.scannedAt),
                    fontSize = 12.sp,
                    color = if (checkpoint.isScanned) UgTextSecondary else UgErrorText
                )
            }
        }
    }
}
