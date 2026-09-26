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
    val cardColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val textPrimary = if (isDark) MaterialTheme.colorScheme.onSurface else UgNavy
    val textSecondary = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else UgTextSecondary
    val inputBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
    val borderColor = if (isDark) MaterialTheme.colorScheme.outline else UgBorder

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

        Spacer(Modifier.height(if (isSmallScreen) 20.dp else 30.dp))

        // ── Encabezado de bienvenida ─────────────────────────────────────
        LoginEntranceBlock(step = 1) {
            Text(
                "Bienvenido",
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Inicia sesión para continuar",
                color = textSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Card del formulario ──────────────────────────────────────────
        LoginEntranceBlock(step = 2) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Email
                    LoginTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Correo electrónico",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = textSecondary
                            )
                        },
                        keyboardType = KeyboardType.Email,
                        enabled = !uiState.isLoading,
                        isError = uiState.emailError != null,
                        errorText = uiState.emailError,
                        inputBg = inputBg,
                        textSecondary = textSecondary,
                        borderColor = borderColor
                    )

                    // Contraseña
                    LoginTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Contraseña",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = textSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = viewModel::togglePasswordVisibility,
                                enabled = !uiState.isLoading,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (uiState.isPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (uiState.isPasswordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    },
                                    tint = textSecondary
                                )
                            }
                        },
                        visualTransformation = if (uiState.isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = uiState.passwordError != null,
                        errorText = uiState.passwordError,
                        imeAction = ImeAction.Done,
                        onImeDone = { viewModel.onLoginClick() },
                        inputBg = inputBg,
                        textSecondary = textSecondary,
                        borderColor = borderColor
                    )

                    // "¿Olvidaste tu contraseña?"
                    AnimatedUnderlineText(
                        text = "¿Olvidaste tu contraseña?",
                        color = textPrimary,
                        fontSize = 12.5.sp,
                        modifier = Modifier.align(Alignment.End)
                        // TODO: onClick -> flujo de recuperación (fuera del Sprint 1)
                    )

                    // Banner de error con animación
                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = slideInVertically(animationSpec = tween(300)) { it / 2 } +
                                fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(200)) +
                                shrinkVertically(animationSpec = tween(200)) { it / 2 }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(UgErrorBg, RoundedCornerShape(10.dp))
                                .border(
                                    BorderStroke(1.dp, UgErrorText.copy(alpha = 0.15f)),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(animationSpec = tween(250), initialScale = 0.6f)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = UgErrorText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                uiState.errorMessage ?: "",
                                color = UgErrorText,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Botón principal con pulso durante la carga
                    LoginPrimaryButton(
                        isLoading = uiState.isLoading,
                        onClick = viewModel::onLoginClick
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ── Enlace "Regístrate" ──────────────────────────────────────────
        LoginEntranceBlock(step = 3) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Nuevo en UrbanGuard? ", color = textSecondary, fontSize = 12.5.sp)
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

/**
 * Campo de texto del login con foco acentuado (borde + halo) y
 * colores preparados para tema claro/oscuro.
 */
@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    isError: Boolean,
    inputBg: Color,
    textSecondary: Color,
    borderColor: Color,
    trailingIcon: @Composable () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeDone: () -> Unit = {},
    enabled: Boolean = true,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onImeDone() }),
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = errorText?.let { message ->
            { Text(message, color = UgErrorText, fontSize = 12.sp) }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBg,
            unfocusedContainerColor = inputBg,
            errorContainerColor = inputBg,
            focusedBorderColor = UgNavy,
            unfocusedBorderColor = borderColor,
            errorBorderColor = UgErrorText,
            focusedLabelColor = UgNavy,
            unfocusedLabelColor = textSecondary,
            cursorColor = UgNavy,
            focusedLeadingIconColor = UgNavy,
            unfocusedLeadingIconColor = textSecondary,
            focusedTrailingIconColor = textSecondary,
            unfocusedTrailingIconColor = textSecondary,
            errorSupportingTextColor = UgErrorText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isError) 0.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = UgNavy.copy(alpha = 0.08f),
                spotColor = UgNavy.copy(alpha = 0.08f)
            )
    )
}

/**
 * Texto con subrayado animado (fade de color al interactuar).
 * Mantiene el onClick dado (si aplica).
 */
@Composable
private fun AnimatedUnderlineText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            )
        ) {
            Text(
                text = text,
                color = if (isPressed) color.copy(alpha = 0.6f) else color,
                fontWeight = FontWeight.Medium,
                fontSize = fontSize,
                modifier = Modifier.semantics { if (onClick != null) contentDescription = text }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(2.dp)
                .background(color.copy(alpha = if (isPressed) 0.9f else 0.0f))
        )
    }
}

/**
 * Botón principal con sombra que se intensifica al presionarlo y
 * pulso de fondo mientras está cargando.
 */
@Composable
private fun LoginPrimaryButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressedElevation by animateFloatAsState(
        targetValue = if (isPressed) 10f else 4f,
        animationSpec = tween(150)
    )

    // Pulso sutil del fondo durante la carga
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(700)),
        label = "buttonPulse"
    )

    // Durante la carga el contenedor late en todo el botón (no solo el área interna)
    val containerColor = if (isLoading) UgNavy.copy(alpha = pulseAlpha) else UgNavy

    Button(
        onClick = onClick,
        enabled = !isLoading,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor,
            disabledContentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = pressedElevation.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = UgNavy.copy(alpha = 0.35f),
                spotColor = UgNavy.copy(alpha = 0.35f)
            )
    ) {
        if (isLoading) {
            // Spinner + texto en fila (el botón ya centra el contenido horizontalmente)
            CircularProgressIndicator(
                color = UgEmerald,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Ingresando...",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        } else {
            Text(
                "Iniciar sesión",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
private fun isDarkTheme(): Boolean {
    val systemDark = isSystemInDarkTheme()
    // Dummy uso del scheme para futura compatibilidad oscura;
    // hoy la app es light-only, por eso se respeta systemDark.
    return systemDark
}
