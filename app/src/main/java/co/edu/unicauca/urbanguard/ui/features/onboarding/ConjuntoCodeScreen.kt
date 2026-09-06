package co.edu.unicauca.urbanguard.ui.features.onboarding


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.ui.theme.*

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
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UgNavy)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = UgEmerald
                )
            }
            Text(
                text = "Código del conjunto",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre del conjunto
            // TODO: reemplazar el texto quemado por el nombre real del conjunto
            // una vez que UG-45/UG-46 estén implementados.
            // El nombre vendrá del SessionManager o del ConjuntoConfig
            // que se cargará en el init del ViewModel.
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

            Spacer(modifier = Modifier.height(4.dp))

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
                                val clipboard = context.getSystemService(
                                    Context.CLIPBOARD_SERVICE
                                ) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Código conjunto", textToCopy)
                                )
                            }
                        },
                        onShareClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Código de vinculación para ${uiState.conjuntoName}: ${uiState.code}"
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Compartir código")
                            )
                        },
                        onRegenerateClick = viewModel::onRegenerateClick,
                        onRegenerateConfirmed = viewModel::onRegenerateConfirmed,
                        onRegenerateDismissed = viewModel::onRegenerateDismissed
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

// ─────────────────────────────────────────────
// Subcomponentes internos
// ─────────────────────────────────────────────

@Composable
private fun InitialLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
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
                color = Color(0xFFCBD5E1),
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
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
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

    InfoBanner(text = "El código se genera una sola vez. Podrás regenerarlo si es necesario, pero el anterior quedará inválido.")

    Button(
        onClick = onGenerateClick,
        colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
        shape = RoundedCornerShape(8.dp),
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
        Text("Generar código", fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ActionLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
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

@Composable
private fun CodeAvailableContent(
    code: String,
    isCodeActive: Boolean,
    copySuccess: Boolean,
    showRegenerateConfirm: Boolean,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    onRegenerateConfirmed: () -> Unit,
    onRegenerateDismissed: () -> Unit
) {
    // Card con el código
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
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
                .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
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
                color = Color(0xFF065F46)
            )
        }
    }

    // Banner de éxito al copiar (reemplaza el banner de info mientras está activo)
    if (copySuccess) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
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
                color = Color(0xFF065F46),
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
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (copySuccess) "Copiar de nuevo" else "Copiar código",
            fontWeight = FontWeight.Medium
        )
    }

    // Botón compartir
    Button(
        onClick = onShareClick,
        colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Compartir", fontWeight = FontWeight.Medium)
    }

    // Separador + botón regenerar
    Divider(color = Color(0xFFE2E8F0))

    OutlinedButton(
        onClick = onRegenerateClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Regenerar código", fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }

    // Modal de confirmación de regenerar (inline, no dialog flotante)
    if (showRegenerateConfirm) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
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
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
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
                text = "El código actual quedará inválido. Los residentes que aún no se han vinculado deberán usar el nuevo código.",
                fontSize = 12.sp,
                color = UgTextSecondary,
                lineHeight = 18.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRegenerateDismissed,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("Cancelar", fontSize = 12.sp)
                }
                Button(
                    onClick = onRegenerateConfirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
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
            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(16.dp).padding(top = 1.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF1E40AF),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgErrorBg, RoundedCornerShape(8.dp))
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