package co.edu.unicauca.urbanguard.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun RegisterScreen(
    onRegisterSuccess: (role: UserRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    // El tipo es explicito porque moveFocus devuelve Boolean y, sin esta
    // anotacion, el valor inferido seria () -> Boolean, que no puede pasarse
    // como onNext: () -> Unit a los campos de texto.
    val moveToNextField: () -> Unit = {
        focusManager.moveFocus(FocusDirection.Down)
    }

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            Toast.makeText(
                context,
                "¡Cuenta creada con éxito! Bienvenido a UrbanGuard",
                Toast.LENGTH_LONG
            ).show()
            val role = uiState.userRole ?: UserRole.RESIDENT
            onRegisterSuccess(role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RegisterHeader()

        Spacer(Modifier.height(20.dp))

        RegisterIdentityFields(
            uiState = uiState,
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onNextField = moveToNextField
        )

        Spacer(Modifier.height(12.dp))

        RegisterPasswordFields(
            uiState = uiState,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            onToggleConfirmPasswordVisibility = viewModel::toggleConfirmPasswordVisibility,
            onNextField = moveToNextField,
            onRegisterFromKeyboard = {
                keyboardController?.hide()
                viewModel.onRegisterClick()
            }
        )

        RegisterErrorBanner(uiState.errorMessage)

        Spacer(Modifier.height(24.dp))

        RegisterSubmitButton(
            isLoading = uiState.isLoading,
            onClick = viewModel::onRegisterClick
        )

        Spacer(Modifier.height(20.dp))

        RegisterFooter(onNavigateToLogin = onNavigateToLogin)

        Spacer(Modifier.height(32.dp))
    }
}

/**
 * Logo, nombre de la aplicacion y encabezado del formulario.
 *
 * Es una extension de [ColumnScope] a proposito: los dos titulos alinean a la
 * izquierda con `Modifier.align`, y ese modificador solo existe dentro del
 * ambito de la Column que los contiene.
 */
@Composable
private fun ColumnScope.RegisterHeader() {
    Spacer(Modifier.height(48.dp))

    Image(
        painter = painterResource(R.drawable.logo_u),
        contentDescription = "Logo UrbanGuard",
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(20.dp))
    )
    Spacer(Modifier.height(8.dp))
    Text("UrbanGuard", color = UgNavy, fontWeight = FontWeight.Medium, fontSize = 20.sp)

    Spacer(Modifier.height(24.dp))

    Text(
        "Crear cuenta",
        color = UgNavy,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        modifier = Modifier.align(Alignment.Start)
    )
    Text(
        "Regístrate para comenzar a gestionar tu comunidad",
        color = UgTextSecondary,
        fontSize = 13.sp,
        modifier = Modifier.align(Alignment.Start)
    )
}

/**
 * Nombre y correo, con el separador de 12.dp que los separa.
 *
 * [RegisterUiState] se pasa entero porque sus campos se usan como
 * `supportingText` con smart cast; reconstruir el estado aqui duplicaria la
 * logica de error del ViewModel.
 */
@Composable
private fun RegisterIdentityFields(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNextField: () -> Unit
) {
    RegisterTextField(
        value = uiState.name,
        onValueChange = onNameChange,
        label = "Nombre completo",
        leadingIcon = Icons.Default.Person,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNextField() }),
        errorText = uiState.nameError
    )

    Spacer(Modifier.height(12.dp))

    RegisterTextField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = "Correo electrónico",
        leadingIcon = Icons.Default.Email,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNextField() }),
        errorText = uiState.emailError
    )
}

/**
 * Campo de texto con icono, error opcional y soporte de teclado. Nombre y correo
 * solo difieren en etiqueta, icono y `KeyboardOptions`, asi que comparten esta
 * implementacion en lugar de duplicarla.
 */
@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    errorText: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = UgTextSecondary
            )
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Contrasena y su confirmacion, con el separador de 12.dp entre ambos.
 *
 * Los dos campos comparten icono, transformacion y textos del toggle, asi que
 * delega en [PasswordField]; lo que cambia es el valor, el estado de
 * visibilidad, el texto de ayuda y la accion de teclado.
 */
@Composable
private fun RegisterPasswordFields(
    uiState: RegisterUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onNextField: () -> Unit,
    onRegisterFromKeyboard: () -> Unit
) {
    PasswordField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = "Contraseña",
        errorText = uiState.passwordError,
        isVisible = uiState.isPasswordVisible,
        onToggleVisibility = onTogglePasswordVisibility,
        // El texto de ayuda es una pista, no un error: se muestra siempre y el
        // error real lo reemplaza cuando existe.
        supportingText = {
            Text(uiState.passwordError ?: "Mínimo 8 caracteres, con una mayúscula y un número.")
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onNextField() })
    )

    Spacer(Modifier.height(12.dp))

    PasswordField(
        value = uiState.confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = "Confirmar contraseña",
        errorText = uiState.confirmPasswordError,
        isVisible = uiState.isConfirmPasswordVisible,
        onToggleVisibility = onToggleConfirmPasswordVisibility,
        supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onRegisterFromKeyboard() })
    )
}

/**
 * Campo de contrasena con toggle de visibilidad.
 *
 * `isError` se deriva de [errorText] y no de [supportingText] porque el campo de
 * contrasena siempre trae texto de ayuda (una pista) y solo se marca invalido
 * cuando hay error. Los parametros con valor por defecto no cuentan para la
 * regla de lista larga.
 */
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String?,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = UgTextSecondary
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
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
                    tint = UgTextSecondary
                )
            }
        },
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = errorText != null,
        supportingText = supportingText,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Aviso de error del envio. El separador vive aqui para que no quede un hueco de
 * 12.dp cuando no hay mensaje.
 */
@Composable
private fun RegisterErrorBanner(message: String?) {
    if (message == null) return
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgErrorBg, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = UgErrorText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(message, color = UgErrorText, fontSize = 12.5.sp)
    }
}

/** Boton de envio: muestra progreso y se deshabilita mientras la peticion corre. */
@Composable
private fun RegisterSubmitButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Registrando...")
        } else {
            Text("Registrarse", fontWeight = FontWeight.Medium)
        }
    }
}

/** Enlace inferior a la pantalla de inicio de sesion. */
@Composable
private fun RegisterFooter(onNavigateToLogin: () -> Unit) {
    Row {
        Text("¿Ya tienes una cuenta? ", color = UgTextSecondary, fontSize = 12.5.sp)
        Text(
            "Iniciar sesión",
            color = UgEmerald,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            modifier = Modifier.clickable { onNavigateToLogin() }
        )
    }
}
