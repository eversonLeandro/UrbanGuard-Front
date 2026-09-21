package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.theme.*

@Composable
fun ConjuntoSelectionScreen(
    onConjuntoSelected: (conjuntoId: String) -> Unit,
    onCreateConjunto: () -> Unit,
    onJoinByCode: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ConjuntoSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        // Header
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
                Image(
                    painter = painterResource(R.drawable.logo_u),
                    contentDescription = "Logo UrbanGuard",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Seleccionar conjunto",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            IconButton(onClick = { showLogoutDialog = true }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Cerrar sesión",
                    tint = UgEmerald
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = UgNavy)
                }
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage!!,
                    onRetry = viewModel::loadConjuntos
                )
            }

            else -> {
                if (uiState.conjuntos.isEmpty()) {
                    EmptyState(onCreateConjunto = onCreateConjunto, onJoinByCode = onJoinByCode)
                } else {
                    ConjuntoList(
                        userName = uiState.userName,
                        conjuntos = uiState.conjuntos,
                        conjuntoActivoId = uiState.conjuntoActivoId,
                        onConjuntoSelected = { id ->
                            viewModel.onConjuntoSelected(id)
                            onConjuntoSelected(id)
                        },
                        onCreateConjunto = onCreateConjunto,
                        onJoinByCode = onJoinByCode
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun ConjuntoList(
    userName: String,
    conjuntos: List<ConjuntoUserAssociation>,
    conjuntoActivoId: String?,
    onConjuntoSelected: (String) -> Unit,
    onCreateConjunto: () -> Unit,
    onJoinByCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = if (userName.isNotBlank()) "Hola, $userName" else "Hola",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy
        )
        Text(
            text = "Selecciona en qué conjunto quieres entrar",
            fontSize = 13.sp,
            color = UgTextSecondary
        )
        Spacer(Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(conjuntos, key = { it.conjuntoId }) { conjunto ->
                ConjuntoCard(
                    conjunto = conjunto,
                    isActive = conjunto.conjuntoId == conjuntoActivoId,
                    onClick = { onConjuntoSelected(conjunto.conjuntoId) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        ActionButtons(
            onCreateConjunto = onCreateConjunto,
            onJoinByCode = onJoinByCode
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ConjuntoCard(
    conjunto: ConjuntoUserAssociation,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(UgEmerald.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = conjunto.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgNavy
                )
                RoleBadge(role = conjunto.roleEnConjunto)
            }
            if (isActive) {
                Text(
                    text = "Activo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgEmerald
                )
            }
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    val (label, color) = when (role) {
        UserRole.ADMIN -> "Administrador" to UgNavy
        UserRole.GUARD -> "Vigilante" to UgAmber
        UserRole.RESIDENT -> "Residente" to UgEmerald
        UserRole.USER -> "Usuario" to UgTextSecondary
        UserRole.SUPERADMIN -> "Superadmin" to UgNavy
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun EmptyState(
    onCreateConjunto: () -> Unit,
    onJoinByCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(UgBackground, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = UgEmerald,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Aún no estás en ningún conjunto",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = UgNavy
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Crea un nuevo conjunto o únete a uno con un código de vinculación.",
            fontSize = 13.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.weight(1f))
        ActionButtons(
            onCreateConjunto = onCreateConjunto,
            onJoinByCode = onJoinByCode
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ActionButtons(
    onCreateConjunto: () -> Unit,
    onJoinByCode: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onCreateConjunto,
            colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Crear nuevo conjunto",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
        OutlinedButton(
            onClick = onJoinByCode,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(UgNavy)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Unirse a un conjunto",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(UgNavy)
            )
        ) {
            Text("Reintentar", fontWeight = FontWeight.Medium)
        }
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