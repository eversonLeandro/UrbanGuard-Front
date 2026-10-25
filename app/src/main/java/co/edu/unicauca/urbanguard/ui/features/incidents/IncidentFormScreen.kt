package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.camera.rememberPhotoCapture
import co.edu.unicauca.urbanguard.core.common.IncidentConstants
import co.edu.unicauca.urbanguard.domain.model.IncidentCategory
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.label
import co.edu.unicauca.urbanguard.ui.components.LocalPhotoImage
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy

/**
 * Formulario de novedad: foto protagonista, categoría, descripción y punto
 * opcional (pre-llenado si viene de una ronda).
 */
@Composable
fun IncidentFormScreen(
    pointId: String?,
    executionId: String?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: IncidentFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val takePhoto = rememberPhotoCapture(
        onCaptured = { viewModel.onPhotoCaptured(it) },
        onError = { viewModel.onCameraError(true) }
    )

    LaunchedEffect(pointId, executionId) {
        viewModel.load(pointId, executionId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event == IncidentFormEvent.Saved) onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Reportar novedad",
            onNavigateBack = onNavigateBack
        )

        IncidentFormBody(
            state = uiState,
            takePhoto = takePhoto,
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IncidentFormBody(
    state: IncidentFormUiState,
    takePhoto: () -> Unit,
    viewModel: IncidentFormViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp)
    ) {
            PhotoHero(
                photoPath = state.photoPath,
                onTakePhoto = takePhoto
            )

            if (state.cameraError) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = IncidentConstants.MSG_CAMERA_ERROR,
                    fontSize = 13.sp,
                    color = UgErrorText
                )
            }

            Spacer(Modifier.height(16.dp))

            CategoryChips(
                selected = state.category,
                error = state.categoryError,
                onSelect = viewModel::onCategoryChange
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("¿Qué está pasando?") },
                placeholder = { Text("Puerta del sótano abierta…") },
                isError = state.descriptionError != null,
                supportingText = state.descriptionError?.let { { Text(it) } },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = UgNavy,
                    unfocusedBorderColor = UgBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (state.availablePoints.isNotEmpty()) {
                PointDropdown(
                    points = state.availablePoints,
                    selectedId = state.pointId,
                    onSelect = viewModel::onPointChange
                )
                Spacer(Modifier.height(12.dp))
            }

            state.errorMessage?.let { message ->
                Text(text = message, fontSize = 13.sp, color = UgErrorText)
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = viewModel::onSaveClick,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Guardar novedad", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
}

@Composable
private fun PhotoHero(photoPath: String?, onTakePhoto: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(UgNavy.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath != null) {
            LocalPhotoImage(
                path = photoPath,
                size = 220.dp,
                contentDescription = "Foto de la novedad",
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = onTakePhoto,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (photoPath == null) {
                    UgEmerald
                } else {
                    Color.White.copy(alpha = 0.92f)
                }
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = if (photoPath == null) Color.White else UgNavy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (photoPath == null) "Tomar foto" else "Retomar foto",
                fontWeight = FontWeight.Bold,
                color = if (photoPath == null) Color.White else UgNavy
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    selected: IncidentCategory?,
    error: String?,
    onSelect: (IncidentCategory) -> Unit
) {
    Column {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IncidentCategory.entries.forEach { category ->
                CategoryChip(
                    category = category,
                    selected = selected == category,
                    onSelect = onSelect
                )
            }
        }
        error?.let {
            Spacer(Modifier.height(4.dp))
            Text(text = it, fontSize = 12.5.sp, color = UgErrorText)
        }
    }
}

@Composable
private fun CategoryChip(
    category: IncidentCategory,
    selected: Boolean,
    onSelect: (IncidentCategory) -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = { onSelect(category) },
        label = {
            Text(
                category.label(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        },
        leadingIcon = {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = categoryColor(category),
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointDropdown(
    points: List<PatrolControlPoint>,
    selectedId: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = points.firstOrNull { it.id == selectedId }?.name
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Punto (opcional)") },
            placeholder = { Text("¿Dónde pasó?") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = UgNavy,
                unfocusedBorderColor = UgBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sin punto") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            points.forEach { point ->
                DropdownMenuItem(
                    text = { Text(point.name) },
                    onClick = {
                        onSelect(point.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
