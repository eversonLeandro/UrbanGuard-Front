package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.ResidentialResident
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
internal fun CensusResidentRow(
    resident: ResidentialResident,
    canManage: Boolean = false,
    isChanging: Boolean = false,
    onChangeStatus: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = UgSuccessText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(resident.fullName, color = UgNavy)
            resident.type?.let { type ->
                Text(text = type.label, color = UgTextSecondary, fontSize = 12.sp)
            }
            if (!resident.active) {
                Text(text = "Inactivo", color = UgTextSecondary, fontSize = 12.sp)
            }
            if (canManage) {
                TextButton(onClick = onChangeStatus, enabled = !isChanging) {
                    Text(
                        text = when {
                            isChanging -> "Guardando…"
                            resident.active -> "Inactivar"
                            else -> "Reactivar"
                        }
                    )
                }
            }
        }
    }
}
