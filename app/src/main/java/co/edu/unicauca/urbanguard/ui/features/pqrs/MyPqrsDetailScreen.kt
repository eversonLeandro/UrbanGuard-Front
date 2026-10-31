package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsHistoryEntry
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun MyPqrsDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyPqrsDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Detalle de PQRS", onNavigateBack = onNavigateBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UgEmerald)
            }
            state.error != null -> ErrorView(
                error = requireNotNull(state.error),
                onRetry = viewModel::load
            )
            state.detail != null -> {
                val detail = requireNotNull(state.detail)
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = UgSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Text(
                                detail.subject, color = UgNavy,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp
                            )
                            HorizontalDivider(color = UgBorder)
                            DetailField("Tipo", detail.type.spanishLabel, pqrsTypeIcon(detail.type))
                            DetailField(
                                "Radicada el",
                                DateTimeUtils.formatFechaSolicitud(detail.createdAt),
                                Icons.Default.CalendarToday
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info, contentDescription = null,
                                    tint = UgEmerald, modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Estado", color = UgTextSecondary, fontSize = 12.sp)
                                    StatusBadge(
                                        detail.status.spanishLabel,
                                        residentPqrsStatusTone(detail.status)
                                    )
                                }
                            }
                            HorizontalDivider(color = UgBorder)
                            DetailField("Descripción", detail.description, Icons.AutoMirrored.Filled.Notes)
                        }
                    }
                    ResidentHistorySection(detail.history)
                }
            }
        }
    }
}

@Composable
private fun ResidentHistorySection(history: List<ResidentPqrsHistoryEntry>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, contentDescription = null, tint = UgEmerald)
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Seguimiento", color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Cambios y respuestas de la administración", color = UgTextSecondary, fontSize = 12.sp)
        }
    }
    if (history.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = UgSurface),
            border = BorderStroke(1.dp, UgBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = UgEmerald)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Aún no hay actualizaciones. Aquí aparecerán los cambios de estado y las respuestas.",
                    color = UgNavy
                )
            }
        }
    } else {
        history.forEachIndexed { index, entry -> ResidentHistoryCard(index + 1, entry) }
    }
}

@Composable
private fun ResidentHistoryCard(number: Int, entry: ResidentPqrsHistoryEntry) {
    val sameStatus = entry.fromStatus == entry.toStatus
    Card(
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        border = BorderStroke(1.dp, UgBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.History, contentDescription = null,
                    tint = UgEmerald, modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Actualización $number", color = UgNavy, fontWeight = FontWeight.SemiBold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Estado anterior", color = UgTextSecondary, fontSize = 12.sp)
                StatusBadge(entry.fromStatus.spanishLabel, residentPqrsStatusTone(entry.fromStatus))
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = if (sameStatus) "Continúa en el mismo estado" else "Cambió a",
                    tint = UgTextSecondary,
                    modifier = Modifier.padding(start = 12.dp).size(16.dp)
                )
                if (sameStatus) {
                    Text("Seguimiento sin cambio de estado", color = UgTextSecondary, fontSize = 12.sp)
                }
                Text("Estado nuevo", color = UgTextSecondary, fontSize = 12.sp)
                StatusBadge(entry.toStatus.spanishLabel, residentPqrsStatusTone(entry.toStatus))
            }
            HorizontalDivider(color = UgBorder)
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Respuesta de la administración", color = UgTextSecondary, fontSize = 12.sp)
                    Text(entry.response, color = UgNavy)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = UgTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(DateTimeUtils.formatFechaSolicitud(entry.createdAt), color = UgTextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = UgEmerald, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = UgTextSecondary, fontSize = 12.sp)
            Text(value, color = UgNavy)
        }
    }
}
