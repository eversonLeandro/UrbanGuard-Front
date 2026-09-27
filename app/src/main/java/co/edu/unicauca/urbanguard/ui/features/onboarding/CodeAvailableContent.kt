package co.edu.unicauca.urbanguard.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Estado 4 de la pantalla: el código ya existe y se puede copiar, compartir o
 * regenerar.
 *
 * Vive en su propio archivo porque, junto con el resto de subcomponentes de
 * ConjuntoCodeScreen, superaba el limite de funciones por archivo. Ninguno de
 * los composables de aqui envuelve al anterior, de modo que la secuencia de
 * nodos emitidos dentro de la Column desplazable no cambia.
 */
@Composable
internal fun CodeAvailableContent(
    code: String,
    isCodeActive: Boolean,
    copySuccess: Boolean,
    showRegenerateConfirm: Boolean,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    regenerate: RegenerateActions
) {
    CodeCard(code = code, isCodeActive = isCodeActive)

    // Banner de éxito al copiar (reemplaza el banner de info mientras está activo)
    if (copySuccess) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(UgSuccessBg, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = UgEmerald,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Código copiado al portapapeles",
                fontSize = 13.sp,
                color = UgSuccessText,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        InfoBanner(text = "Comparte este código con los residentes para que soliciten su vinculación al conjunto.")
    }

    CodeActionButtons(
        copySuccess = copySuccess,
        onCopyClick = onCopyClick,
        onShareClick = onShareClick
    )

    // Separador + botón regenerar
    HorizontalDivider(color = UgBorderSoft)

    OutlinedButton(
        onClick = regenerate.onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = UgErrorBright),
        border = androidx.compose.foundation.BorderStroke(1.dp, UgErrorBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Regenerar código", fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }

    // Modal de confirmación de regenerar (inline, no dialog flotante)
    if (showRegenerateConfirm) {
        RegenerateConfirmCard(regenerate = regenerate)
    }
}

/** Card con el código en monoespaciado y el badge de estado. */
@Composable
private fun CodeCard(
    code: String,
    isCodeActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, UgBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "CÓDIGO DE VINCULACIÓN",
            fontSize = 11.sp,
            color = UgTextSecondary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = code,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy,
            letterSpacing = 6.sp,
            fontFamily = FontFamily.Monospace
        )
        // Badge estado activo
        Row(
            modifier = Modifier
                .background(UgSuccessBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = UgEmerald,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isCodeActive) "Código activo" else "Código inactivo",
                fontSize = 12.sp,
                color = UgSuccessText
            )
        }
    }
}

/** Boton de copiar y boton de compartir, en ese orden. */
@Composable
private fun CodeActionButtons(
    copySuccess: Boolean,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Button(
        onClick = onCopyClick,
        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (copySuccess) "Copiar de nuevo" else "Copiar código",
            fontWeight = FontWeight.Bold
        )
    }

    Button(
        onClick = onShareClick,
        colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Compartir", fontWeight = FontWeight.Bold)
    }
}

/**
 * Confirmacion en linea de la regeneracion. Se dibuja dentro del flujo, no como
 * dialog flotante, asi que los `weight` de sus dos botones siguen resolviendo
 * contra el RowScope de la Row que emite este composable.
 */
@Composable
private fun RegenerateConfirmCard(regenerate: RegenerateActions) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, UgErrorBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(UgErrorBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = UgErrorBright,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "¿Regenerar el código?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = UgNavy
            )
        }
        Text(
            text = "El código actual quedará inválido. " +
                "Los residentes que aún no se han vinculado deberán usar el nuevo código.",
            fontSize = 12.sp,
            color = UgTextSecondary,
            lineHeight = 18.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = regenerate.onDismissed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text("Cancelar", fontSize = 12.sp)
            }
            Button(
                onClick = regenerate.onConfirmed,
                colors = ButtonDefaults.buttonColors(containerColor = UgErrorBright),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text("Sí, regenerar", fontSize = 12.sp)
            }
        }
    }
}
