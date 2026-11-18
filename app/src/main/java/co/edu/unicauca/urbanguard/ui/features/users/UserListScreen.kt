package co.edu.unicauca.urbanguard.ui.features.users

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.domain.model.User
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Gestión de cuentas del superadministrador (HU-006): desactivar (revocar
 * acceso sin borrar historial) y reactivar cuentas.
 *
 * La fila propia va marcada "Tu cuenta" y SIN acción de desactivación: es la
 * defensa de UX; el backend además responde 409 si el servidor recibe un
 * intento de autodesactivación.
 */
@Composable
fun UserListScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: UserListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar de éxito: se muestra una vez y se descarta al terminar.
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onFeedbackShown()
        }
    }

    Scaffold(
        containerColor = UgBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { UserListTopBar(onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UserListFilters(state = state, viewModel = viewModel)

            when {
                state.isLoading -> FullLoading()
                state.errorMessage != null -> ErrorBanner(
                    message = state.errorMessage!!,
                    onDismiss = viewModel::onFeedbackShown
                )
                else -> UserResults(state = state, viewModel = viewModel)
            }
        }
    }

    val pending = state.pendingDeactivation
    if (pending != null) {
        ConfirmDeactivateDialog(
            user = pending,
            isSelf = pending.userId == state.myUserId,
            inProgress = state.actionInProgressUserId == pending.userId,
            onConfirm = viewModel::onConfirmDeactivate,
            onDismiss = { viewModel.onDeactivateDialogChange(null) }
        )
    }
}

@Composable
private fun UserListFilters(state: UserListUiState, viewModel: UserListViewModel) {
    // Descripción del flujo para dar contexto antes de la primera acción.
    Text(
        "Desactiva cuentas para revocar su acceso sin eliminar su historial. " +
            "Puedes reactivarlas en cualquier momento.",
        fontSize = 12.5.sp,
        color = UgTextSecondary
    )

    // Filtro de estado: cambia el parámetro ?estado= y recarga del servidor.
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = state.filter == UserListFilter.ACTIVE,
            onClick = { viewModel.onSelectFilter(UserListFilter.ACTIVE) },
            label = { Text("Activos") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = UgEmerald.copy(alpha = 0.15f)
            )
        )
        FilterChip(
            selected = state.filter == UserListFilter.INACTIVE,
            onClick = { viewModel.onSelectFilter(UserListFilter.INACTIVE) },
            label = { Text("Desactivados") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = UgAmber.copy(alpha = 0.2f)
            )
        )
    }

    // Búsqueda local sobre el catálogo cargado.
    OutlinedTextField(
        value = state.query,
        onValueChange = viewModel::onQueryChange,
        label = { Text("Buscar por nombre o correo") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = UgTextSecondary)
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UgEmerald,
            cursorColor = UgEmerald
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Gestión de usuarios", fontWeight = FontWeight.Bold, color = UgNavy) },
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
private fun UserResults(state: UserListUiState, viewModel: UserListViewModel) {
    val users = state.filteredUsers
    if (users.isEmpty()) {
        EmptyResults(
            filter = state.filter,
            hasQuery = state.query.isNotBlank()
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (state.isRefreshing) {
            LinearProgressIndicator(
                color = UgEmerald,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users, key = { it.userId }) { user ->
                UserRow(state = state, user = user, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun EmptyResults(filter: UserListFilter, hasQuery: Boolean) {
    val message = if (hasQuery) {
        "Ninguna cuenta coincide con la búsqueda."
    } else {
        when (filter) {
            UserListFilter.ACTIVE -> "No hay cuentas activas."
            UserListFilter.INACTIVE -> "No hay cuentas desactivadas."
        }
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
private fun ConfirmDeactivateDialog(
    user: User,
    isSelf: Boolean,
    inProgress: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!inProgress) onDismiss() },
        icon = {
            Icon(Icons.Default.Block, contentDescription = null, tint = UgErrorText)
        },
        title = {
            Text("¿Desactivar esta cuenta?", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${user.displayName} (${user.email ?: user.userId}) perderá el acceso " +
                        "inmediatamente y ya no podrá iniciar sesión.",
                    fontSize = 13.5.sp
                )
                Text(
                    "Su historial se conserva y podrá reactivarse posteriormente.",
                    fontSize = 12.5.sp,
                    color = UgTextSecondary
                )
                if (isSelf) {
                    // Red de seguridad: la fila propia no debía llegar hasta aquí.
                    Text(
                        "No puedes desactivar tu propia cuenta.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UgErrorText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !inProgress && !isSelf,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UgErrorBg,
                    contentColor = UgErrorText
                )
            ) {
                Text("Desactivar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inProgress) {
                Text("Cancelar", color = UgNavy)
            }
        }
    )
}
