package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgAmberSoft
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

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
        // Header navy (mismo patrón que el resto de onboarding)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UgNavy)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = UgEmerald
                )
            }
            Text(
                text = "Detalle de solicitud",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
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

            else -> {
                val request = uiState.request
                if (request != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RequestHeaderCard(request)
                        InfoCard(request)

                        uiState.successMessage?.let { message ->
                            val isWarning = message.contains("pendiente", ignoreCase = true)
                            val textColor = if (isWarning) UgAmber else UgEmerald
                            val bgColor = if (isWarning) UgAmberSoft else Color(0xFFECFDF5)
                            Text(
                                text = message,
                                color = textColor,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgColor, RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                                    .clickable { viewModel.onSuccessDismissed() }
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        // Botones de acción (bloqueados tras decidir)
                        if (uiState.isLoadingAction) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = UgEmerald)
                            }
                        } else {
                            Button(
                                onClick = viewModel::onApproveClick,
                                enabled = uiState.isActionEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UgEmerald,
                                    disabledContainerColor = Color(0xFFD1FAE5)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Aprobar solicitud", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            OutlinedButton(
                                onClick = viewModel::onRejectClick,
                                enabled = uiState.isActionEnabled,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = UgErrorText,
                                    disabledContentColor = Color(0xFFFECACA)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isActionEnabled) UgErrorText else Color(0xFFFECACA)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Rechazar solicitud", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de aprobación
    if (uiState.showApproveDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDialogDismissed,
            containerColor = Color.White,
            title = {
                Text("¿Aprobar la solicitud?", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            },
            text = {
                Text(
                    "El residente ${uiState.request?.nombreSolicitante ?: ""} será vinculado a ${uiState.request?.conjuntoNombre ?: "su conjunto"}.",
                    fontSize = 13.sp,
                    color = UgTextSecondary,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onApproveConfirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Aprobar", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDialogDismissed) {
                    Text("Cancelar", fontSize = 12.sp, color = UgTextSecondary)
                }
            }
        )
    }

    // Diálogo de confirmación de rechazo
    if (uiState.showRejectDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDialogDismissed,
            containerColor = Color.White,
            title = {
                Text("¿Rechazar la solicitud?", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            },
            text = {
                Text(
                    "El residente ${uiState.request?.nombreSolicitante ?: ""} no podrá vincularse con este código.",
                    fontSize = 13.sp,
                    color = UgTextSecondary,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onRejectConfirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = UgErrorText),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Rechazar", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDialogDismissed) {
                    Text("Cancelar", fontSize = 12.sp, color = UgTextSecondary)
                }
            }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(UgEmerald),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = request.nombreSolicitante,
            color = UgNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = request.emailSolicitante,
            color = UgTextSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(2.dp))
        DetailStatusBadge(status = LinkRequestState.fromString(request.estado))
    }
}

@Composable
private fun DetailStatusBadge(status: LinkRequestState) {
    val (bg, content) = when (status) {
        LinkRequestState.PENDIENTE -> UgAmberSoft to UgAmber
        LinkRequestState.APROBADA -> Color(0xFFECFDF5) to UgEmerald
        LinkRequestState.RECHAZADA -> UgErrorBg to UgErrorText
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = when (status) {
                LinkRequestState.PENDIENTE -> "Pendiente de revisión"
                LinkRequestState.APROBADA -> "Solicitud aprobada"
                LinkRequestState.RECHAZADA -> "Solicitud rechazada"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = content
        )
    }
}

// ─────────────────────────────────────────────
// Filas de información
// ─────────────────────────────────────────────

@Composable
private fun InfoCard(request: LinkRequest) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(vertical = 4.dp)
    ) {
        InfoRow(label = "Conjunto", value = request.conjuntoNombre)
        CardDivider()
        InfoRow(label = "Fecha de solicitud", value = request.fechaSolicitud)
        CardDivider()
        InfoRow(label = "Código usado", value = request.codigoVinculacion)
        CardDivider()
        InfoRow(label = "Unidad", value = request.unidad)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = UgTextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = UgTextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF1F5F9)
    )
}