package co.edu.unicauca.urbanguard.ui.features.profile

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.components.LogoutDialog
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgIconMuted
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Suppress("LongMethod")
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onOpenTermsPrivacy: () -> Unit = {},
    onOpenHelp: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    PasswordChangeTicketEffect(
        ticketUrl = uiState.ticketUrl,
        onConsumed = viewModel::onTicketConsumed
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(title = "Mi Perfil")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 20.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Card de Perfil de Usuario ─────────────────────────────────
            UserProfileCard(
                userName = uiState.userName,
                userRole = uiState.userRole
            )

            // ── Error de cambio de contraseña ─────────────────────────────
            if (uiState.passwordChangeError != null) {
                PasswordChangeErrorBanner(
                    message = uiState.passwordChangeError!!,
                    onDismiss = viewModel::onPasswordChangeErrorDismissed
                )
            }

            // ── Sección: Ajustes de Cuenta ────────────────────────────────
            ProfileSectionCard(title = "Ajustes de Cuenta") {
                ProfileOptionRow(
                    icon = Icons.Default.Person,
                    title = "Información Personal",
                    subtitle = "Datos y contacto"
                )
                ProfileOptionRow(
                    icon = Icons.Default.Lock,
                    title = "Seguridad",
                    subtitle = "Contraseña y acceso",
                    isLoading = uiState.isRequestingTicket,
                    onClick = { viewModel.requestPasswordChange() }
                )
                ProfileOptionRow(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Alertas y avisos"
                )
            }

            // ── Sección: Soporte e Información ─────────────────────────────
            ProfileSectionCard(title = "Soporte e Información") {
                ProfileOptionRow(
                    icon = Icons.Default.Shield,
                    title = "Términos y Privacidad",
                    subtitle = "Políticas del servicio",
                    onClick = onOpenTermsPrivacy
                )
                ProfileOptionRow(
                    icon = Icons.Default.Help,
                    title = "Centro de Ayuda",
                    subtitle = "Preguntas frecuentes",
                    onClick = { onOpenHelp(roleForHelp(uiState.role)) }
                )
            }

            // ── Botón Destacado de Cerrar Sesión ───────────────────────────
            LogoutButton(onClick = { showLogoutDialog = true })
        }
    }

    ProfileDialogs(
        showLogoutDialog = showLogoutDialog,
        needsReauth = uiState.needsReauth,
        onLogout = {
            showLogoutDialog = false
            onLogout()
        },
        onLogoutDismiss = { showLogoutDialog = false },
        onReauthConfirm = { email, password -> viewModel.onReauthSubmit(email, password) },
        onReauthDismiss = viewModel::onReauthDismissed
    )
}

private fun roleForHelp(role: UserRole?): String = when (role) {
    UserRole.ADMIN -> "admin"
    UserRole.GUARD -> "guard"
    UserRole.RESIDENT, UserRole.USER -> "resident"
    else -> "resident"
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileDialogs(
    showLogoutDialog: Boolean,
    needsReauth: Boolean,
    onLogout: () -> Unit,
    onLogoutDismiss: () -> Unit,
    onReauthConfirm: (email: String, password: String) -> Unit,
    onReauthDismiss: () -> Unit
) {
    // ── Diálogo de confirmación de cierre de sesión ───────────────────────
    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = onLogout,
            onDismiss = onLogoutDismiss
        )
    }

    // ── Diálogo de reautenticación ────────────────────────────────────────
    if (needsReauth) {
        ReauthDialog(
            onConfirm = onReauthConfirm,
            onDismiss = onReauthDismiss
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Abre el ticket de cambio de contraseña en un Custom Tab cuando llega.
 *
 * Se consume después de lanzar para no perder la URL si el Custom Tab falla.
 * La URL es una credencial temporal: no se guarda, solo se abre y se descarta.
 */
@Composable
private fun PasswordChangeTicketEffect(
    ticketUrl: String?,
    onConsumed: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(ticketUrl) {
        val url = ticketUrl ?: return@LaunchedEffect
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } finally {
            onConsumed()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Card con la foto inicial, el nombre y el rol.
 */
@Composable
private fun UserProfileCard(
    userName: String,
    userRole: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = UgNavy.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, UgBorder, RoundedCornerShape(24.dp))
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(UgNavyGradient)
                    .border(2.dp, UgEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName.take(1).uppercase() else "U",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (userName.isNotBlank()) userName else "Usuario UrbanGuard",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgNavy
                )
                if (userRole.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(UgEmerald.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = userRole,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = UgEmerald
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/** Banner de error no intrusivo para fallos en el cambio de contraseña. */
@Composable
private fun PasswordChangeErrorBanner(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(UgErrorBg)
            .border(1.dp, UgErrorBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = UgErrorText,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onDismiss) {
                Text("OK", color = UgErrorText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/** Botón destacado de cierre de sesión. */
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UgErrorBg,
            contentColor = UgErrorText
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, UgErrorBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = UgErrorText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Cerrar Sesión",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = UgErrorText
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                content = content
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null && !isLoading) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UgInputBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = UgNavy, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = UgNavy)
            Text(subtitle, fontSize = 12.sp, color = UgTextSecondary)
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = UgNavy
            )
        } else if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = UgIconMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Diálogo de reautenticación para cambio de contraseña.
 *
 * Muestra un mensaje explicativo y solicita el correo y la contraseña del usuario.
 * Se lanza cuando el backend responde REAUTH_REQUIRED (auth_time > 5 minutos).
 */
@Composable
private fun ReauthDialog(
    onConfirm: (email: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Confirma tu identidad",
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Por seguridad, necesitamos verificar tu identidad antes de cambiar la contraseña. " +
                            "Ingresa tus credenciales de acceso.",
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgNavy,
                        focusedLabelColor = UgNavy
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña"
                                else "Mostrar contraseña",
                                tint = UgIconMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgNavy,
                        focusedLabelColor = UgNavy
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy)
            ) {
                Text("Confirmar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = UgTextSecondary)
            }
        }
    )
}
