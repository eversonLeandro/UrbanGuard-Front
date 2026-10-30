package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.ui.components.ErrorBanner
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun CreatePqrsScreen(
    onNavigateBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreatePqrsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.createdIds.collect(onCreated) }
    Column(Modifier.fillMaxSize().background(UgBackground).imePadding()) {
        UrbanGuardTopBar(title = "Radicar PQRS", onNavigateBack = onNavigateBack)
        CreatePqrsForm(state, viewModel, Modifier.weight(1f))
        CreatePqrsActions(state, viewModel, onNavigateBack)
    }
}

@Composable
private fun CreatePqrsForm(state: CreatePqrsUiState, viewModel: CreatePqrsViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cuéntanos qué ocurrió", color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            "Completa estos datos para enviar tu solicitud a la administración.",
            color = UgTextSecondary, fontSize = 14.sp
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = UgSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = UgEmerald)
                    Spacer(Modifier.width(10.dp))
                    Text("Datos de la solicitud", color = UgNavy, fontWeight = FontWeight.SemiBold)
                }
                TypeSelector(
                    state.type, state.typeError, state.isSubmitting || state.completedElsewhere,
                    viewModel::onTypeChanged
                )
                OutlinedTextField(
                    value = state.subject,
                    onValueChange = viewModel::onSubjectChanged,
                    label = { Text("Asunto") },
                    placeholder = { Text("Ej. Iluminación del acceso") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    isError = state.subjectError != null,
                    supportingText = {
                        FieldSupport(state.subjectError, state.subject.trim().length, CreatePqrsViewModel.SUBJECT_MAX)
                    },
                    enabled = !state.isSubmitting && !state.completedElsewhere,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp), colors = pqrsFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                DescriptionField(state, viewModel::onDescriptionChanged)
            }
        }
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(horizontal = 2.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = UgTextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tu solicitud se radicará en tu conjunto activo.", color = UgTextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DescriptionField(state: CreatePqrsUiState, onDescriptionChanged: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.Notes, contentDescription = null,
                tint = UgEmerald, modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Descripción", color = UgNavy, fontWeight = FontWeight.Medium)
        }
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text("Describe lo sucedido y dónde ocurrió") },
            isError = state.descriptionError != null,
            supportingText = {
                FieldSupport(
                    state.descriptionError, state.description.trim().length,
                    CreatePqrsViewModel.DESCRIPTION_MAX
                )
            },
            enabled = !state.isSubmitting && !state.completedElsewhere,
            minLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(12.dp), colors = pqrsFieldColors(),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Descripción" }
        )
    }
}

@Composable
private fun FieldSupport(error: String?, count: Int, max: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (error != null) Text(error, modifier = Modifier.weight(1f, fill = false))
        else Spacer(Modifier.weight(1f))
        Text("$count/$max", color = UgTextSecondary)
    }
}

@Composable
private fun CreatePqrsActions(
    state: CreatePqrsUiState,
    viewModel: CreatePqrsViewModel,
    onNavigateBack: () -> Unit
) {
    Surface(color = UgSurface, shadowElevation = 8.dp) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val validationError = state.typeError != null ||
                state.subjectError != null || state.descriptionError != null
            if (state.submitError != null) {
                ErrorBanner(message = requireNotNull(state.submitError))
            } else if (validationError) {
                ErrorBanner(message = "Revisa los campos marcados en el formulario.")
            }
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSubmitting && !state.requiresNewAttempt && !state.completedElsewhere,
                colors = ButtonDefaults.buttonColors(containerColor = UgEmerald, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .semantics { contentDescription = "Enviar PQRS" }
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (state.isSubmitting) "Enviando..." else "Radicar PQRS",
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                )
            }
            if (state.requiresNewAttempt) {
                androidx.compose.material3.OutlinedButton(
                    onClick = viewModel::startNewAttempt, modifier = Modifier.fillMaxWidth()
                ) { Text("Iniciar nuevo intento") }
            }
            if (state.completedElsewhere) {
                TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver a Mis PQRS")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    value: PqrsType?,
    error: String?,
    submitting: Boolean,
    onSelect: (PqrsType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (!submitting) expanded = it }) {
            OutlinedTextField(
                value = value?.spanishLabel.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de PQRS") },
                placeholder = { Text("Seleccionar tipo") },
                leadingIcon = {
                    Icon(value?.let(::pqrsTypeIcon) ?: Icons.Default.Description, contentDescription = null)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                isError = error != null,
                enabled = !submitting,
                shape = RoundedCornerShape(12.dp), colors = pqrsFieldColors(),
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .semantics {
                        contentDescription = "Tipo de PQRS: ${value?.spanishLabel ?: "Seleccionar tipo"}"
                    }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                PqrsType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.spanishLabel) },
                        leadingIcon = { Icon(pqrsTypeIcon(type), contentDescription = null) },
                        onClick = { onSelect(type); expanded = false }
                    )
                }
            }
        }
        if (error != null) Text(
            error,
            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
private fun pqrsFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = UgSurface,
    unfocusedContainerColor = UgSurface,
    focusedBorderColor = UgNavy,
    unfocusedBorderColor = UgBorder
)

