package co.edu.unicauca.urbanguard.ui.features.incidents

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.IncidentConstants
import co.edu.unicauca.urbanguard.domain.model.IncidentSyncState
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.model.label
import co.edu.unicauca.urbanguard.ui.components.LocalPhotoImage
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorActionButton
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val incidentDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault())

/**
 * Bandeja de novedades del guardia: foto, categoría, hora relativa y estado
 * de envío. Solo muestra el FAB cuando ya hay novedades.
 */
@Composable
fun IncidentsScreen(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
    onIncidentClick: (SecurityIncident) -> Unit,
    viewModel: IncidentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            UrbanGuardTopBar(
                title = "Novedades",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (uiState.incidents.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    text = {
                        Text("Reportar", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    containerColor = UgEmerald
                )
            }
        },
        containerColor = UgBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            IncidentsBody(
                uiState = uiState,
                onAddClick = onAddClick,
                onIncidentClick = { incident ->
                    viewModel.selectIncident(incident)
                    onIncidentClick(incident)
                },
                onRefresh = viewModel::onRefresh
            )
        }
    }
}

@Composable
private fun IncidentsBody(
    uiState: IncidentsUiState,
    onAddClick: () -> Unit,
    onIncidentClick: (SecurityIncident) -> Unit,
    onRefresh: () -> Unit
) {
    val errorMessage = uiState.errorMessage
    when {
        uiState.isLoading -> IncidentsLoading()
        errorMessage != null -> IncidentsError(
            message = errorMessage,
            onRetryClick = onRefresh
        )
        uiState.isEmpty -> IncidentsEmpty(onAddClick = onAddClick)
        else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 18.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.pendingCount > 0) {
                        item(key = "pending") {
                            PendingBanner(count = uiState.pendingCount)
                        }
                    }
                    items(uiState.incidents, key = { it.id }) { incident ->
                        Box(Modifier.animateItem()) {
                            IncidentCard(
                                incident = incident,
                                onClick = { onIncidentClick(incident) }
                            )
                        }
                    }
                }
            }
}

@Composable
private fun PendingBanner(count: Int) {
    Text(
        text = if (count == 1) {
            "1 novedad por enviar"
        } else {
            "$count novedades por enviar"
        },
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = UgAmber,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(UgAmber.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

@Composable
private fun IncidentCard(incident: SecurityIncident, onClick: () -> Unit) {
    val color = categoryColor(incident.category)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalPhotoImage(
                path = incident.photoPath,
                size = 64.dp,
                contentDescription = null,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoryIcon(incident.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = incident.category.label(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                    SyncBadge(synced = incident.syncState == IncidentSyncState.SYNCED)
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = incident.description,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgTextPrimary,
                    maxLines = 2
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = incidentWhenLabel(incident),
                    fontSize = 11.5.sp,
                    color = UgTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SyncBadge(synced: Boolean) {
    Text(
        text = if (synced) "ENVIADA" else "PENDIENTE",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (synced) UgEmerald else UgAmber,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                (if (synced) UgEmerald else UgAmber).copy(alpha = 0.12f)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private fun incidentWhenLabel(incident: SecurityIncident): String {
    val minutes = Duration.between(incident.createdAt, Instant.now()).toMinutes()
    val relative = when {
        minutes < 1 -> "Ahora mismo"
        minutes < 60 -> "Hace $minutes min"
        minutes < 60 * 24 -> "Hace ${minutes / 60} h"
        else -> incidentDateFormat.format(incident.createdAt)
    }
    return if (incident.pointName.isNullOrBlank()) {
        relative
    } else {
        "$relative · ${incident.pointName}"
    }
}

@Composable
private fun IncidentsLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun IncidentsEmpty(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = IncidentConstants.LIST_EMPTY_TITLE,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = IncidentConstants.LIST_EMPTY_SUBTITLE,
            fontSize = 13.5.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        VisitorActionButton(text = "Reportar novedad", onClick = onAddClick)
    }
}

@Composable
private fun IncidentsError(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        VisitorActionButton(text = "Reintentar", onClick = onRetryClick)
    }
}
