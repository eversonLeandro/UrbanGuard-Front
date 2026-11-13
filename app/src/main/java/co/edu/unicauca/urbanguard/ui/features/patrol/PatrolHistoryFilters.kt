package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import co.edu.unicauca.urbanguard.domain.model.PatrolGuardRef
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftStatus
import co.edu.unicauca.urbanguard.ui.components.DateFilterField
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Fecha corta de los campos del rango ("11/01/26"), igual que el listado. */
private val RANGE_FIELD_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yy", Locale.forLanguageTag("es-CO"))

/** Callbacks de interacción para los filtros de historial de patrullaje. */
data class HistoryFilterActions(
    val onFromClick: () -> Unit,
    val onToClick: () -> Unit,
    val onGuardSelected: (PatrolGuardRef?) -> Unit,
    val onStatusSelected: (PatrolShiftStatus?) -> Unit,
    val onOnlyIncompleteChange: (Boolean) -> Unit
)

/**
 * Filtros de la lista de turnos: rango de fechas, vigilante, estado en fila de chips
 * y "solo turnos cerrados con meta incumplida".
 */
@Composable
fun HistoryFilters(
    uiState: PatrolHistoryUiState,
    actions: HistoryFilterActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RangeField(
                label = "Desde",
                date = uiState.from,
                onClick = actions.onFromClick,
                modifier = Modifier.weight(1f)
            )
            RangeField(
                label = "Hasta",
                date = uiState.to,
                onClick = actions.onToClick,
                modifier = Modifier.weight(1f)
            )
        }

        if (uiState.guards.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            GuardFilterField(
                guards = uiState.guards,
                selectedGuard = uiState.selectedGuard,
                onGuardSelected = actions.onGuardSelected
            )
        }

        Spacer(Modifier.height(12.dp))
        StatusFilterChipsRow(
            status = uiState.status,
            onStatusSelected = actions.onStatusSelected
        )

        Spacer(Modifier.height(10.dp))
        IncompleteSwitchRow(
            checked = uiState.onlyIncomplete,
            onCheckedChange = actions.onOnlyIncompleteChange
        )
    }
}

@Composable
private fun StatusFilterChipsRow(
    status: PatrolShiftStatus?,
    onStatusSelected: (PatrolShiftStatus?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Estado del turno",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = UgTextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = status == null,
                onClick = { onStatusSelected(null) },
                label = { Text("Todos", fontSize = 12.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UgNavy,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = status == PatrolShiftStatus.CLOSED,
                onClick = { onStatusSelected(PatrolShiftStatus.CLOSED) },
                label = { Text("Cerrados", fontSize = 12.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UgEmerald,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = status == PatrolShiftStatus.PENDING,
                onClick = { onStatusSelected(PatrolShiftStatus.PENDING) },
                label = { Text("Pendientes", fontSize = 12.5.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UgNavy,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun IncompleteSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = UgNavy
            )
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Solo turnos cerrados con meta incumplida",
            fontSize = 13.sp,
            color = UgTextPrimary
        )
    }
}

@Composable
private fun RangeField(
    label: String,
    date: java.time.LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = UgTextSecondary)
        Spacer(Modifier.height(4.dp))
        DateFilterField(
            date = date,
            onShowDatePicker = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            dateFormat = RANGE_FIELD_FORMAT
        )
    }
}

/**
 * Selector de vigilante del filtro en un contenedor estilizado. Muestra "Todos"
 * para quitar el filtro y la lista ordenada alfabéticamente.
 */
@Composable
private fun GuardFilterField(
    guards: List<PatrolGuardRef>,
    selectedGuard: PatrolGuardRef?,
    onGuardSelected: (PatrolGuardRef?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Vigilante",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = UgTextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(UgInputBg)
                    .border(1.dp, UgBorder, RoundedCornerShape(10.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedGuard?.name ?: "Todos los vigilantes",
                    fontSize = 13.sp,
                    color = if (selectedGuard != null) UgTextPrimary else UgTextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Desplegar lista de vigilantes",
                    tint = UgTextSecondary
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Todos los vigilantes", fontSize = 13.sp) },
                    onClick = {
                        expanded = false
                        onGuardSelected(null)
                    }
                )
                guards.forEach { guard ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = guard.name,
                                fontSize = 13.sp,
                                color = if (guard.id == selectedGuard?.id) UgEmerald
                                else UgTextPrimary
                            )
                        },
                        onClick = {
                            expanded = false
                            onGuardSelected(guard)
                        }
                    )
                }
            }
        }
    }
}

