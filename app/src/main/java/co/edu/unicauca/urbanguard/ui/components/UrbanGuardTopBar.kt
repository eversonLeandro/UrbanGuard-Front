package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Barra superior estilizada: degradado marino, bordes inferiores curvos,
 * contenedor iluminado para el logo y acciones a la derecha.
 *
 * El cierre de sesion vive en el perfil, asi que las vistas de rol muestran la
 * campana de notificaciones en lugar de un menu hamburguesa. [onLogout] queda
 * disponible solo para los flujos que no tienen perfil (superadmin y la
 * seleccion de conjunto), donde sigue siendo la unica via de salida.
 */
@Composable
fun UrbanGuardTopBar(
    title: String,
    onNotificationsClick: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
            )
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(UgNavyGradient)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        TopBarLeading(onNavigateBack = onNavigateBack)

        // Título centrado
        TopBarTitle(title)

        TopBarActions(
            onNotificationsClick = onNotificationsClick,
            onLogout = onLogout,
            onShowLogoutDialog = { showLogoutDialog = true }
        )
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout?.invoke()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

/**
 * Lado izquierdo: botón de volver donde hay navegación, logo donde no la hay.
 *
 * Es extensión de [BoxScope] porque se posiciona con `Modifier.align`, que solo
 * existe dentro del ámbito del Box que la contiene. Lo mismo aplica a
 * [TopBarTitle] y [TopBarActions].
 */
@Composable
private fun BoxScope.TopBarLeading(onNavigateBack: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.CenterStart)
    ) {
        if (onNavigateBack != null) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = UgEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_u),
                    contentDescription = "Logo UrbanGuard",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
        }
    }
}

/** Título centrado sobre el degradado. */
@Composable
private fun BoxScope.TopBarTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.3.sp,
        modifier = Modifier.align(Alignment.Center)
    )
}

/**
 * Lado derecho: campana y salida. El separador entre ambas solo aparece cuando
 * las dos están presentes, para que la salida no quede pegada a la campana.
 */
@Composable
private fun BoxScope.TopBarActions(
    onNotificationsClick: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    onShowLogoutDialog: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.CenterEnd)
    ) {
        if (onNotificationsClick != null) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = UgEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (onLogout != null) {
            if (onNotificationsClick != null) Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onShowLogoutDialog,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = UgEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = UgNavy) },
        text = {
            Text(
                "¿Seguro que deseas cerrar la sesión actual?",
                color = UgTextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sí, cerrar sesión", color = UgErrorText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = UgTextSecondary, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White
    )
}
