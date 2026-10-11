package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgEmeraldLight
import co.edu.unicauca.urbanguard.ui.theme.UgHeroGradient
import co.edu.unicauca.urbanguard.ui.theme.UgInfoIcon
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun GuardHomeScreen(
    onNotificationsClick: (() -> Unit)? = null,
    onScanQr: () -> Unit = {},
    onReportIncident: () -> Unit = {},
    viewModel: GuardHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Panel de Vigilancia",
            onNotificationsClick = onNotificationsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Tarjeta de Bienvenida del Guardia ──────────────────
            GuardWelcomeCard(userName = uiState.userName)

            // ── Métricas del Día (Tarjetas Idénticas) ────────────────
            Text(
                "Resumen del Turno",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GuardMetricCard(
                    count = "14",
                    label = "Visitantes",
                    icon = Icons.Default.People,
                    color = UgEmerald,
                    modifier = Modifier.weight(1f)
                )
                GuardMetricCard(
                    count = "3",
                    label = "Paquetes",
                    icon = Icons.Default.Inventory2,
                    color = UgInfoIcon,
                    modifier = Modifier.weight(1f)
                )
                GuardMetricCard(
                    count = "4",
                    label = "Rondas",
                    icon = Icons.Default.Shield,
                    color = UgAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Accesos Rápidos de Guardia ──────────────────────────
            Text("Acciones Rápidas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UgNavy)

            GuardQuickActions(onScanQr = onScanQr, onReportIncident = onReportIncident)

            // ── Bitácora Reciente ───────────────────────────────────
            Text(
                "Actividad Reciente en Portería",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )

            GuardRecentActivityCard()
        }
    }
}

/**
 * Tarjeta de bienvenida en degradado, con la inicial del guardia y su badge.
 *
 * El `weight(1f)` del bloque de texto sigue resolviendo contra el RowScope de la
 * Row que se emite aqui dentro, asi que la fila se reparte igual que antes.
 */
@Composable
private fun GuardWelcomeCard(userName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = UgNavy.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(UgHeroGradient)
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(UgEmerald.copy(alpha = 0.2f))
                        .border(1.5.dp, UgEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userName.isNotBlank()) userName.take(1)
                            .uppercase() else "G",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "¡Bienvenido!",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = if (userName.isNotBlank()) {
                            userName
                        } else {
                            "guardia@urbanguard.com"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(UgEmerald.copy(alpha = 0.25f))
                    .border(1.dp, UgEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "GUARDIA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgEmeraldLight
                )
            }
        }
    }
}

/**
 * Acciones rapidas del guardia: escaneo QR y reporte de novedades.
 */
@Composable
private fun GuardQuickActions(onScanQr: () -> Unit, onReportIncident: () -> Unit) {
    Button(
        onClick = onScanQr,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = UgEmerald.copy(alpha = 0.4f)
            )
    ) {
        Icon(
            Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Escanear Pase QR de Ingreso",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    OutlinedButton(
        onClick = onReportIncident,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, UgAmber),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = UgAmber),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Icon(
            Icons.Default.ReportProblem,
            contentDescription = null,
            tint = UgAmber,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Reportar novedad",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy
        )
    }
}

/** Bitacora reciente de la porteria. */
@Composable
private fun GuardRecentActivityCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GuardLogItem(
                time = "10:42 AM",
                title = "Ingreso Peatonal Autorizado",
                subtitle = "Juan Pérez - Apt 402",
                status = "Permitido",
                statusColor = UgEmerald
            )
            HorizontalDivider(color = UgBorderSoft)
            GuardLogItem(
                time = "09:15 AM",
                title = "Recepción de Paquete",
                subtitle = "Servientrega -> Apt 101",
                status = "Recibido",
                statusColor = UgInfoIcon
            )
        }
    }
}

@Composable
private fun GuardMetricCard(
    count: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UgNavy)
                Text(
                    label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun GuardLogItem(
    time: String,
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UgTextSecondary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UgNavy)
            }
            Text(subtitle, fontSize = 12.sp, color = UgTextSecondary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
        }
    }
}
