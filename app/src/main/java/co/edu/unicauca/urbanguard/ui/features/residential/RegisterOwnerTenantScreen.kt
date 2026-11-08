package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.ResidentType
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBright
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
internal fun RegisterOwnerTenantDialog(
    unitId: String,
    unitIdentifier: String,
    onRegistered: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: RegisterOwnerTenantViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(unitId) {
        viewModel.onOpen(unitId)
    }
    LaunchedEffect(state.successMessage) {
        val message = state.successMessage ?: return@LaunchedEffect
        viewModel.onSuccessConsumed()
        onRegistered(message)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { if (!state.isSaving) onDismiss() },
        title = { Text("Asignar residente") },
        text = { OwnerTenantForm(state, unitIdentifier, viewModel) },
        confirmButton = {
            TextButton(onClick = viewModel::onSubmit, enabled = !state.isSaving) {
                if (state.isSaving) {
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
            TextButton(onClick = onDismiss, enabled = !state.isSaving) {
                Text("Cancelar", color = UgTextSecondary)
            }
        },
        containerColor = Color.White
    )
}

@Composable
internal fun ResidentRegistrationDialog(
    unit: ResidentialUnit?,
    visible: Boolean,
    onRegistered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible || unit == null) return
    RegisterOwnerTenantDialog(
        unitId = unit.id,
        unitIdentifier = unit.identifier,
        onRegistered = onRegistered,
        onDismiss = onDismiss
    )
}

@Composable
private fun OwnerTenantForm(
    state: RegisterOwnerTenantUiState,
    unitIdentifier: String,
    viewModel: RegisterOwnerTenantViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Apartamento $unitIdentifier", color = UgTextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResidentType.entries.forEach { type ->
                FilterChip(
                    selected = state.type == type,
                    onClick = { viewModel.onTypeChange(type) },
                    enabled = !state.isSaving,
                    label = { Text(type.label) }
                )
            }
        }
        ResidentField(
            "Nombres", state.nombres, state.fieldErrors[OwnerTenantField.NOMBRES],
            !state.isSaving, viewModel::onNombresChange
        )
        ResidentField(
            "Apellidos", state.apellidos, state.fieldErrors[OwnerTenantField.APELLIDOS],
            !state.isSaving, viewModel::onApellidosChange
        )
        ResidentField(
            "Documento", state.documento, state.fieldErrors[OwnerTenantField.DOCUMENTO],
            !state.isSaving, viewModel::onDocumentoChange, KeyboardType.Text
        )
        ResidentField(
            "Correo electrónico", state.email, state.fieldErrors[OwnerTenantField.EMAIL],
            !state.isSaving, viewModel::onEmailChange, KeyboardType.Email
        )
        ResidentField(
            "Teléfono", state.telefono, state.fieldErrors[OwnerTenantField.TELEFONO],
            !state.isSaving, viewModel::onTelefonoChange, KeyboardType.Phone
        )
        state.serverError?.let { error ->
            Text(text = error.message, color = UgErrorBright, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ResidentField(
    label: String,
    value: String,
    error: String?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            label = { Text(label) },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
        if (error != null) {
            Text(text = error, color = UgErrorBright, fontSize = 12.sp)
        }
    }
}
