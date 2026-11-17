package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Gestión de memberships de vigilantes por conjunto (HU de inactivación) —
 * misma convención visual y de feedback que la gestión de usuarios del
 * superadministrador (UserListScreen): Scaffold con Snackbar de éxito,
 * FilterChips coloreados por estado, filas con avatar de iniciales e
 * iconos de acción, y banner de error reutilizable.
 */
@Composable
fun GuardManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: GuardManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingGuard by remember { mutableStateOf<GuardMembership?>(null) }

    // Snackbar de éxito: se muestra una vez y se descarta al terminar.
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onDismissFeedback()
        }
    }

    Scaffold(
        containerColor = UgBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { GuardManagementTopBar(onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Descripción del flujo para dar contexto antes de la primera acción.
            Text(
                "Inactiva un vigilante para retirar su acceso a este conjunto. " +
                    "Su cuenta y sus accesos a otros conjuntos no cambian.",
                fontSize = 12.5.sp,
                color = UgTextSecondary
            )

            // Filtro de estado: cambia el parámetro ?estado= y recarga del servidor.
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = state.statusFilter == GuardStatusFilter.ACTIVE,
                    onClick = { viewModel.onStatusFilterChange(GuardStatusFilter.ACTIVE) },
                    label = { Text("Activos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UgEmerald.copy(alpha = 0.15f)
                    )
                )
                FilterChip(
                    selected = state.statusFilter == GuardStatusFilter.INACTIVE,
                    onClick = { viewModel.onStatusFilterChange(GuardStatusFilter.INACTIVE) },
                    label = { Text("Inactivos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UgAmber.copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = state.statusFilter == GuardStatusFilter.ALL,
                    onClick = { viewModel.onStatusFilterChange(GuardStatusFilter.ALL) },
                    label = { Text("Todos") }
                )
            }

            when {
                state.isLoading -> FullLoading()
                state.errorMessage != null -> ErrorBanner(
                    message = state.errorMessage!!,
                    onDismiss = viewModel::onDismissFeedback
                )
                state.guards.isEmpty() -> EmptyResults(state.statusFilter)
                else -> GuardResults(
                    state = state,
                    onActionRequest = { guard -> pendingGuard = guard }
                )
            }
        }
    }

    val pending = pendingGuard
    if (pending != null) {
        ConfirmGuardStatusDialog(
            guard = pending,
            onConfirm = {
                pendingGuard = null
                viewModel.onChangeStatus(pending)
            },
            onDismiss = { pendingGuard = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuardManagementTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Gestión de vigilantes", fontWeight = FontWeight.Bold, color = UgNavy) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = UgNavy
                )
            }
        }
    )
}

@Composable
private fun FullLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UgErrorBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UgErrorBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, fontSize = 13.sp, color = UgErrorText, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = UgErrorText)
            }
        }
    }
}

@Composable
private fun EmptyResults(filter: GuardStatusFilter) {
    val message = when (filter) {
        GuardStatusFilter.ACTIVE -> "No hay vigilantes activos."
        GuardStatusFilter.INACTIVE -> "No hay vigilantes inactivos."
        GuardStatusFilter.ALL -> "No hay vigilantes para este conjunto."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(42.dp)
        )
        Text(message, fontSize = 13.5.sp, color = UgTextSecondary)
    }
}

@Composable
private fun GuardResults(
    state: GuardManagementUiState,
    onActionRequest: (GuardMembership) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.guards, key = GuardMembership::membershipId) { guard ->
            GuardRow(
                guard = guard,
                isBusy = state.changingMembershipId == guard.membershipId,
                onChangeStatus = { onActionRequest(guard) }
            )
        }
    }
}

@Composable
private fun ConfirmGuardStatusDialog(
    guard: GuardMembership,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (guard.active) "Inactivar vigilante" else "Reactivar vigilante") },
        text = {
            Text(
                if (guard.active) {
                    "Se retirará el acceso de ${guard.fullName} a este conjunto. " +
                        "Su cuenta y sus accesos a otros conjuntos no cambiarán."
                } else {
                    "Se restaurará el acceso de ${guard.fullName} a este conjunto."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (guard.active) "Inactivar" else "Reactivar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
