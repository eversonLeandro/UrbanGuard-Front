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
            onNavigateBack = onNavigateBack,
            showMenu = false
        )

        // Filtros por estado
        FilterChips(
            activeFilter = uiState.activeFilter,
            onFilterSelected = viewModel::onFilterSelected
        )

        // Confirmación de la acción realizada en el detalle (aprobar/rechazar)
        uiState.message?.let { message ->
            val isApproved = message.contains("aceptada", ignoreCase = true)
            val textColor = if (isApproved) UgEmerald else UgErrorText
            val bgColor = if (isApproved) UgSuccessBg else UgErrorBg
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = UgEmerald)
                }
            }

            uiState.errorMessage != null -> {
                ErrorRequestsState(
                    message = uiState.errorMessage!!,
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.filteredRequests.isEmpty() -> {
                EmptyRequestsState(
                    filter = uiState.activeFilter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredRequests, key = { it.id }) { request ->
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, UgDivider, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RequestAvatar(name = request.nombreSolicitante)

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = request.nombreSolicitante,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = UgNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = request.emailSolicitante,
                fontSize = 12.sp,
                color = UgTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${request.conjuntoNombre.ifBlank { "Conjunto" }} · Apartamento ${request.unidad.ifBlank { "sin unidad" }}",
                fontSize = 12.sp,
                color = UgNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = request.fechaSolicitud.ifBlank { "Solicitud pendiente" },
                fontSize = 11.sp,
                color = UgTextSecondary
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            RequestStatusBadge(
                status = LinkRequestState.fromString(request.estado)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = UgIconFaint
            )
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

@Composable
private fun ErrorRequestsState(
    message: String,
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
                .background(UgErrorBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                fontSize = 30.sp,
                color = UgErrorText,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No fue posible cargar las solicitudes",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = UgNavy
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = UgTextSecondary,
            lineHeight = 18.sp
        )
    }
}