package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.model.LinkRequestState
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgAmberSoft
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgDivider
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgIconFaint
import co.edu.unicauca.urbanguard.ui.theme.UgIconMuted
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar

// Vista 1 de UG-49: lista de solicitudes de vinculación con filtros y estado.
@Composable
fun PendingRequestsScreen(
    onNavigateBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    reviewResult: String? = null,
    onReviewResultHandled: () -> Unit = {},
    viewModel: PendingRequestsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver del detalle tras aprobar/rechazar, muestra la confirmación,
    // recarga la lista (para que el nuevo estado se refleje de inmediato)
    // y limpia el resultado para que solo se muestre una vez.
    LaunchedEffect(reviewResult) {
        if (reviewResult != null) {
            viewModel.showMessage(
                if (reviewResult.equals("APROBADA", ignoreCase = true)) {
                    "Solicitud aceptada correctamente. El usuario fue agregado como residente."
                } else {
                    "Solicitud rechazada correctamente."
                }
            )
            viewModel.reloadRequests()
            onReviewResultHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        // Header unificado
        UrbanGuardTopBar(
            title = "Solicitudes de vinculación",
            onNavigateBack = onNavigateBack
        )

        var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

        // Filtros por estado
        FilterChips(
            activeFilter = uiState.activeFilter,
            onFilterSelected = viewModel::onFilterSelected
        )

        // Buscador
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, UgBorder, RoundedCornerShape(12.dp))
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o correo...", color = UgTextSecondary, fontSize = 14.sp) },
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = UgEmerald
                )
            )
        }

        // Confirmación de la acción realizada en el detalle (aprobar/rechazar)
        uiState.message?.let { message ->
            val isApproved = message.contains("aceptada", ignoreCase = true)
            val textColor = if (isApproved) UgEmerald else UgErrorText
            val bgColor = if (isApproved) UgSuccessBg else UgErrorBg
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(bgColor, RoundedCornerShape(10.dp))
                    .clickable { viewModel.clearMessage() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isApproved) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val loadError = uiState.error
        val finalRequests = uiState.filteredRequests.filter {
            it.nombreSolicitante.contains(searchQuery, ignoreCase = true) ||
            it.emailSolicitante.contains(searchQuery, ignoreCase = true)
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = UgEmerald)
                }
            }

            loadError != null -> {
                ErrorView(
                    error = loadError,
                    onRetry = viewModel::reloadRequests
                )
            }

            finalRequests.isEmpty() -> {
                EmptyRequestsState(
                    filter = uiState.activeFilter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(finalRequests, key = { it.id }) { request ->
                        LinkRequestCard(
                            request = request,
                            onClick = { onOpenDetail(request.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Filtros
// ─────────────────────────────────────────────

private val FilterList = listOf(
    LinkRequestState.PENDIENTE,
    LinkRequestState.APROBADA,
    LinkRequestState.RECHAZADA
)

@Composable
private fun FilterChips(
    activeFilter: LinkRequestState,
    onFilterSelected: (LinkRequestState) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterList.forEach { filter ->
            val isActive = activeFilter == filter
            val label = when (filter) {
                LinkRequestState.PENDIENTE -> "Pendientes"
                LinkRequestState.APROBADA -> "Aprobadas"
                LinkRequestState.RECHAZADA -> "Rechazadas"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(color = if (isActive) UgNavy else Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isActive) UgNavy else UgBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                    color = if (isActive) Color.White else UgTextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Tarjeta de solicitud
// ─────────────────────────────────────────────

@Composable
private fun LinkRequestCard(
    request: LinkRequest,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RequestAvatar(name = request.nombreSolicitante)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.nombreSolicitante,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = request.emailSolicitante,
                        fontSize = 13.sp,
                        color = UgTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                RequestStatusBadge(status = LinkRequestState.fromString(request.estado))
            }
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Divider(color = UgDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Unidad",
                        fontSize = 11.sp,
                        color = UgTextSecondary
                    )
                    Text(
                        text = request.unidad.ifBlank { "Sin unidad" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = UgNavy
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Fecha",
                        fontSize = 11.sp,
                        color = UgTextSecondary
                    )
                    Text(
                        text = request.fechaSolicitud.ifBlank { "Pendiente" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = UgNavy
                    )
                }
            }
        }
    }
}

// Avatar circular con las iniciales del solicitante
@Composable
private fun RequestAvatar(name: String) {
    val initials = name
        .split(" ")
        .filter { it.isNotEmpty() }
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(UgEmerald),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Badge de estado coloreado según el filtro
@Composable
private fun RequestStatusBadge(status: LinkRequestState) {
    val (bg, content) = when (status) {
        LinkRequestState.PENDIENTE -> UgAmberSoft to UgAmber
        LinkRequestState.APROBADA -> UgSuccessBg to UgEmerald
        LinkRequestState.RECHAZADA -> UgErrorBg to UgErrorText
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = when (status) {
                LinkRequestState.PENDIENTE -> "Pendiente"
                LinkRequestState.APROBADA -> "Aprobada"
                LinkRequestState.RECHAZADA -> "Rechazada"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = content
        )
    }
}

// ─────────────────────────────────────────────
// Estado vacío
// ─────────────────────────────────────────────

@Composable
private fun EmptyRequestsState(
    filter: LinkRequestState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(UgBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (filter == LinkRequestState.PENDIENTE) "✓" else "!",
                fontSize = 30.sp,
                color = UgIconMuted,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = when (filter) {
                LinkRequestState.PENDIENTE -> "No hay solicitudes pendientes"
                LinkRequestState.APROBADA -> "No hay solicitudes aprobadas"
                LinkRequestState.RECHAZADA -> "No hay solicitudes rechazadas"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = UgNavy
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Cuando un residente use el código de vinculación, aparecerá aquí.",
            fontSize = 12.sp,
            color = UgTextSecondary,
            lineHeight = 18.sp
        )
    }
}

