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
import androidx.compose.material.icons.filled.Image
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
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.model.label
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
 * Bandeja remota de novedades, compartida por guardia y administrador.
 */
@Composable
fun IncidentsScreen(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
    onIncidentClick: (SecurityIncident) -> Unit,
    isAdmin: Boolean = false,
    viewModel: IncidentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onRefresh(isAdmin)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            UrbanGuardTopBar(
                title = if (isAdmin) "Novedades de vigilancia" else "Novedades",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (!isAdmin) {
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
                onIncidentClick = onIncidentClick,
                onRefresh = { viewModel.onRefresh(isAdmin) },
                isAdmin = isAdmin
            )
        }
    }
}

@Composable
private fun IncidentsBody(
    uiState: IncidentsUiState,
    onAddClick: () -> Unit,
    onIncidentClick: (SecurityIncident) -> Unit,
    onRefresh: () -> Unit,
    isAdmin: Boolean
) {
    val errorMessage = uiState.errorMessage
    when {
        uiState.isLoading -> IncidentsLoading()
        errorMessage != null -> IncidentsError(
            message = errorMessage,
            onRetryClick = onRefresh
        )
        uiState.isEmpty -> IncidentsEmpty(isAdmin = isAdmin)
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
                    items(uiState.incidents, key = { it.id }) { incident ->
                        Box(Modifier.animateItem()) {
                            IncidentCard(
                                incident = incident,
                                isAdmin = isAdmin,
                                onClick = { onIncidentClick(incident) }
                            )
                        }
                    }
                }
            }
}

@Composable
private fun IncidentCard(incident: SecurityIncident, isAdmin: Boolean, onClick: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UgAmber.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = "Foto de la novedad", tint = UgAmber)
            }
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
                    text = buildString {
                        append(incidentWhenLabel(incident))
                        if (isAdmin) append(" · Guardia ${incident.guardId.take(8)}")
                    },
                    fontSize = 11.5.sp,
                    color = UgTextSecondary
                )
            }
        }
    }
}

private fun incidentWhenLabel(incident: SecurityIncident): String {
    val minutes = Duration.between(incident.createdAt, Instant.now()).toMinutes()
    val relative = when {
        minutes < 1 -> "Ahora mismo"
        minutes < 60 -> "Hace $minutes min"
        minutes < 60 * 24 -> "Hace ${minutes / 60} h"
        else -> incidentDateFormat.format(incident.createdAt)
    }
    val point = incident.pointName ?: incident.pointId?.take(8)?.let { "Punto $it" }
    return if (point.isNullOrBlank()) relative else "$relative · $point"
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
private fun IncidentsEmpty(isAdmin: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isAdmin) "No hay novedades en este periodo" else IncidentConstants.LIST_EMPTY_TITLE,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isAdmin) "Las novedades reportadas aparecerán aquí." else IncidentConstants.LIST_EMPTY_SUBTITLE,
            fontSize = 13.5.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
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
