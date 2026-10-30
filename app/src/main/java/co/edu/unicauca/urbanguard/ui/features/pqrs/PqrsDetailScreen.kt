@file:Suppress("MaxLineLength")

package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.core.common.PqrsConstants
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun PqrsDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PqrsDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { UrbanGuardTopBar(title = "Detalle de PQRS", onNavigateBack = onNavigateBack) },
        bottomBar = {
            if (state.canManage) {
                Button(
                    onClick = viewModel::openDialog,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgEmerald)
                ) { Text("Gestionar PQRS") }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UgEmerald)
            }
            state.errorMessage != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.errorMessage.orEmpty(), color = UgTextSecondary)
                    TextButton(onClick = viewModel::load) { Text("Reintentar") }
                }
            }
            state.detail != null -> {
                val detail = state.detail!!
                Column(
                    modifier = Modifier.fillMaxSize().background(UgBackground).padding(padding)
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.successMessage != null) {
                        Text(state.successMessage.orEmpty(), color = UgEmerald, fontWeight = FontWeight.Bold)
                        LaunchedEffect(state.successMessage) { viewModel.consumeSuccessMessage() }
                    }
                    val statusUi = pqrsStatusUi(detail.status)
                    StatusBadge(statusUi.first, statusUi.second)
                    Text(detail.subject, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UgNavy)
                    Text("Radicada el ${DateTimeUtils.formatFechaSolicitud(detail.createdAt)}", color = UgTextSecondary, fontSize = 12.sp)
                    Text("Residente: ${detail.authorName}", color = UgTextSecondary)
                    DetailCard("Descripción") { Text(detail.description) }
                    if (!detail.adminResponse.isNullOrBlank()) {
                        DetailCard("Última respuesta administrativa") {
                            Text(detail.adminResponse.orEmpty())
                            Text(detail.respondedByName.orEmpty(), color = UgTextSecondary, fontSize = 12.sp)
                            Text(DateTimeUtils.formatFechaSolicitud(detail.respondedAt), color = UgTextSecondary, fontSize = 12.sp)
                        }
                    }
                    DetailCard("Historial") {
                        Text("Radicada", fontWeight = FontWeight.Bold)
                        detail.history.forEach { entry ->
                            Text("${entry.fromStatus.spanishLabel} → ${entry.toStatus.spanishLabel}", fontWeight = FontWeight.Bold)
                            Text(entry.response)
                            Text("${entry.respondedByName} · ${DateTimeUtils.formatFechaSolicitud(entry.createdAt)}", color = UgTextSecondary, fontSize = 12.sp)
                        }
                    }
                    if (detail.status.isTerminal) {
                        Text("Esta PQRS ya está cerrada.", color = UgTextSecondary)
                    }
                }
            }
        }
    }

    if (state.isDialogOpen) {
        PqrsResponseDialog(state = state, viewModel = viewModel)
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    androidx.compose.material3.Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = UgNavy)
            content()
        }
    }
}

@Composable
private fun PqrsResponseDialog(state: PqrsDetailUiState, viewModel: PqrsDetailViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissDialog,
        title = { Text("Gestionar PQRS", color = UgNavy, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusOption("En proceso", Icons.Default.Autorenew, PqrsStatus.IN_PROGRESS, state.targetStatus, viewModel::onTargetStatusChange)
                    StatusOption("Resuelta", Icons.Default.CheckCircle, PqrsStatus.RESOLVED, state.targetStatus, viewModel::onTargetStatusChange)
                }
                StatusOption("Rechazada", Icons.Default.Cancel, PqrsStatus.REJECTED, state.targetStatus, viewModel::onTargetStatusChange)
                OutlinedTextField(
                    value = state.response,
                    onValueChange = viewModel::onResponseChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text("Respuesta administrativa") },
                    supportingText = { Text("${state.response.length}/${PqrsConstants.RESPONSE_MAX_LENGTH}") }
                )
                if (!state.submitErrorMessage.isNullOrBlank()) {
                    Text(state.submitErrorMessage.orEmpty(), color = UgErrorText)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = if (state.targetStatus == PqrsStatus.REJECTED) UgErrorText else UgEmerald)
            ) {
                if (state.isSubmitting) CircularProgressIndicator(modifier = Modifier, color = Color.White)
                else Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = viewModel::dismissDialog) { Text("Cancelar") } }
    )
}

@Composable
private fun StatusOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    status: PqrsStatus,
    selected: PqrsStatus,
    onSelect: (PqrsStatus) -> Unit
) {
    val isSelected = status == selected
    TextButton(onClick = { onSelect(status) }) {
        Icon(icon, contentDescription = null, tint = if (isSelected) UgEmerald else UgTextSecondary)
        Text(label, color = if (isSelected) UgEmerald else UgTextSecondary)
    }
}

