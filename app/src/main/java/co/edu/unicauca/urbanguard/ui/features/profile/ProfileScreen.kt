package co.edu.unicauca.urbanguard.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import co.edu.unicauca.urbanguard.ui.navigation.Routes
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import kotlinx.coroutines.launch

private const val DRAWER_WIDTH = 300

@Composable
fun ProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    fun navigateTo(route: String) {
        scope.launch { drawerState.close() }
        navController.navigate(route)
    }

    // El drawer desliza desde la DERECHA invirtiendo la dirección de diseño
    // (start = derecha en RTL) solo para el ModalNavigationDrawer.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ResidentDrawer(
                        onClose = { scope.launch { drawerState.close() } },
                        onNavigate = { navigateTo(it) },
                        onLogoutClick = {
                            scope.launch { drawerState.close() }
                            showLogoutDialog = true
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                ProfileContent(
                    uiState = uiState,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.onLogout()
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onOpenDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UgNavy)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                text = "Perfil",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Abrir menú",
                    tint = UgEmerald
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (uiState.userName.isNotBlank()) uiState.userName else "Residente",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgNavy
                    )
                    if (uiState.userRole.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = UgEmerald.copy(alpha = 0.12f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = uiState.userRole,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = UgEmerald
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Toca el menú de la esquina superior derecha\npara acceder a las opciones de Residente.",
                    fontSize = 14.sp,
                    color = UgTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ResidentDrawer(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(DRAWER_WIDTH.dp),
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UgNavy)
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(UgEmerald, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("UG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("UrbanGuard", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar menú",
                        tint = UgEmerald
                    )
                }
            }

            DrawerSection(
                title = "MI CONJUNTO",
                items = listOf(
                    DrawerEntry("Información del conjunto", Icons.Default.Apartment, Routes.CONJUNTO_INFO),
                    DrawerEntry("Reservar espacios comunes", Icons.Default.CalendarMonth, Routes.RESERVATIONS),
                    DrawerEntry("Comunicados", Icons.Default.Campaign, Routes.NEWS)
                ),
                onNavigate = onNavigate
            )

            DrawerSection(
                title = "MIS SERVICIOS",
                items = listOf(
                    DrawerEntry("Mis paquetes", Icons.Default.Inventory2, Routes.PACKAGES),
                    DrawerEntry("Seguridad", Icons.Default.Security, Routes.SECURITY)
                ),
                onNavigate = onNavigate
            )

            Text(
                text = "CUENTA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = UgTextSecondary,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 16.dp, bottom = 4.dp)
            )

            DrawerRow(
                icon = Icons.Default.Notifications,
                label = "Notificaciones",
                tint = UgNavy
            ) {
                onNavigate(Routes.NOTIFICATIONS)
            }

            DrawerRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = "Cerrar sesión",
                tint = UgErrorText,
                isDestructive = true
            ) {
                onLogoutClick()
            }
        }
    }
}

private data class DrawerEntry(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
private fun DrawerSection(
    title: String,
    items: List<DrawerEntry>,
    onNavigate: (String) -> Unit
) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        color = UgTextSecondary,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 16.dp, bottom = 4.dp)
    )
    items.forEach { entry ->
        DrawerRow(
            icon = entry.icon,
            label = entry.label,
            tint = UgNavy
        ) {
            onNavigate(entry.route)
        }
    }
}

@Composable
private fun DrawerRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = tint,
            fontWeight = if (isDestructive) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = UgNavy) },
        text = { Text("¿Seguro que deseas cerrar sesión?", color = UgTextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sí, cerrar sesión", color = UgErrorText, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = UgTextSecondary)
            }
        },
        containerColor = Color.White
    )
}