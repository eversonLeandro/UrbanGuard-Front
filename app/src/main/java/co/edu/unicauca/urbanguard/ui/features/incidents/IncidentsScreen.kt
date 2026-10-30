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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
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
                onIncidentClick = onIncidentClick,
                onRefresh = { viewModel.onRefresh(isAdmin) },
                isAdmin = isAdmin
            )
        }
    }
}

private enum class IncidentDateFilter(val label: String) {
    ALL("Todas"), TODAY("Hoy"), WEEK("Últimos 7 días"), MONTH("Últimos 30 días")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncidentsBody(
    uiState: IncidentsUiState,
    onIncidentClick: (SecurityIncident) -> Unit,
    onRefresh: () -> Unit,
    isAdmin: Boolean
) {
    val errorMessage = uiState.errorMessage
    var dateFilter by remember { mutableStateOf(IncidentDateFilter.ALL) }
    val filteredIncidents = remember(uiState.incidents, dateFilter) {
        uiState.incidents.filter { it.matches(dateFilter) }
    }

    when {
        uiState.isLoading -> IncidentsLoading()
        errorMessage != null -> IncidentsError(message = errorMessage, onRetryClick = onRefresh)
        uiState.isEmpty -> IncidentsEmpty(isAdmin = isAdmin)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Filtrar por fecha",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UgTextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IncidentDateFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = dateFilter == filter,
                                onClick = { dateFilter = filter },
                                label = { Text(filter.label) }
                            )
                        }
                    }
                }
            }
            if (filteredIncidents.isEmpty()) {
                item { IncidentsEmpty(isAdmin = isAdmin, filtered = true) }
            } else {
                items(filteredIncidents, key = { it.id }) { incident ->
                    Box(Modifier.animateItem()) {
                        IncidentCard(incident = incident, isAdmin = isAdmin, onClick = { onIncidentClick(incident) })
                    }
                }
            }
        }
    }
}

private fun SecurityIncident.matches(filter: IncidentDateFilter): Boolean {
    val zone = ZoneId.systemDefault()
    val incidentDate = createdAt.atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when (filter) {
        IncidentDateFilter.ALL -> true
        IncidentDateFilter.TODAY -> incidentDate == today
        IncidentDateFilter.WEEK -> !incidentDate.isBefore(today.minusDays(6))
        IncidentDateFilter.MONTH -> !incidentDate.isBefore(today.minusDays(29))
    }
}

@Composable
private fun IncidentCard(incident: SecurityIncident, isAdmin: Boolean, onClick: () -> Unit) {
    val color = categoryColor(incident.category)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp
                        )
                    )
                    .background(UgAmber.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (incident.imageUrl.isNullOrBlank()) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = UgAmber,
                        modifier = Modifier.size(38.dp)
                    )
                } else {
                    AsyncImage(
                        model = incident.imageUrl,
                        contentDescription = "Foto de la novedad",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Text(
                    text = incident.category.label(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = incident.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UgTextPrimary,
                    maxLines = 2
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = incidentWhenLabel(incident),
                    fontSize = 11.5.sp,
                    color = UgTextSecondary
                )
                if (isAdmin) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = "Guardia: ${incident.guardName ?: "Nombre no disponible"}",
                        fontSize = 12.sp,
                        color = UgTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
private fun IncidentsEmpty(isAdmin: Boolean, filtered: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (filtered || isAdmin) "No hay novedades en este periodo" else IncidentConstants.LIST_EMPTY_TITLE,
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
