package co.edu.unicauca.urbanguard.ui.features.onboarding


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgIconFaint
import co.edu.unicauca.urbanguard.ui.theme.UgIconMuted
import co.edu.unicauca.urbanguard.ui.theme.UgInfoBg
import co.edu.unicauca.urbanguard.ui.theme.UgInfoIcon
import co.edu.unicauca.urbanguard.ui.theme.UgInfoText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun ConjuntoCodeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConjuntoCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        // Header unificado
        UrbanGuardTopBar(
            title = "Código del conjunto",
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre del conjunto
            // Pendiente: reemplazar el texto quemado por el nombre real del conjunto
            // una vez que UG-45/UG-46 estén implementados.
            // El nombre vendrá del SessionManager o del ConjuntoConfig
            // que se cargará en el init del ViewModel.
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Conjunto residencial",
                    fontSize = 13.sp,
                    color = UgTextSecondary
                )
                Text(
                    text = uiState.conjuntoName, // <-- NOMBRE QUEMADO TEMPORAL en UiState
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgNavy
                )
            }

            when {
                // Estado 1: carga inicial
                uiState.isLoadingInitial -> {
                    InitialLoadingContent()
                }

                // Estado 2: sin código generado todavía
                uiState.code == null && !uiState.isLoadingAction -> {
                    EmptyCodeContent(
                        onGenerateClick = viewModel::onGenerateClick
                    )
                }

                // Estado 3: acción en progreso (generar o regenerar)
                uiState.isLoadingAction -> {
                    ActionLoadingContent()
                }

                // Estado 4: código disponible (activo o con confirmación de regenerar)
                uiState.code != null -> {
                    CodeAvailableContent(
                        code = uiState.code!!,
                        isCodeActive = uiState.isCodeActive,
                        copySuccess = uiState.copySuccess,
                        showRegenerateConfirm = uiState.showRegenerateConfirm,
                        onCopyClick = {
                            viewModel.onCopyCode(uiState.code!!) { textToCopy ->
                                copyToClipboard(context, textToCopy)
                            }
                        },
                        onShareClick = {
                            shareCode(context, uiState.conjuntoName, uiState.code!!)
                        },
                        regenerate = RegenerateActions(
                            onClick = viewModel::onRegenerateClick,
                            onConfirmed = viewModel::onRegenerateConfirmed,
                            onDismissed = viewModel::onRegenerateDismissed
                        )
                    )
                }
            }

            // Banner de error (aplica a cualquier estado)
            if (uiState.errorMessage != null) {
                ErrorBanner(
                    message = uiState.errorMessage!!,
                    onDismiss = viewModel::onErrorDismissed
                )
            }
        }
    }
}

/** Escribe el código en el portapapeles del sistema bajo la etiqueta del conjunto. */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Código conjunto", text))
}

/** Abre el selector de compartir del sistema con el texto de vinculación listo. */
private fun shareCode(context: Context, conjuntoName: String, code: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Código de vinculación para $conjuntoName: $code")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir código"))
}

// ─────────────────────────────────────────────
// Subcomponentes internos
// ─────────────────────────────────────────────

@Composable
private fun InitialLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, UgBorder, RoundedCornerShape(12.dp))
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = UgEmerald,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun EmptyCodeContent(onGenerateClick: () -> Unit) {
    // Card estado vacío con borde punteado
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = UgIconFaint,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(UgBorderSoft, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = UgIconMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Aún no hay código generado",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = UgNavy,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Genera un código para que los residentes puedan solicitar su vinculación al conjunto.",
                fontSize = 12.sp,
                color = UgTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }

    InfoBanner(
        text = "El código se genera una sola vez. Podrás regenerarlo si es necesario, " +
            "pero el anterior quedará inválido."
    )

    Button(
        onClick = onGenerateClick,
        colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Generar código", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, UgBorder, RoundedCornerShape(12.dp))
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = UgEmerald,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Generando código...",
                fontSize = 13.sp,
                color = UgTextSecondary
            )
        }
    }
}

/**
 * Acciones del diálogo de regeneración del código. Van agrupadas porque siempre
 * viajan juntas: abrir la confirmación, confirmarla y descartarla.
 */
private data class RegenerateActions(
    val onClick: () -> Unit,
    val onConfirmed: () -> Unit,
    val onDismissed: () -> Unit
)

@Composable
private fun CodeAvailableContent(
    code: String,
    isCodeActive: Boolean,
    copySuccess: Boolean,
    showRegenerateConfirm: Boolean,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    regenerate: RegenerateActions
) {
    // Card con el código
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

    // Botón copiar
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

    // Botón compartir
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
}

// ─────────────────────────────────────────────
// Componentes reutilizables
// ─────────────────────────────────────────────

@Composable
fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgInfoBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = UgInfoIcon,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = UgInfoText,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgErrorBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = UgErrorText,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = message,
            fontSize = 12.sp,
            color = UgErrorText,
            modifier = Modifier.weight(1f),
            lineHeight = 18.sp
        )
        TextButton(onClick = onDismiss) {
            Text("OK", color = UgErrorText, fontSize = 12.sp)
        }
    }
}
