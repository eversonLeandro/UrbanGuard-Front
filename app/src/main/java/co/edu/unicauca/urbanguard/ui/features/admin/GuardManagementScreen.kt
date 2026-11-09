package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.GuardMembership
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun GuardManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: GuardManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingGuard by remember { mutableStateOf<GuardMembership?>(null) }

    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Gestión de vigilantes", onNavigateBack = onNavigateBack)
        state.successMessage?.let { message ->
            Text(
                message,
                color = UgEmerald,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        state.errorMessage?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuardStatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.statusFilter == filter,
                    onClick = { viewModel.onStatusFilterChange(filter) },
                    label = { Text(filter.label) }
                )
            }
        }
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp), color = UgEmerald)
        } else if (state.guards.isEmpty()) {
            Text(
                text = "No hay vigilantes para este filtro.",
                color = UgTextSecondary,
                modifier = Modifier.padding(20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 20.dp
                )
            ) {
                items(state.guards, key = GuardMembership::membershipId) { guard ->
                    GuardManagementCard(
                        guard = guard,
                        isChanging = state.changingMembershipId == guard.membershipId,
                        onChangeStatus = { pendingGuard = guard }
                    )
                }
            }
        }
    }

    pendingGuard?.let { guard ->
        AlertDialog(
            onDismissRequest = { pendingGuard = null },
            title = { Text(if (guard.active) "Inactivar vigilante" else "Reactivar vigilante") },
            text = {
                Text(
                    if (guard.active) {
                        "Se retirará el acceso de ${guard.fullName} a este conjunto. Su cuenta y accesos a otros conjuntos no cambiarán."
                    } else {
                        "Se restaurará el acceso de ${guard.fullName} a este conjunto."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onChangeStatus(guard)
                    pendingGuard = null
                }) {
                    Text(if (guard.active) "Inactivar" else "Reactivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingGuard = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun GuardManagementCard(
    guard: GuardMembership,
    isChanging: Boolean,
    onChangeStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(guard.fullName, color = UgNavy, fontWeight = FontWeight.SemiBold)
            guard.email?.let { Text(it, color = UgTextSecondary) }
            Text(
                if (guard.active) "Activo en este conjunto" else "Inactivo en este conjunto",
                color = UgTextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onChangeStatus,
                enabled = !isChanging,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isChanging -> "Guardando…"
                        guard.active -> "Inactivar"
                        else -> "Reactivar"
                    }
                )
            }
        }
    }
}
