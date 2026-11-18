package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
internal fun CensusUnitDetailSection(
    state: CensusUiState,
    onEditUnit: () -> Unit,
    onAssignResident: () -> Unit,
    onChangeResidentStatus: (String, ResidentialResident) -> Unit,
    onDismissUnitDetail: () -> Unit
) {
    var residentToChange by remember { mutableStateOf<ResidentialResident?>(null) }
    val selectedUnit = state.selectedUnit ?: return
    if (state.isLoading || state.error != null) return

    UnitDetailSheet(
        unit = selectedUnit,
        isAdmin = state.isAdmin,
        changingResidentId = state.changingResidentId.takeIf { state.isChangingResidentStatus },
        onEditUnit = onEditUnit,
        onAssignResident = onAssignResident,
        onChangeResidentStatus = { resident -> residentToChange = resident },
        onDismiss = onDismissUnitDetail
    )

    residentToChange?.let { resident ->
        val active = resident.active
        AlertDialog(
            onDismissRequest = { residentToChange = null },
            title = { Text(if (active) "Inactivar registro" else "Reactivar registro") },
            text = {
                Text(
                    if (active) {
                        "¿Deseas inactivar a ${resident.fullName}? El registro y su " +
                            "historial se conservarán."
                    } else {
                        "¿Deseas reactivar el registro de ${resident.fullName}?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onChangeResidentStatus(selectedUnit.id, resident)
                        residentToChange = null
                    }
                ) { Text(if (active) "Inactivar" else "Reactivar") }
            },
            dismissButton = {
                TextButton(onClick = { residentToChange = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UnitDetailSheet(
    unit: ResidentialUnit,
    isAdmin: Boolean,
    changingResidentId: String?,
    onEditUnit: () -> Unit,
    onAssignResident: () -> Unit,
    onChangeResidentStatus: (ResidentialResident) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Apartamento ${unit.identifier}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )
            if (unit.residents.isEmpty()) {
                Text(
                    text = "Sin residentes asociados",
                    color = CensusVacantText,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = if (unit.residents.size == 1) "Residente asociado" else "Residentes asociados",
                    color = UgTextSecondary,
                    style = MaterialTheme.typography.titleMedium
                )
                unit.residents.forEach { resident ->
                    val residentId = resident.ownerTenantId ?: resident.membershipId
                    CensusResidentRow(
                        resident = resident,
                        canManage = isAdmin && residentId != null,
                        isChanging = changingResidentId == residentId,
                        onChangeStatus = { onChangeResidentStatus(resident) }
                    )
                }
            }
            if (isAdmin && unit.residents.isEmpty()) {
                Button(
                    onClick = onAssignResident,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgEmerald)
                ) {
                    Text(
                        text = "Asignar residente",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (isAdmin) {
                Button(
                    onClick = onEditUnit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UgNavy)
                ) {
                    Text(
                        text = "Editar apartamento",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

internal val CensusVacantText = Color(0xFF92400E)
