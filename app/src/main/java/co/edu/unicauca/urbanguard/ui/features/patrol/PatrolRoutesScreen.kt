package co.edu.unicauca.urbanguard.ui.features.patrol

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButton
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
import co.edu.unicauca.urbanguard.domain.model.PatrolRoute
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import co.edu.unicauca.urbanguard.domain.model.shortLabel
import co.edu.unicauca.urbanguard.domain.model.weekDays
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorActionButton
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.format.DateTimeFormatter

private val timeLabelFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Lista de rutas de patrullaje del conjunto con sus horarios (UG-40).
 *
 * Cada ruta combina puntos y repeticiones: la misma ruta puede caminarse a
 * varias horas, y puede haber varias rutas con puntos distintos.
 */
@Composable
fun PatrolRoutesScreen(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    viewModel: PatrolRoutesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recarga al volver del formulario: el ViewModel sobrevive al back,
    // así que el `init` no se repite y la lista quedaría desactualizada.
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
                title = "Rutas de patrullaje",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            // Solo con rutas existentes: en vacío el CTA es "Crear primera
            // ruta" del estado vacío.
            if (uiState.routes.isNotEmpty()) {
                AddRouteFab(onClick = onAddClick)
            }
        },
        containerColor = UgBackground
    ) { padding ->
        val errorMessage = uiState.errorMessage
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> RoutesLoading()
                errorMessage != null -> RoutesError(
                    message = errorMessage,
                    onRetryClick = viewModel::onRefresh
                )
                uiState.isEmpty -> RoutesEmpty(onAddClick = onAddClick)
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
                    items(uiState.routes, key = { it.id }) { route ->
                        Box(Modifier.animateItem()) {
                            RouteCard(
                                route = route,
                                onEdit = { onEditClick(route.id) },
                                onToggleActive = { viewModel.onToggleActive(route, it) },
                                onDelete = { viewModel.onRequestDelete(route) }
                            )
                        }
                    }
                }
            }

            val pending = uiState.pendingDelete
            if (pending != null) {
                DeleteRouteDialog(
                    routeName = pending.name,
                    onConfirm = viewModel::onConfirmDelete,
                    onDismiss = viewModel::onDismissDelete
                )
            }
        }
    }
}

@Composable
private fun AddRouteFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        },
        text = {
            Text("Nueva ruta", fontWeight = FontWeight.Bold, color = Color.White)
        },
        containerColor = UgEmerald
    )
}

@Composable
private fun RouteCard(
    route: PatrolRoute,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onEdit,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = UgNavy,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = route.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgNavy,
                    modifier = Modifier.weight(1f)
                )
                RouteActiveBadge(isActive = route.isActive)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${route.pointIds.size} " +
                    if (route.pointIds.size == 1) "punto" else "puntos",
                fontSize = 12.5.sp,
                color = UgTextSecondary
            )

            route.schedules.forEach { schedule ->
                Spacer(Modifier.height(6.dp))
                ScheduleLine(schedule = schedule)
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = route.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(checkedTrackColor = UgEmerald)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar ruta",
                        tint = UgErrorText
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleLine(schedule: PatrolSchedule) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = UgEmerald,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = scheduleLineText(schedule),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = UgTextPrimary
        )
    }
}

private fun scheduleLineText(schedule: PatrolSchedule): String {
    val days = weekDays()
        .filter { schedule.days.contains(it) }
        .joinToString(", ") { it.shortLabel() }
    return "${schedule.time.format(timeLabelFormat)} · $days"
}

@Composable
private fun RouteActiveBadge(isActive: Boolean) {
    val color = if (isActive) UgEmerald else UgTextSecondary
    Text(
        text = if (isActive) "ACTIVA" else "INACTIVA",
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

@Composable
private fun RoutesLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun RoutesEmpty(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Route,
            contentDescription = null,
            tint = UgNavy,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Aún no hay rutas",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Arma la primera ruta con sus puntos y horarios.",
            fontSize = 13.5.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        VisitorActionButton(text = "Crear primera ruta", onClick = onAddClick)
    }
}

@Composable
private fun RoutesError(message: String, onRetryClick: () -> Unit) {
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

@Composable
private fun DeleteRouteDialog(
    routeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar ruta", fontWeight = FontWeight.Bold, color = UgNavy) },
        text = {
            Text("¿Eliminar \"$routeName\"? Los puntos de control no se borran.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = UgErrorText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = UgNavy) }
        }
    )
}
