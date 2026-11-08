package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import kotlinx.coroutines.delay

@Composable
internal fun RegisterUnitButton(
    isRegistering: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isRegistering,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UgNavy,
            disabledContainerColor = UgBorder
        )
    ) {
        Text(
            text = if (isRegistering) "Agregando..." else "+ Agregar apartamento",
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun RegisterUnitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar apartamento") },
        text = {
            Text(
                text = "Se creará un nuevo apartamento con el siguiente número disponible en este conjunto.",
                color = UgTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Agregar", color = UgEmerald, fontWeight = FontWeight.Bold)
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

@Composable
internal fun RegistrationFeedback(
    successMessage: String?,
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    val message = successMessage ?: errorMessage ?: return
    val isSuccess = successMessage != null
    val accent = if (isSuccess) UgSuccessText else UgErrorText
    val background = if (isSuccess) UgSuccessBg else UgErrorBg

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(SUCCESS_MESSAGE_DURATION)
            onDismiss()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                color = accent,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar mensaje",
                    tint = accent
                )
            }
        }
    }
}

private const val SUCCESS_MESSAGE_DURATION = 4_000L
