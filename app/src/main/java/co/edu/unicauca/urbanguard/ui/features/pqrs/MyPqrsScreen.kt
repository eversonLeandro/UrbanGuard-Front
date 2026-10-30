package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary
import co.edu.unicauca.urbanguard.ui.components.ErrorBanner
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPqrsScreen(
    onNavigateBack: () -> Unit,
    onRadicar: () -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: MyPqrsViewModel = hiltViewModel()
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

    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Mis PQRS", onNavigateBack = onNavigateBack)
        PqrsFilters(
            status = state.statusFilter,
            type = state.typeFilter,
            hasFilters = state.hasFilters,
            onStatusSelected = viewModel::onStatusSelected,
            onTypeSelected = viewModel::onTypeSelected,
            onClear = viewModel::clearFilters
        )
        if (state.error != null && state.items.isNotEmpty()) {
            ErrorBanner(
                message = state.error?.message.orEmpty(),
                modifier = Modifier.padding(horizontal = 20.dp),
                onRetry = viewModel::onRefresh
            )
        }
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UgEmerald)
                }
                state.error != null && state.items.isEmpty() -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                if (state.error is AppError.Forbidden) "Sin permisos"
                                else "No pudimos cargar tus PQRS",
                                color = UgNavy, fontWeight = FontWeight.SemiBold
                            )
                            ErrorBanner(
                                message = state.error?.message.orEmpty(),
                                onRetry = viewModel::onRefresh
                            )
                        }
                    }
                }
                state.isGeneralEmpty -> EmptyMessage("No tienes PQRS radicadas actualmente.")
                state.isFilteredEmpty -> EmptyMessage(
                    "No hay PQRS que coincidan con los filtros seleccionados.",
                    onClear = viewModel::clearFilters
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items, key = { it.id }) { item -> PqrsCard(item, onOpenDetail) }
                }
            }
        }
        RadicarBottomBar(onRadicar)
    }
}

@Composable
private fun RadicarBottomBar(onRadicar: () -> Unit) {
    Surface(color = UgSurface, shadowElevation = 8.dp) {
        Button(
            onClick = onRadicar,
            colors = ButtonDefaults.buttonColors(
                containerColor = UgEmerald,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Radicar PQRS", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PqrsFilters(
    status: PqrsStatus?,
    type: PqrsType?,
    hasFilters: Boolean,
    onStatusSelected: (PqrsStatus?) -> Unit,
    onTypeSelected: (PqrsType?) -> Unit,
    onClear: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = UgEmerald)
                Spacer(Modifier.width(8.dp))
                Text("Filtrar solicitudes", color = UgNavy, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                if (hasFilters) TextButton(onClick = onClear) {
                    Text("Limpiar", color = UgEmerald, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PqrsFilterMenu(
                    label = "Estado", value = status, options = PqrsStatus.entries,
                    valueLabel = { it.spanishLabel }, onSelect = onStatusSelected,
                    modifier = Modifier.weight(1f)
                )
                PqrsFilterMenu(
                    label = "Tipo", value = type, options = PqrsType.entries,
                    valueLabel = { it.spanishLabel }, onSelect = onTypeSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun <T> PqrsFilterMenu(
    label: String,
    value: T?,
    options: List<T>,
    valueLabel: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selection = value?.let(valueLabel) ?: "Todos"
    Box(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (value == null) UgInputBg else UgSuccessBg)
                .border(BorderStroke(1.dp, if (value == null) UgBorder else UgEmerald), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .semantics { contentDescription = "$label: $selection" }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, color = UgTextSecondary, fontSize = 11.sp)
                Text(selection, color = UgNavy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = UgNavy)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelect(null); expanded = false })
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(valueLabel(option)) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun PqrsCard(item: ResidentPqrsSummary, onOpenDetail: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
            .semantics {
                contentDescription = "Abrir PQRS ${item.type.spanishLabel}: " +
                    "${item.subject}, ${item.status.spanishLabel}"
            }
            .clickable { onOpenDetail(item.id) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                        .background(UgEmerald.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(pqrsTypeIcon(item.type), contentDescription = null, tint = UgEmerald)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.subject, color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(item.type.spanishLabel, color = UgTextSecondary, fontSize = 13.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = UgTextSecondary)
            }
            HorizontalDivider(color = UgBorder)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday, contentDescription = null,
                    tint = UgTextSecondary, modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    DateTimeUtils.formatFechaSolicitud(item.createdAt),
                    color = UgTextSecondary, fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(item.status.spanishLabel, residentPqrsStatusTone(item.status))
            }
        }
    }
}

internal fun pqrsTypeIcon(type: PqrsType): ImageVector = when (type) {
    PqrsType.PETITION -> Icons.Default.Description
    PqrsType.COMPLAINT -> Icons.Default.Feedback
    PqrsType.CLAIM -> Icons.Default.ReportProblem
    PqrsType.SUGGESTION -> Icons.Default.Lightbulb
}

@Composable
private fun EmptyMessage(message: String, onClear: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Inbox, contentDescription = null, tint = UgTextSecondary)
        Spacer(Modifier.padding(6.dp))
        Text(message, color = UgNavy, fontWeight = FontWeight.Medium)
        if (onClear != null) TextButton(onClick = onClear) { Text("Limpiar filtros") }
    }
}

