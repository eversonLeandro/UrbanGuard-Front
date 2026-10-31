@file:Suppress("MaxLineLength")

package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.core.common.PqrsConstants
import co.edu.unicauca.urbanguard.domain.model.PqrsDetail
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
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
        bottomBar = { PqrsDetailBottomBar(state, viewModel) }
    ) { padding ->
        when {
            state.isLoading -> PqrsDetailLoading(padding)
            state.errorMessage != null -> PqrsDetailError(state.errorMessage.orEmpty(), padding, viewModel::load)
            state.detail != null -> {
                val detail = state.detail!!
                PqrsDetailContent(detail, state.successMessage, padding, viewModel)
            }
        }
    }

    if (state.isDialogOpen) {
        PqrsResponseDialog(state = state, viewModel = viewModel)
    }
}

@Composable
private fun PqrsDetailBottomBar(state: PqrsDetailUiState, viewModel: PqrsDetailViewModel) {
    if (state.canManage) {
        Button(
            onClick = viewModel::openDialog,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UgEmerald)
        ) { Text("Gestionar PQRS") }
    }
}

@Composable
private fun PqrsDetailLoading(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun PqrsDetailError(message: String, padding: PaddingValues, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = UgTextSecondary)
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun PqrsDetailContent(
    detail: PqrsDetail,
    successMessage: String?,
    padding: PaddingValues,
    viewModel: PqrsDetailViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().background(UgBackground).padding(padding)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (successMessage != null) {
            Text(successMessage, color = UgEmerald, fontWeight = FontWeight.Bold)
            LaunchedEffect(successMessage) { viewModel.consumeSuccessMessage() }
        }
        PqrsDetailSummary(detail)
        DetailCard("Asunto", Icons.Default.Description) {
            Text(detail.subject, color = UgNavy, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        DetailCard("Descripción", Icons.Default.Description) {
            Text(detail.description, color = UgNavy, lineHeight = 21.sp)
        }
        if (!detail.adminResponse.isNullOrBlank()) {
            DetailCard("Respuesta administrativa", Icons.Default.CheckCircle) {
                Text(detail.adminResponse.orEmpty(), color = UgNavy, lineHeight = 21.sp)
                HorizontalDivider(color = UgBorder)
                Text(detail.respondedByName.orEmpty(), color = UgTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(DateTimeUtils.formatFechaSolicitud(detail.respondedAt), color = UgTextSecondary, fontSize = 12.sp)
            }
        }
        DetailCard("Historial de gestión", Icons.Default.History) {
            detail.history.forEach { entry ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(UgEmerald, androidx.compose.foundation.shape.CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Text("${entry.fromStatus.spanishLabel} → ${entry.toStatus.spanishLabel}",
                            fontWeight = FontWeight.Bold, color = UgNavy)
                    }
                    Text(entry.response, color = UgNavy, modifier = Modifier.padding(start = 18.dp), lineHeight = 20.sp)
                    Text(
                        "${entry.respondedByName} · ${DateTimeUtils.formatFechaSolicitud(entry.createdAt)}",
                        color = UgTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 18.dp)
                    )
                }
            }
        }
        if (detail.status.isTerminal) {
            Text("Esta PQRS ya está cerrada.", color = UgTextSecondary)
        }
    }
}

@Composable
private fun PqrsDetailSummary(detail: PqrsDetail) {
            val statusUi = pqrsStatusUi(detail.status)
            androidx.compose.material3.Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = UgSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).background(UgEmerald.copy(alpha = 0.12f), RoundedCornerShape(15.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(pqrsTypeIcon(detail.type), contentDescription = null, tint = UgEmerald)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(detail.type.spanishLabel.uppercase(), color = UgEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(detail.subject, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = UgNavy)
                        }
                    }
                    HorizontalDivider(color = UgBorder)
                    DetailInfoRow(Icons.Default.Person, "Residente", detail.authorName)
                    DetailInfoRow(Icons.Default.CalendarToday, "Fecha de radicación", DateTimeUtils.formatFechaSolicitud(detail.createdAt))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Estado", color = UgTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        StatusBadge(statusUi.first, statusUi.second)
                    }
                }
            }
}


@Composable
private fun DetailCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    androidx.compose.material3.Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = UgSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = UgEmerald, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = UgNavy, fontSize = 15.sp)
            }
            content()
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = UgTextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = UgTextSecondary, fontSize = 11.sp)
            Text(value, color = UgNavy, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
