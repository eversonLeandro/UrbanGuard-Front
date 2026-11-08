package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun PackageDeliveryDialog(
    parcel: Package?,
    onDismiss: () -> Unit,
    onDeliver: (Package, String) -> Unit
) {
    parcel ?: return
    var deliveredTo by remember(parcel.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar entrega") },
        text = {
            OutlinedTextField(
                value = deliveredTo,
                onValueChange = { deliveredTo = it },
                label = { Text("Nombre de quien recibe") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (deliveredTo.isNotBlank()) {
                    onDeliver(parcel, deliveredTo.trim())
                }
            }) { Text("Entregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = UgTextSecondary)
            }
        }
    )
}
