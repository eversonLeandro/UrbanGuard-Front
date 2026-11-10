package co.edu.unicauca.urbanguard.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgWarningBg
import co.edu.unicauca.urbanguard.ui.theme.UgWarningText

/**
 * Pantalla de error para cuando no hay nada que mostrar porque la carga falló.
 *
 * Recibe un [AppError] tipado y no un texto suelto: de ahí salen el icono, el
 * título y el color, de modo que el usuario puede distinguir de un vistazo si
 * se le venció la sesión, si no tiene internet o si el recurso no existe.
 */
@Composable
fun ErrorView(
    error: AppError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val presentation = remember(error) { presentationFor(error) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(presentation.background, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = presentation.icon,
                contentDescription = null,
                tint = presentation.accent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = titleFor(error),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = error.message,
            fontSize = 14.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Reintentar", fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * Banner de error para fallos que no vacían la pantalla (una acción que falló
 * dentro de una pantalla que ya cargó bien).
 */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(UgErrorBg, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = UgErrorText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(message, color = UgErrorText, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
        if (onRetry != null) {
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 10.dp,
                    vertical = 0.dp
                )
            ) {
                Text("Reintentar", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** Título corto y explícito: es lo que permite identificar el error de un vistazo. */
private fun titleFor(error: AppError): String = when (error) {
    is AppError.NoConnection -> "Sin conexión"
    is AppError.Timeout -> "Tiempo de espera agotado"
    is AppError.SessionExpired -> "Sesión expirada"
    is AppError.ReauthRequired -> "Confirma tu identidad"
    is AppError.InvalidCredentials -> "No pudimos verificar tus datos"
    is AppError.EmailAlreadyRegistered -> "Correo ya registrado"
    is AppError.EmailNotVerified -> "Cuenta sin verificar"
    is AppError.WeakPassword -> "Contraseña insegura"
    is AppError.AccountBlocked -> "Cuenta bloqueada"
    is AppError.AccountDisabled -> "Cuenta deshabilitada"
    is AppError.ExtraVerificationRequired -> "Verificación adicional"
    is AppError.Forbidden -> "Sin permisos"
    is AppError.NotFound -> "No encontrado"
    is AppError.Conflict -> "Acción no disponible"
    is AppError.RouteChangeBlocked -> "Ronda en curso"
    is AppError.RateLimited -> "Demasiados intentos"
    is AppError.ServerError -> "Servicio caído"
    is AppError.Validation -> "Datos inválidos"
    is AppError.ImageTooLarge -> "Imagen demasiado grande"
    is AppError.UnsupportedImage -> "Formato no compatible"
    is AppError.Unexpected -> "Error inesperado"
}

private data class Presentation(
    val icon: ImageVector,
    val accent: Color,
    val background: Color
)

private fun presentationFor(error: AppError): Presentation = when (error) {
    is AppError.NoConnection -> Presentation(Icons.Default.CloudOff, UgWarningText, UgWarningBg)
    is AppError.Timeout -> Presentation(Icons.Default.Timer, UgWarningText, UgWarningBg)
    is AppError.SessionExpired -> Presentation(Icons.Default.Lock, UgWarningText, UgWarningBg)
    is AppError.InvalidCredentials -> Presentation(Icons.Default.Lock, UgWarningText, UgWarningBg)
    is AppError.WeakPassword -> Presentation(Icons.Default.Shield, UgWarningText, UgWarningBg)
    is AppError.RateLimited -> Presentation(Icons.Default.Timer, UgWarningText, UgWarningBg)
    is AppError.NotFound -> Presentation(Icons.Default.SearchOff, UgNavy, UgWarningBg)
    is AppError.Forbidden -> Presentation(Icons.Default.Block, UgErrorText, UgErrorBg)
    else -> Presentation(Icons.Default.ErrorOutline, UgErrorText, UgErrorBg)
}
