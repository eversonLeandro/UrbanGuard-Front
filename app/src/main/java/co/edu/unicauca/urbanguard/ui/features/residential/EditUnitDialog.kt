package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.validation.UnitIdentifierValidator
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
internal fun EditUnitDialog(
    currentIdentifier: String,
    isUpdating: Boolean,
    serverError: AppError?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var identifier by rememberSaveable(currentIdentifier) {
        mutableStateOf(currentIdentifier)
    }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val errorMessage = localError ?: serverError?.message
    val submit: () -> Unit = {
        val trimmed = identifier.trim()
        val validationError = UnitIdentifierValidator.validate(trimmed)
        if (validationError != null) {
            localError = validationError
        } else {
            localError = null
            onConfirm(trimmed)
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        title = { Text("Editar apartamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        localError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating,
                    singleLine = true,
                    label = { Text("Número o identificador") },
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (!isUpdating) submit() }
                    )
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = UgErrorBright,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = submit, enabled = !isUpdating) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = UgEmerald,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar", color = UgEmerald, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUpdating) {
                Text("Cancelar", color = UgTextSecondary)
            }
        },
        containerColor = Color.White
    )
}
