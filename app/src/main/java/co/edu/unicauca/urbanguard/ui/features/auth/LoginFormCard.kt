package co.edu.unicauca.urbanguard.ui.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy

/**
 * Card con correo, contraseña, enlace de recuperación, banner de error y boton.
 *
 * Recibe el estado y el ViewModel enteros porque, al mostrar todos los campos
 * a la vez, separarlos en un solo manejador de cambios haria la lista de
 * parametros mas larga que el propio formulario.
 *
 * Vive en su propio archivo porque, junto al resto de subcomponentes de
 * LoginScreen, superaba el limite de funciones por archivo.
 */
@Composable
internal fun LoginFormCard(
    uiState: LoginUiState,
    viewModel: LoginViewModel,
    colors: LoginColors,
    onNavigateToPasswordReset: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardColor),
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
                        tint = colors.textSecondary
                    )
                },
                keyboardType = KeyboardType.Email,
                enabled = !uiState.isLoading,
                isError = uiState.emailError != null,
                errorText = uiState.emailError,
                colors = loginFieldColors(colors.inputBg, colors.textSecondary, colors.borderColor)
            )

            // Contraseña
            LoginPasswordField(
                uiState = uiState,
                viewModel = viewModel,
                colors = colors
            )

            // "¿Olvidaste tu contraseña?"
            AnimatedUnderlineText(
                text = "¿Olvidaste tu contraseña?",
                color = colors.textPrimary,
                fontSize = 12.5.sp,
                modifier = Modifier.align(Alignment.End),
                onClick = { onNavigateToPasswordReset(uiState.email) }
            )

            // Banner de error con animación
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = slideInVertically(animationSpec = tween(300)) { it / 2 } +
                        fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) +
                        shrinkVertically(animationSpec = tween(200)) { it / 2 }
            ) {
                LoginErrorBanner(message = uiState.errorMessage ?: "")
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

/**
 * Campo de contraseña con su boton de visibilidad.
 *
 * El `weight` del texto no aplica aqui: el IconButton conserva su tamano fijo de
 * 48.dp, que es el minimo tactil de Material.
 */
@Composable
private fun LoginPasswordField(
    uiState: LoginUiState,
    viewModel: LoginViewModel,
    colors: LoginColors
) {
    LoginTextField(
        value = uiState.password,
        onValueChange = viewModel::onPasswordChange,
        label = "Contraseña",
        leadingIcon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = colors.textSecondary
            )
        },
        trailingIcon = {
            PasswordVisibilityToggle(
                isVisible = uiState.isPasswordVisible,
                enabled = !uiState.isLoading,
                onClick = viewModel::togglePasswordVisibility,
                tint = colors.textSecondary
            )
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
        colors = loginFieldColors(colors.inputBg, colors.textSecondary, colors.borderColor)
    )
}

/** Boton que alterna la visibilidad de la contraseña. */
@Composable
private fun PasswordVisibilityToggle(
    isVisible: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            if (isVisible) {
                Icons.Default.VisibilityOff
            } else {
                Icons.Default.Visibility
            },
            contentDescription = if (isVisible) {
                "Ocultar contraseña"
            } else {
                "Mostrar contraseña"
            },
            tint = tint
        )
    }
}

/**
 * Banner de error del login, con la animacion de entrada que ya usaba la
 * pantalla. El `weight(1f)` del texto sigue resolviendo contra el RowScope de la
 * Row que se emite aqui dentro.
 */
@Composable
private fun LoginErrorBanner(message: String) {
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
            message,
            color = UgErrorText,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Paleta del campo de texto del login. Vive aparte para que [LoginTextField] no
 * reciba tres colores sueltos: Compose ya tiene un tipo para esto.
 */
@Composable
private fun loginFieldColors(
    inputBg: Color,
    textSecondary: Color,
    borderColor: Color
): TextFieldColors = OutlinedTextFieldDefaults.colors(
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
)

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
    colors: TextFieldColors,
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
        colors = colors,
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
internal fun AnimatedUnderlineText(
    text: String,
    color: Color,
    fontSize: TextUnit,
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
