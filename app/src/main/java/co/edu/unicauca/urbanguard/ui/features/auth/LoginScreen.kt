package co.edu.unicauca.urbanguard.ui.features.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import kotlinx.coroutines.delay

/**
 * Pantalla de login con rediseño visual.
 *
 * Solo se mejora la presentación (animaciones, tarjetas, jerarquía y foco):
 * la lógica de autenticación, estados, validaciones y navegación son
 * exactamente las de [LoginViewModel].
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (role: UserRole) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToRegister: () -> Unit = onNavigateToOnboarding,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            Toast.makeText(
                context,
                "Inicio de sesión exitoso",
                Toast.LENGTH_SHORT
            ).show()
            val role = uiState.userRole ?: UserRole.USER
            onLoginSuccess(role)
            viewModel.onLoginHandled()
        }
    }

    // Distinción tema claro/oscuro para preparar la pantalla sin hardcodear.
    val isDark = isDarkTheme()
    val colors = LoginColors(
        cardColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
        textPrimary = if (isDark) MaterialTheme.colorScheme.onSurface else UgNavy,
        textSecondary = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else UgTextSecondary,
        inputBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White,
        borderColor = if (isDark) MaterialTheme.colorScheme.outline else UgBorder
    )

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 640

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = if (isSmallScreen) 24.dp else 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Bloque logo + título ─────────────────────────────────────────
        LoginEntranceBlock(step = 0) {
            LoginBrandBlock(textPrimary = colors.textPrimary)
        }

        Spacer(Modifier.height(if (isSmallScreen) 20.dp else 30.dp))

        // ── Encabezado de bienvenida ─────────────────────────────────────
        LoginEntranceBlock(step = 1) {
            Text(
                "Bienvenido",
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Inicia sesión para continuar",
                color = colors.textSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Card del formulario ──────────────────────────────────────────
        LoginEntranceBlock(step = 2) {
            LoginFormCard(
                uiState = uiState,
                viewModel = viewModel,
                colors = colors
            )
        }

        Spacer(Modifier.height(22.dp))

        // ── Enlace "Regístrate" ──────────────────────────────────────────
        LoginEntranceBlock(step = 3) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Nuevo en UrbanGuard? ", color = colors.textSecondary, fontSize = 12.5.sp)
                AnimatedUnderlineText(
                    text = "Empezar aquí",
                    color = UgEmerald,
                    fontSize = 12.5.sp,
                    onClick = onNavigateToRegister
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Bloques visuales
// ─────────────────────────────────────────────

/** Titulo y logo de la pantalla. */
@Composable
private fun LoginBrandBlock(textPrimary: Color) {
    Text(
        "UrbanGuard",
        color = textPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.4.sp
    )
    Spacer(Modifier.height(12.dp))
    Image(
        painter = painterResource(R.drawable.logo_u),
        contentDescription = "Logo UrbanGuard",
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
    )
}

/**
 * Envuelve un bloque con fade-in + slide-up escalonado por [step].
 * Solo anima la primera composición (la pantalla carga una vez).
 */
@Composable
private fun LoginEntranceBlock(
    step: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150L + step * 90L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(380)) +
                slideInVertically(animationSpec = tween(380)) { it / 4 }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
private fun isDarkTheme(): Boolean {
    val systemDark = isSystemInDarkTheme()
    // Dummy uso del scheme para futura compatibilidad oscura;
    // hoy la app es light-only, por eso se respeta systemDark.
    return systemDark
}
