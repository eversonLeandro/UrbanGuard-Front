package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.LinkRequest
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Dialogos de confirmacion de la revision de una solicitud.
 *
 * Viven aparte de [LinkRequestDetailScreen] porque la pantalla ya acumula
 * demasiadas funciones y porque aprobar y rechazar son dos decisiones
 * distintas: comparten el armazon del dialogo pero no su contenido, asi que
 * cada una conserva sus textos y su color sin condiciones a medio camino.
 *
 * `visible` se resuelve con una salida temprana para que la pantalla pueda
 * invocarlas siempre y no tenga que envolver cada llamada en un `if`.
 */

@Composable
internal fun ApproveRequestDialog(
    visible: Boolean,
    request: LinkRequest?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("¿Aprobar la solicitud?", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        },
        text = {
            Text(
                "El residente ${request?.nombreSolicitante ?: ""} será vinculado a " +
                    "${request?.conjuntoNombre ?: "su conjunto"}.",
                fontSize = 13.sp,
                color = UgTextSecondary,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Aprobar", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontSize = 12.sp, color = UgTextSecondary)
            }
        }
    )
}

@Composable
internal fun RejectRequestDialog(
    visible: Boolean,
    request: LinkRequest?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("¿Rechazar la solicitud?", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        },
        text = {
            Text(
                "El residente ${request?.nombreSolicitante ?: ""} no podrá vincularse con este código.",
                fontSize = 13.sp,
                color = UgTextSecondary,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = UgErrorText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Rechazar", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontSize = 12.sp, color = UgTextSecondary)
            }
        }
    )
}
