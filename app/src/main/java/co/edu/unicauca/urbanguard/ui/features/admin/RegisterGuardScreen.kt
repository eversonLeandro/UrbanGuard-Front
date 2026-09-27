package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun RegisterGuardScreen(
    onNavigateBack: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

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
            Text(
                text = "Información del guardia",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )

            GuardTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                icon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            GuardTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = "Nombres",
                icon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            GuardTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = "Apellidos",
                icon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            GuardTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = "Teléfono",
                icon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            )

            // Pendiente: el registro de vigilantes aún no está implementado;
            // el botón habilita la validación de campos pero no persiste nada.
            GuardFormFooter(
                enabled = email.isNotBlank() &&
                    nombres.isNotBlank() &&
                    apellidos.isNotBlank() &&
                    telefono.isNotBlank(),
                onSubmit = {}
            )
        }
    }
}

/**
 * Campo de texto del formulario de guardia.
 *
 * Los cuatro campos solo cambiaban etiqueta, icono, tipo de teclado e accion de
 * IME; la forma, los colores y el ancho eran identicos, asi que se unifican aqui
 * para que un cambio de estilo se aplique a los cuatro a la vez.
 *
 * La accion de IME se deduce de [KeyboardOptions.imeAction]: con [ImeAction.Done]
 * el boton del teclado cierra el teclado, y en cualquier otro caso se pasa el
 * foco al campo siguiente. Asi el llamador solo declara la intencion y no repite
 * el cableado de foco en cada campo.
 */
@Composable
private fun GuardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
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
                tint = UgEmerald
            )
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { keyboardController?.hide() }
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UgNavy,
            focusedLabelColor = UgNavy,
            cursorColor = UgNavy
        )
    )
}

/** Cierre del formulario: nota de ayuda, separador y boton de envio. */
@Composable
private fun GuardFormFooter(
    enabled: Boolean,
    onSubmit: () -> Unit
) {
    Text(
        text = "Nota: El teléfono se usará como contraseña inicial.",
        fontSize = 12.sp,
        color = UgTextSecondary,
        modifier = Modifier.padding(start = 4.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSubmit,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            text = "Registrar Guardia",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
