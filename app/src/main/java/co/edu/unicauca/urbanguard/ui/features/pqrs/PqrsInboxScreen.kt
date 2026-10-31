@file:Suppress("MaxLineLength")

package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.domain.model.PqrsSummary
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg

@Composable
fun PqrsInboxScreen(
    onNavigateBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: PqrsInboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { UrbanGuardTopBar(title = "PQRS recibidas", onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(UgBackground).padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Buscar por asunto o residente…") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            PqrsInboxFilters(
                activeFilter = state.activeFilter,
                activeType = state.activeType,
                onFilterSelected = viewModel::onFilterSelected,
                onTypeSelected = viewModel::onTypeSelected
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UgEmerald)
                }
                state.errorMessage != null && state.items.isEmpty() -> ErrorPqrs(state.errorMessage.orEmpty(), viewModel::onRefresh)
                state.visibleItems.isEmpty() -> EmptyPqrs()
                else -> PqrsInboxList(state.visibleItems, onOpenDetail)
            }
        }
    }
}

@Composable
private fun PqrsInboxList(items: List<PqrsSummary>, onOpenDetail: (String) -> Unit) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { pqrs ->
                    PqrsInboxCard(pqrs, onOpenDetail)
                }
            }
}

@Composable
private fun PqrsInboxCard(pqrs: PqrsSummary, onOpenDetail: (String) -> Unit) {
            Card(
                colors = CardDefaults.cardColors(containerColor = UgSurface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().clickable { onOpenDetail(pqrs.id) }
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                                .background(UgEmerald.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(pqrsTypeIcon(pqrs.type), contentDescription = null, tint = UgEmerald)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("TIPO", color = UgTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(pqrs.type.spanishLabel, color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = UgTextSecondary)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("ASUNTO", color = UgTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(pqrs.subject, fontWeight = FontWeight.Bold, color = UgNavy, fontSize = 17.sp)
                    }
                    HorizontalDivider(color = UgBorder)
                    PqrsInboxCardResident(pqrs)
                    PqrsInboxCardDate(pqrs.createdAt)
                }
            }
}

@Composable
private fun PqrsInboxCardResident(pqrs: PqrsSummary) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            pqrs.authorName,
            color = UgNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        val uiStatus = pqrsStatusUi(pqrs.status)
        StatusBadge(text = uiStatus.first, tone = uiStatus.second)
    }
}

@Composable
private fun PqrsInboxCardDate(createdAt: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Radicada el ${DateTimeUtils.formatFechaSolicitud(createdAt)}",
            color = UgTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PqrsInboxFilters(
    activeFilter: PqrsFilter,
    activeType: PqrsType?,
    onFilterSelected: (PqrsFilter) -> Unit,
    onTypeSelected: (PqrsType?) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = UgEmerald, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Filtrar solicitudes", color = UgNavy, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PqrsFilterSelect(
                    label = "Estado",
                    selection = activeFilter.label,
                    modifier = Modifier.weight(1f),
                    options = PqrsFilter.entries.map { it.label },
                    onSelected = { label ->
                        PqrsFilter.entries.firstOrNull { it.label == label }?.let(onFilterSelected)
                    }
                )
                PqrsFilterSelect(
                    label = "Tipo",
                    selection = activeType?.spanishLabel ?: "Todos",
                    modifier = Modifier.weight(1f),
                    options = listOf("Todos") + PqrsType.entries.map { it.spanishLabel },
                    onSelected = { label ->
                        onTypeSelected(PqrsType.entries.firstOrNull { it.spanishLabel == label })
                    }
                )
            }
        }
    }
}

@Composable
private fun PqrsFilterSelect(
    label: String,
    selection: String,
    modifier: Modifier,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(UgInputBg, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, color = UgTextSecondary, fontSize = 10.sp)
                Text(selection, color = UgNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = UgTextSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun pqrsStatusUi(status: PqrsStatus): Pair<String, StatusTone> = when (status) {
    PqrsStatus.SUBMITTED, PqrsStatus.IN_REVIEW -> status.spanishLabel to StatusTone.WARNING
    PqrsStatus.IN_PROGRESS -> status.spanishLabel to StatusTone.INFO
    PqrsStatus.RESOLVED -> status.spanishLabel to StatusTone.SUCCESS
    PqrsStatus.REJECTED -> status.spanishLabel to StatusTone.ERROR
    PqrsStatus.CLOSED -> status.spanishLabel to StatusTone.NEUTRAL
}

@Composable
private fun ErrorPqrs(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = UgTextSecondary)
            Text("Reintentar", color = UgEmerald, modifier = Modifier.clickable(onClick = onRetry), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyPqrs() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = UgTextSecondary)
            Text("No hay PQRS en esta vista", fontWeight = FontWeight.Bold, color = UgNavy)
        }
    }
}
