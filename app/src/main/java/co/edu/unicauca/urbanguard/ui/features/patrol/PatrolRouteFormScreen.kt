package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PatrolSchedule
import co.edu.unicauca.urbanguard.domain.model.shortLabel
import co.edu.unicauca.urbanguard.domain.model.weekDays
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

private val formTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Formulario de ruta: nombre, puntos que la componen y sus horarios.
 *
 * Cada horario combina hora y días; agregar varios repite la misma ruta a
 * distintas horas. Con [routeId] nulo crea; con valor, edita.
 */
@Composable
fun PatrolRouteFormScreen(
    routeId: String?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PatrolRouteFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        viewModel.load(routeId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event == PatrolRouteFormEvent.Saved) onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = if (routeId.isNullOrBlank()) "Nueva ruta" else "Editar ruta",
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp)
        ) {
            RouteNameField(
                value = uiState.name,
                error = uiState.nameError,
                onChange = viewModel::onNameChange
            )

            Spacer(Modifier.height(12.dp))

            RouteSummaryHeader(
                pointCount = uiState.selectedIds.size,
                scheduleCount = uiState.schedules.size
            )

            Spacer(Modifier.height(20.dp))

            RoutePointsSection(
                options = uiState.availablePointIds,
                selectedIds = uiState.selectedIds,
                onTogglePoint = viewModel::onTogglePoint,
                onMovePoint = viewModel::onMovePoint
            )

            Spacer(Modifier.height(20.dp))

            RouteSchedulesSection(
                schedules = uiState.schedules,
                selectedDays = uiState.selectedDays,
                formError = uiState.formError,
                onEvent = viewModel::onScheduleEvent,
                onPreset = viewModel::onAddPreset
            )

            Spacer(Modifier.height(12.dp))

            RouteFooter(
                isActive = uiState.isActive,
                isSaving = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onActiveChange = viewModel::onActiveChange,
                onSaveClick = viewModel::onSaveClick
            )
        }
    }

    val editingIndex = uiState.editingTimeIndex
    uiState.routeChangeBlockedMessage?.let { message ->
        PatrolRouteChangeBlockedDialog(
            message = message,
            onDismiss = viewModel::onDismissRouteChangeBlocked
        )
    }

    if (editingIndex != null) {
        val current = uiState.schedules.getOrNull(editingIndex)
        if (current != null) {
            RouteTimeDialog(
                hour = current.time.hour,
                minute = current.time.minute,
                onConfirm = { hour, minute ->
                    viewModel.onScheduleEvent(RouteScheduleEvent.PickTime(hour, minute))
                },
                onDismiss = {
                    viewModel.onScheduleEvent(RouteScheduleEvent.ClosePicker)
                }
            )
        }
    }
}

@Composable
private fun RouteNameField(
    value: String,
    error: String?,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Nombre de la ruta") },
        placeholder = { Text("Ronda nocturna") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = UgNavy,
            unfocusedBorderColor = UgBorder
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RoutePointsSection(
    options: List<PatrolPointOption>,
    selectedIds: List<String>,
    onTogglePoint: (String, Boolean) -> Unit,
    onMovePoint: (String, Int) -> Unit
) {
    RouteSectionTitle("Puntos de la ruta")

    Spacer(Modifier.height(8.dp))

    if (options.isEmpty()) {
        Text(
            text = "Primero registra puntos de control para armar la ruta.",
            fontSize = 13.sp,
            color = UgTextSecondary
        )
    }
    options.forEach { option ->
        val position = selectedIds.indexOf(option.id).takeIf { it >= 0 }
        PointCheckRow(
            name = option.name,
            position = position,
            checked = position != null,
            onCheckedChange = { onTogglePoint(option.id, it) }
        )
    }

    Spacer(Modifier.height(10.dp))

    SelectedRouteStrip(
        points = selectedIds.mapNotNull { id -> options.firstOrNull { it.id == id } },
        onMove = onMovePoint
    )
}

@Composable
private fun RouteSchedulesSection(
    schedules: List<PatrolSchedule>,
    selectedDays: Set<DayOfWeek>,
    formError: String?,
    onEvent: (RouteScheduleEvent) -> Unit,
    onPreset: (Int, Int) -> Unit
) {
    RouteSectionTitle("Horarios")

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Los días seleccionados aplican a todos los horarios de esta ronda. " +
            "La ronda se habilita 10 minutos antes.",
        fontSize = 12.sp,
        color = UgTextSecondary
    )

    Spacer(Modifier.height(8.dp))

    SchedulePresetRow(onPreset = onPreset)

    Spacer(Modifier.height(4.dp))

    Text(
        text = "Días de la ronda",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = UgTextSecondary
    )
    Spacer(Modifier.height(6.dp))
    DayChipsRow(
        selected = selectedDays,
        onToggleDay = { day, selected ->
            onEvent(RouteScheduleEvent.ToggleDay(day, selected))
        }
    )
    Spacer(Modifier.height(10.dp))

    schedules.forEachIndexed { index, schedule ->
        ScheduleEditorRow(
            schedule = schedule,
            onTimeClick = { onEvent(RouteScheduleEvent.OpenPicker(index)) },
            onRemove = { onEvent(RouteScheduleEvent.Remove(index)) }
        )
        Spacer(Modifier.height(10.dp))
    }

    TextButton(onClick = { onEvent(RouteScheduleEvent.Add) }) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = UgEmerald
        )
        Spacer(Modifier.width(4.dp))
        Text("Agregar horario", color = UgEmerald, fontWeight = FontWeight.Bold)
    }

    formError?.let { error ->
        Spacer(Modifier.height(8.dp))
        Text(text = error, fontSize = 13.sp, color = UgErrorText)
    }
}

@Composable
private fun RouteFooter(
    isActive: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onActiveChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Ruta activa",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = UgNavy,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isActive,
            onCheckedChange = onActiveChange,
            colors = SwitchDefaults.colors(checkedTrackColor = UgEmerald)
        )
    }

    errorMessage?.let { message ->
        Spacer(Modifier.height(12.dp))
        Text(text = message, fontSize = 13.sp, color = UgErrorText)
    }

    Spacer(Modifier.height(20.dp))

    Button(
        onClick = onSaveClick,
        enabled = !isSaving,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text("Guardar ruta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun RouteSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = UgNavy
    )
}

@Composable
private fun PointCheckRow(
    name: String,
    position: Int?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = UgEmerald)
        )
        Text(
            text = position?.let { "${it + 1}. $name" } ?: name,
            fontSize = 14.sp,
            color = UgNavy,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScheduleEditorRow(
    schedule: PatrolSchedule,
    onTimeClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onTimeClick) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = schedule.time.format(formTimeFormat),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgNavy
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar horario",
                    tint = UgErrorText
                )
            }
        }
    }
}

@Composable
private fun DayChipsRow(
    selected: Set<DayOfWeek>,
    onToggleDay: (DayOfWeek, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weekDays().chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { day ->
                    val isSelected = selected.contains(day)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleDay(day, !isSelected) },
                        label = {
                            Text(
                                day.shortLabel(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UgNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteTimeDialog(
    hour: Int,
    minute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hora de la ronda", fontWeight = FontWeight.Bold, color = UgNavy) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("Aceptar", color = UgNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = UgTextSecondary) }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = UgEmerald
            )
        }
    )
}
