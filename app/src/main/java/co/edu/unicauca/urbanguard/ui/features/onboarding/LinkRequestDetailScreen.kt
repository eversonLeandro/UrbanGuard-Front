package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.domain.model.LinkRequestState
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgAmberSoft
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import kotlinx.coroutines.delay

// Vista 2 de UG-49: detalle de una solicitud con aprobación/rechazo.
@Composable
fun LinkRequestDetailScreen(
    onNavigateBack: () -> Unit,
    onReviewFinished: (String) -> Unit,
    viewModel: LinkRequestDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando la acción (aprobar/rechazar) termina, se muestra el mensaje de
    // confirmación un instante y se regresa automáticamente a la lista.
    LaunchedEffect(uiState.successMessage, uiState.reviewResult) {
        val result = uiState.reviewResult
        if (result != null) {
            delay(1500)
            viewModel.consumeReviewResult()
            onReviewFinished(result)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Detalle de solicitud",
            onNavigateBack = onNavigateBack
        )

        RequestDetailBody(
            uiState = uiState,
            onErrorDismissed = viewModel::onErrorDismissed,
            onSuccessDismissed = viewModel::onSuccessDismissed,
            onApproveClick = viewModel::onApproveClick,
            onRejectClick = viewModel::onRejectClick
        )
    }

    ApproveRequestDialog(
        visible = uiState.showApproveDialog,
        request = uiState.request,
        onConfirm = viewModel::onApproveConfirmed,
        onDismiss = viewModel::onDialogDismissed
    )

    RejectRequestDialog(
        visible = uiState.showRejectDialog,
        request = uiState.request,
        onConfirm = viewModel::onRejectConfirmed,
        onDismiss = viewModel::onDialogDismissed
    )
}

/**
 * Cuerpo de la pantalla: resuelve los tres estados posibles (cargando, detalle
 * disponible, sin solicitud). Vive aparte para que [LinkRequestDetailScreen] no
 * tenga que conocer la estructura de estados ni los banners.
 *
 * [LinkRequestDetailUiState] se pasa entero en vez de descomponerlo en
 * parámetros: son más de ocho y la regla de lista larga penalizaría una firma
 * que además volvería frágil frente a cualquier campo nuevo.
 *
 * La parte informative va en una columna scrolleable que cede el espacio
 * sobrante, y las acciones quedan fuera de ella: si no, la suma de cabecera,
 * ficha y botones supera la altura útil en equipos cortos y Compose dibuja el
 * último botón por debajo del borde inferior, donde ya no se ve.
 */
@Composable
private fun RequestDetailBody(
    uiState: LinkRequestDetailUiState,
    onErrorDismissed: () -> Unit,
    onSuccessDismissed: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = UgEmerald)
        }
        return
    }

    val request = uiState.request

    // Sin detalle que mostrar (no existe o no se pudo cargar).
    // El error es lo único relevante y no se puede cerrar: la
    // única salida es la flecha del top bar.
    if (request == null) {
        uiState.errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                DetailMessageBanner(
                    text = message,
                    textColor = UgErrorText,
                    backgroundColor = UgErrorBg,
                    onDismiss = null
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RequestHeaderCard(request)
            InfoCard(request)

            uiState.errorMessage?.let { message ->
                DetailMessageBanner(
                    text = message,
                    textColor = UgErrorText,
                    backgroundColor = UgErrorBg,
                    onDismiss = onErrorDismissed
                )
            }

            ReviewOutcomeBanner(
                message = uiState.successMessage,
                onDismiss = onSuccessDismissed
            )
        }

        // Solo se puede revisar una solicitud pendiente; tras
        // aprobar o rechazar la decisión ya no es reversible.
        if (uiState.isPending) {
            ReviewActionArea(
                isLoadingAction = uiState.isLoadingAction,
                onApproveClick = onApproveClick,
                onRejectClick = onRejectClick
            )
        }
    }
}

/**
 * Banner de exito tras aprobar o rechazar. El tono pasa a ambar cuando el
 * mensaje reporta una sincronizacion pendiente, que no es un fallo pero tampoco
 * un cierre limpio.
 */
@Composable
private fun ReviewOutcomeBanner(message: String?, onDismiss: () -> Unit) {
    message?.let { text ->
        val isWarning = text.contains("pendiente", ignoreCase = true)
        DetailMessageBanner(
            text = text,
            textColor = if (isWarning) UgAmber else UgEmerald,
            backgroundColor = if (isWarning) UgAmberSoft else UgSuccessBg,
            onDismiss = onDismiss
        )
    }
}

/**
 * Botones de aprobar y rechazar, o el indicador de progreso mientras la decisión
 * viaja al servidor. No introduce un nodo de layout: los dos botones siguen
 * siendo hijos directos de la Column que lo contiene, con el mismo
 * `spacedBy(14.dp)`.
 */
@Composable
private fun ReviewActionArea(
    isLoadingAction: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    if (isLoadingAction) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = UgEmerald)
        }
    } else {
        Button(
            onClick = onApproveClick,
            colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                "Aprobar solicitud",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        OutlinedButton(
            onClick = onRejectClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UgErrorText),
            border = BorderStroke(1.dp, UgErrorText),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                "Rechazar solicitud",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────
// Avisos de resultado (éxito / error) — mismo estilo
// que el banner de PendingRequestsScreen
// ─────────────────────────────────────────────

@Composable
private fun DetailMessageBanner(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onDismiss: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .then(if (onDismiss != null) Modifier.clickable(onClick = onDismiss) else Modifier)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (textColor == UgErrorText) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

// ─────────────────────────────────────────────
// Cabecera: avatar grande + nombre + email + badge
// ─────────────────────────────────────────────

@Composable
private fun RequestHeaderCard(request: LinkRequest) {
    val initials = request.nombreSolicitante
        .split(" ")
        .filter { it.isNotEmpty() }
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(UgEmerald),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = request.nombreSolicitante,
                    color = UgNavy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = request.emailSolicitante,
                    color = UgTextSecondary,
                    fontSize = 14.sp
                )
            }
            DetailStatusBadge(status = LinkRequestState.fromString(request.estado))
        }
    }
}

@Composable
private fun DetailStatusBadge(status: LinkRequestState) {
    val (bg, content) = when (status) {
        LinkRequestState.PENDIENTE -> UgAmberSoft to UgAmber
        LinkRequestState.APROBADA -> UgSuccessBg to UgEmerald
        LinkRequestState.RECHAZADA -> UgErrorBg to UgErrorText
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = when (status) {
                LinkRequestState.PENDIENTE -> "Pendiente de revisión"
                LinkRequestState.APROBADA -> "Solicitud aprobada"
                LinkRequestState.RECHAZADA -> "Solicitud rechazada"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = content
        )
    }
}

// ─────────────────────────────────────────────
// Filas de información
// ─────────────────────────────────────────────

@Composable
private fun InfoCard(request: LinkRequest) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            InfoRow(
                label = "Conjunto",
                value = request.conjuntoNombre,
                icon = Icons.Default.CheckCircle
            )
            CardDivider()
            InfoRow(
                label = "Fecha de solicitud",
                value = request.fechaSolicitud.ifBlank { "Pendiente" },
                icon = Icons.Default.CheckCircle
            )
            CardDivider()
            InfoRow(label = "Unidad", value = request.unidad, icon = Icons.Default.CheckCircle)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(UgBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                color = UgTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            color = UgNavy,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = UgBorderSoft
    )
}
