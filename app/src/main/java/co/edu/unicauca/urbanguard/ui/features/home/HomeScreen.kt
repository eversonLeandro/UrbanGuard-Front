package co.edu.unicauca.urbanguard.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.*

@Composable
fun HomeScreen(
    onNotificationsClick: (() -> Unit)? = null,
    onOpenConjuntoSelection: () -> Unit = {},
    onOpenConjuntoCode: () -> Unit = {},
    onOpenPendingRequests: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "UrbanGuard",
            onNotificationsClick = onNotificationsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Tarjeta Héroe de Bienvenida ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), ambientColor = UgNavy.copy(alpha = 0.3f), spotColor = UgNavy.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(UgHeroGradient)
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
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
                                text = if (uiState.userName.isNotBlank()) uiState.userName.take(1).uppercase() else "U",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "¡Hola de nuevo!",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = if (uiState.userName.isNotBlank()) uiState.userName else "Usuario UrbanGuard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (uiState.userRole.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(UgEmerald.copy(alpha = 0.25f))
                                        .border(1.dp, UgEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 9.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = uiState.userRole,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = UgEmeraldLight
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        HeroStatItem(label = "Estado", value = "Activo", icon = Icons.Default.Shield)
                        HeroStatItem(
                            label = "Conjunto",
                            value = uiState.conjuntoName.ifBlank { "Sin conjunto" },
                            icon = Icons.Default.Key
                        )
                    }
                }
            }

            // ── Acciones Administrativas (solo ADMIN) ───────────────────
            if (uiState.role == UserRole.ADMIN) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Gestión de Administración",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminQuickActionCard(
                            title = "Compartir Código",
                            subtitle = "Invitar residentes",
                            icon = Icons.Default.Key,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenConjuntoCode
                        )
                        AdminQuickActionCard(
                            title = "Solicitudes",
                            subtitle = "Aprobar accesos",
                            icon = Icons.Default.PersonAdd,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenPendingRequests
                        )
                    }
                }
            }

            // ── Accesos Rápidos de Comunidad ────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Servicios Principales",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgNavy
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeActionCard(
                        title = "Pases QR",
                        subtitle = "Invitar visitantes",
                        icon = Icons.Default.QrCodeScanner,
                        badgeColor = UgEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    HomeActionCard(
                        title = "Paquetes",
                        subtitle = "Entregas en portería",
                        icon = Icons.Default.Inventory2,
                        badgeColor = UgInfoIcon,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeActionCard(
                        title = "Comunicados",
                        subtitle = "Noticias del conjunto",
                        icon = Icons.Default.Campaign,
                        badgeColor = UgAmber,
                        modifier = Modifier.weight(1f)
                    )
                    HomeActionCard(
                        title = "PQRS & Reportes",
                        subtitle = "Atención al residente",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        badgeColor = UgNavy,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Selector de conjunto ───────────────────────────────────
            // Reemplaza el banner informativo anterior, que repetia lo que ya
            // dice la tarjeta "Comunicados" de la seccion de servicios.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, UgBorder, RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenConjuntoSelection)
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(UgEmerald.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = UgEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seleccionar conjunto", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = UgNavy)
                        Text(
                            text = if (uiState.conjuntoName.isNotBlank()) {
                                uiState.conjuntoName
                            } else {
                                "Toca para ver los conjuntos vinculados a tu cuenta"
                            },
                            fontSize = 12.sp,
                            color = UgTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = UgTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = UgEmeraldLight, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        // weight + ellipsis: los nombres de conjunto reales son largos y sin limite
        // romperian el reparto de la fila contra el stat de "Estado".
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = UgNavy)
                Text(subtitle, fontSize = 11.5.sp, color = UgTextSecondary, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun AdminQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UgNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(80.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(UgEmerald.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = UgEmerald, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                Text(subtitle, fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

