package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.edu.unicauca.urbanguard.domain.model.CreatedGuard
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Alta de vigilante (HU-009) — POST /api/complexes/{complexId}/guards.
 *
 * El conjunto destino lo decide el ViewModel contra la sesión (el conjunto
 * activo), así que esta pantalla no necesita recibir ningún argumento de
 * navegación.
 */
@Composable
fun RegisterGuardScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RegisterGuardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Registrar guardia",
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuardFormSection(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onNombresChange = viewModel::onNombresChange,
                onApellidosChange = viewModel::onApellidosChange,
                onTelefonoChange = viewModel::onTelefonoChange
            )

            GuardFormFooter(
                enabled = !uiState.isSubmitting,
                isLoading = uiState.isSubmitting,
                errorMessage = uiState.errorMessage,
                onSubmit = viewModel::onSubmit
            )
        }
    }

    // El diálogo se monta fuera de la columna: es la única señal de que el
    // vigilante quedó creado, y tiene que sobrevivir al scroll del formulario.
    uiState.createdGuard?.let { created ->
        GuardCreatedDialog(
            created = created,
            onDismiss = {
                viewModel.onCreatedGuardHandled()
                onNavigateBack()
            }
        )
    }
}

/**
 * Los cuatro campos del formulario.
 *
 * Se separan de [RegisterGuardScreen] para que esa función separe armazón y
 * acciones, y para que la lista de campos se lea de un vistazo en lugar de
 * quedar enterrada en el medio de la columna.
 */
@Composable
private fun GuardFormSection(
    uiState: RegisterGuardUiState,
    onEmailChange: (String) -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit
) {
    Text(
        text = "Información del guardia",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = UgNavy
    )

    GuardTextField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = "Email",
        icon = Icons.Default.Email,
        errorText = uiState.emailError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
    )

    GuardTextField(
        value = uiState.nombres,
        onValueChange = onNombresChange,
        label = "Nombres",
        icon = Icons.Default.Person,
        errorText = uiState.nombresError,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )

    GuardTextField(
        value = uiState.apellidos,
        onValueChange = onApellidosChange,
        label = "Apellidos",
        icon = Icons.Default.Person,
        errorText = uiState.apellidosError,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )

    GuardTextField(
        value = uiState.telefono,
        onValueChange = onTelefonoChange,
        label = "Teléfono",
        icon = Icons.Default.Phone,
        errorText = uiState.telefonoError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        )
    )
}

/**
 * Campo de texto del formulario de guardia.
 *
 * Los cuatro campos solo cambiaban etiqueta, icono, tipo de teclado, accion de
 * IME y error; la forma, los colores y el ancho eran identicos, asi que se
 * unifican aqui para que un cambio de estilo se aplique a los cuatro a la vez.
 *
 * La accion de IME se deduce de [KeyboardOptions.imeAction]: con [ImeAction.Done]
 * el boton del teclado cierra el teclado, y en cualquier otro caso se pasa el
 * foco al campo siguiente. Asi el llamador solo declara la intencion y no repite
 * el cableado de foco en cada campo.
 *
 * [errorText] se pinta como texto de apoyo del propio campo y no como banner:
 * el backend aplana los errores de validacion en un solo string, asi que quien
 * puede atribuirlos a un campo concreto es la validacion local del ViewModel.
 */
@Composable
private fun GuardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    errorText: String?,
    keyboardOptions: KeyboardOptions
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (errorText != null) UgErrorBright else UgEmerald
            )
        },
        singleLine = true,
        isError = errorText != null,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { keyboardController?.hide() }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UgNavy,
            focusedLabelColor = UgNavy,
            cursorColor = UgNavy,
            errorBorderColor = UgErrorBright,
            errorLabelColor = UgErrorBright
        ),
        supportingText = if (errorText != null) {
            { Text(errorText) }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Cierre del formulario: nota de ayuda, banner de error y boton de envio.
 *
 * El banner recoge lo que no se puede atribuir a un campo — 403, 404, 409, los
 * 5xx y el 400 del backend — y por eso convive con los errores por campo sin
 * duplicarlos.
 */
@Composable
private fun GuardFormFooter(
    enabled: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit
) {
    Text(
        text = "Nota: El teléfono se usará como contraseña inicial.",
        fontSize = 12.sp,
        color = UgTextSecondary,
        modifier = Modifier.padding(start = 4.dp)
    )

    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = UgNavy,
            disabledContainerColor = UgNavy.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Registrando...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Text(
                text = "Registrar Guardia",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Confirmación del alta.
 *
 * Recuerda que el teléfono es la contraseña inicial porque es el único punto
 * donde el administrador ve ese dato con claridad: si se pierde, la cuenta
 * queda creada y sin forma de entrar, y el backend no tiene endpoint de
 * reseteo para vigilantes.
 */
@Composable
private fun GuardCreatedDialog(
    created: CreatedGuard,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(UgSuccessBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = UgSuccessText,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Guardia registrado",
                fontWeight = FontWeight.Bold,
                color = UgNavy,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${created.nombres} ya tiene acceso a UrbanGuard con el rol de vigilante.",
                    fontSize = 14.sp,
                    color = UgTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Comunica el teléfono y el correo con los que se registró: el teléfono " +
                        "es su contraseña inicial.",
                    fontSize = 13.sp,
                    color = UgSuccessText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Listo",
                    color = UgNavy,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White
    )
}
