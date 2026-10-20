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
import androidx.compose.material.icons.filled.Nfc
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
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorActionButton
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Lista de checkpoints NFC del conjunto (UG-40).
 *
 * El admin edita, activa o desactiva cada punto y agrega nuevos con el botón
 * flotante. El orden de las paradas se define en cada ruta.
 */
@Composable
fun PatrolPointsScreen(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    viewModel: PatrolPointsViewModel = hiltViewModel()
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
                title = "Puntos de control",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            // Solo con puntos existentes: en vacío el CTA es "Registrar
            // primer punto" del estado vacío.
            if (uiState.points.isNotEmpty()) {
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
                        Text(
                            "Agregar punto",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    containerColor = UgEmerald
                )
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
                uiState.isLoading -> PointsLoading()
                errorMessage != null -> PointsError(
                    message = errorMessage,
                    onRetryClick = viewModel::onRefresh
                )
                uiState.isEmpty -> PointsEmpty(onAddClick = onAddClick)
                else -> PointsContent(
                    uiState = uiState,
                    onEditClick = onEditClick,
                    onToggleActive = viewModel::onToggleActive
                )
            }
        }
    }
}

@Composable
private fun PointsContent(
    uiState: PatrolPointsUiState,
    onEditClick: (String) -> Unit,
    onToggleActive: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PointsSummaryCard(
            total = uiState.points.size,
            active = uiState.activeCount,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.points, key = { it.id }) { point ->
                Box(Modifier.animateItem()) {
                    PatrolPointListItem(
                        point = point,
                        actions = PatrolPointActions(
                            onEdit = { onEditClick(point.id) },
                            onToggleActive = { onToggleActive(point.id, it) }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PointsSummaryCard(total: Int, active: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(UgNavyGradient, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Nfc,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "Puntos NFC",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$total ${if (total == 1) "punto" else "puntos"} · $active activos",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun PointsLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun PointsEmpty(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Nfc,
            contentDescription = null,
            tint = UgNavy,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = PatrolConstants.LIST_EMPTY_TITLE,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = PatrolConstants.LIST_EMPTY_SUBTITLE,
            fontSize = 13.5.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        VisitorActionButton(
            text = "Registrar primer punto",
            onClick = onAddClick
        )
    }
}

@Composable
private fun PointsError(message: String, onRetryClick: () -> Unit) {
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
        VisitorActionButton(
            text = "Reintentar",
            onClick = onRetryClick
        )
    }
}

