package co.edu.unicauca.urbanguard.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.theme.*

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onOpenConjuntoCode: () -> Unit = {},
    onOpenPendingRequests: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        Spacer(Modifier.height(20.dp))
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UgNavy)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(UgEmerald, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("UG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text("UrbanGuard", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            // Menú hamburguesa — solo visible para ADMIN
            if (uiState.role == UserRole.ADMIN) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = UgEmerald
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configurar datos del conjunto") },
                            onClick = {
                                menuExpanded = false
                                // TODO: definir si reutiliza RegisterConjuntoScreen
                                // en modo edición o se crea EditConjuntoScreen
                                // — pendiente de decisión
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Generar / compartir código") },
                            onClick = {
                                menuExpanded = false
                                onOpenConjuntoCode()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Aprobar solicitudes de vinculación") },
                            onClick = {
                                menuExpanded = false
                                onOpenPendingRequests()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                menuExpanded = false
                                onLogout()
                                viewModel.onLogout()
                            }
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "¡Bienvenido!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = UgNavy
                    )
                    if (uiState.userName.isNotBlank()) {
                        Text(
                            text = uiState.userName,
                            fontSize = 16.sp,
                            color = UgTextSecondary
                        )
                    }
                    if (uiState.userRole.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = UgEmerald.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
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

            // Placeholder content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(UgBackground, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = UgEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "Dashboard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = UgNavy
                    )
                    Text(
                        text = "Próximamente las funcionalidades\nde tu comunidad estarán aquí.",
                        fontSize = 13.sp,
                        color = UgTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
