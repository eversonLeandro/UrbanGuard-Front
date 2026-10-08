package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.VisitorValidationFailure
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Tarjeta de resultado de la validación de un pase QR (flujo guardia).
 *
 * Vive en archivo propio para no superar el máximo de funciones por archivo
 * de detekt. La pantalla ([GuardScanQrScreen]) se queda con cámara, overlay
 * y permiso; aquí solo el resultado validado contra el backend.
 */

// Estados visuales de un pase ya resuelto por el backend.
enum class PassStatus {
    APPROVED, EXPIRED, USED
}

// Resumen de la visita mostrada en la tarjeta de acceso autorizado.
data class ScanSummary(
    val visitorName: String,
    val extraGuests: Int,
    val destination: String
)

// Información visual de cada estado de la tarjeta.
data class PassStatusInfo(
    val color: Color,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val summary: ScanSummary? = null
)

fun getStatusInfo(status: PassStatus): PassStatusInfo {
    return when (status) {
        PassStatus.APPROVED -> PassStatusInfo(
            color = UgEmerald,
            icon = Icons.Default.CheckCircle,
            title = "Acceso Autorizado",
            description = "El pase QR es válido. Permita el ingreso al visitante."
        )
        PassStatus.EXPIRED -> PassStatusInfo(
            color = Color(0xFFE53935), // Rojo
            icon = Icons.Default.Cancel,
            title = "Pase Expirado",
            description = "Este pase fue emitido para una fecha anterior y ya no es válido."
        )
        PassStatus.USED -> PassStatusInfo(
            color = Color(0xFFF57C00), // Naranja
            icon = Icons.Default.Info,
            title = "Pase Ya Utilizado",
            description = "Este pase ya fue registrado previamente en la portería."
        )
    }
}

@Composable
fun ScanResultContent(
    state: GuardScanQrUiState,
    onScanAgain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val statusInfo = when {
        state.isLoading -> null
        state.result != null -> {
            val result = state.result
            PassStatusInfo(
                color = UgEmerald,
                icon = Icons.Default.CheckCircle,
                title = "Acceso Autorizado",
                description = "El pase QR es válido. Permita el ingreso al visitante.",
                summary = ScanSummary(
                    visitorName = result.visitorName,
                    extraGuests = (result.guestCount - 1).coerceAtLeast(0),
                    destination = "Visita del ${result.visitDate}"
                )
            )
        }
        state.failure != null -> failureStatusInfo(state.failure, state.errorMessage)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(color = UgNavy)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Validando pase…",
                    fontSize = 15.sp,
                    color = UgTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            statusInfo != null -> {
                StatusCard(statusInfo = statusInfo)
                Spacer(Modifier.height(40.dp))
                ScanResultActions(onScanAgain = onScanAgain, onNavigateBack = onNavigateBack)
            }
            else -> {
                ScanResultActions(onScanAgain = onScanAgain, onNavigateBack = onNavigateBack)
            }
        }
    }
}

fun failureStatusInfo(
    failure: VisitorValidationFailure,
    message: String?
): PassStatusInfo {
    val red = Color(0xFFE53935)
    val orange = Color(0xFFF57C00)
    return when (failure) {
        VisitorValidationFailure.USED -> getStatusInfo(PassStatus.USED)
        VisitorValidationFailure.EXPIRED -> getStatusInfo(PassStatus.EXPIRED)
        VisitorValidationFailure.NOT_YET_VALID -> PassStatusInfo(
            color = orange,
            icon = Icons.Default.Info,
            title = "Pase aún no válido",
            description = message ?: "Este pase todavía no es válido para hoy."
        )
        VisitorValidationFailure.INVALID_TOKEN,
        VisitorValidationFailure.NOT_FOUND,
        VisitorValidationFailure.INVALID_FORMAT -> PassStatusInfo(
            color = red,
            icon = Icons.Default.Cancel,
            title = "Pase no válido",
            description = message ?: "El código escaneado no corresponde a una invitación válida."
        )
        VisitorValidationFailure.FORBIDDEN -> PassStatusInfo(
            color = red,
            icon = Icons.Default.Cancel,
            title = "Sin permiso",
            description = message ?: "No tienes permiso para validar pases en este conjunto."
        )
        VisitorValidationFailure.NO_ACTIVE_COMPLEX -> PassStatusInfo(
            color = red,
            icon = Icons.Default.Cancel,
            title = "Sin conjunto activo",
            description = message ?: "No tienes un conjunto activo seleccionado."
        )
        VisitorValidationFailure.SESSION_EXPIRED -> PassStatusInfo(
            color = red,
            icon = Icons.Default.Cancel,
            title = "Sesión expirada",
            description = message ?: "Tu sesión expiró. Inicia sesión de nuevo."
        )
        VisitorValidationFailure.NO_CONNECTION -> PassStatusInfo(
            color = orange,
            icon = Icons.Default.Info,
            title = "Sin conexión",
            description = message ?: "Revisa tu red e intenta de nuevo."
        )
        else -> PassStatusInfo(
            color = red,
            icon = Icons.Default.Cancel,
            title = "No se pudo validar",
            description = message ?: "Ocurrió un error al validar el pase."
        )
    }
}

@Composable
fun StatusCard(statusInfo: PassStatusInfo) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusCardHeader(statusInfo)

            // Solo mostrar el resumen de visita si hay datos (ej. Aprobado)
            if (statusInfo.summary != null) {
                Spacer(Modifier.height(24.dp))
                VisitorSummaryBox(summary = statusInfo.summary)
            }
        }
    }
}

@Composable
fun StatusCardHeader(statusInfo: PassStatusInfo) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(statusInfo.color.copy(alpha = 0.1f), RoundedCornerShape(40.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = statusInfo.icon,
            contentDescription = null,
            tint = statusInfo.color,
            modifier = Modifier.size(48.dp)
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = statusInfo.title,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = statusInfo.color
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = statusInfo.description,
        fontSize = 15.sp,
        color = UgTextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
    )
}

@Composable
fun VisitorSummaryBox(summary: ScanSummary) {
    Surface(
        color = UgBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("👤 Visitante:", fontSize = 12.sp, color = UgTextSecondary)
            Text(
                text = "${summary.visitorName} (+${summary.extraGuests} acomp.)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy
            )
            Spacer(Modifier.height(8.dp))
            Text("🏠 Destino:", fontSize = 12.sp, color = UgTextSecondary)
            Text(
                text = summary.destination,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgNavy
            )
        }
    }
}

@Composable
fun ScanResultActions(onScanAgain: () -> Unit, onNavigateBack: () -> Unit) {
    Button(
        onClick = onScanAgain,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text("Escanear siguiente pase", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }

    Spacer(Modifier.height(16.dp))

    OutlinedButton(
        onClick = onNavigateBack,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = UgNavy),
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text("Volver al menú", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
