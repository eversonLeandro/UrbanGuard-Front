@file:Suppress("MaxLineLength")

package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

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
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PqrsFilter.entries.forEach { filter ->
                    val selected = state.activeFilter == filter
                    Text(
                        text = filter.label,
                        modifier = Modifier
                            .background(
                                if (selected) UgNavy else androidx.compose.ui.graphics.Color.White,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.onFilterSelected(filter) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        color = if (selected) androidx.compose.ui.graphics.Color.White else UgTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UgEmerald)
                }
                state.errorMessage != null && state.items.isEmpty() -> ErrorPqrs(state.errorMessage.orEmpty(), viewModel::onRefresh)
                state.visibleItems.isEmpty() -> EmptyPqrs()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.visibleItems, key = { it.id }) { pqrs ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onOpenDetail(pqrs.id) }
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(pqrs.subject, fontWeight = FontWeight.Bold, color = UgNavy, fontSize = 16.sp)
                                Text("${pqrs.type.spanishLabel} · ${DateTimeUtils.formatFechaSolicitud(pqrs.createdAt)}", color = UgTextSecondary, fontSize = 12.sp)
                                Text(pqrs.authorName, color = UgTextSecondary, fontSize = 13.sp)
                                val uiStatus = pqrsStatusUi(pqrs.status)
                                StatusBadge(text = uiStatus.first, tone = uiStatus.second)
                            }
                        }
                    }
                }
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

