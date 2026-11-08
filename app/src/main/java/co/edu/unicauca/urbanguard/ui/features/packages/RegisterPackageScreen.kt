package co.edu.unicauca.urbanguard.ui.features.packages

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.core.camera.rememberPhotoCapture
import co.edu.unicauca.urbanguard.ui.components.LocalPhotoImage

@Composable
fun RegisterPackageScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: RegisterPackageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val takePhoto = rememberPhotoCapture(
        onCaptured = { viewModel.updatePhoto(it.absolutePath) },
        onError = {}
    )
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Column(Modifier.fillMaxSize().background(UgBackground).imePadding()) {
        UrbanGuardTopBar(title = "Registrar ingreso", onNavigateBack = onNavigateBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Datos del paquete", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UgTextPrimary)
            Text(
                "Registra la recepción para llevar el control de portería.",
                fontSize = 13.sp,
                color = UgTextSecondary
            )
            androidx.compose.material3.Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PackageField(
                        "Nombre del destinatario *",
                        state.recipientName,
                        viewModel::updateRecipient,
                        maxLength = 150
                    )
                    PackageField(
                        "Unidad / apartamento",
                        state.residentialUnitLabel,
                        viewModel::updateUnit,
                        maxLength = 50
                    )
                    PackageField(
                        "Remitente o mensajería",
                        state.sender,
                        viewModel::updateSender,
                        maxLength = 150
                    )
                    PackageField(
                        "Descripción del paquete *",
                        state.description,
                        viewModel::updateDescription,
                        minLines = 3,
                        maxLength = 500
                    )
                    PackageField("Observaciones", state.observations, viewModel::updateObservations, minLines = 2)
                    PackagePhoto(photoPath = state.photoPath, onTakePhoto = takePhoto)
                }
            }
            (state.validationError ?: state.errorMessage)?.let {
                Text(it, color = UgErrorText)
            }
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator()
                else Text("Registrar paquete")
            }
        }
    }
}

@Composable
private fun PackageField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
    maxLength: Int = Int.MAX_VALUE
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = UgTextPrimary)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            singleLine = minLines == 1,
            placeholder = { Text(label, fontSize = 13.sp, color = UgTextSecondary) },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = UgInputBg,
                unfocusedContainerColor = UgInputBg,
                focusedBorderColor = UgNavy,
                unfocusedBorderColor = UgBorder
            ),
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            maxLines = if (minLines == 1) 1 else 4
        )
    }
}

@Composable
private fun PackagePhoto(photoPath: String?, onTakePhoto: () -> Unit) {
    Column {
        Text("Foto del paquete (opcional)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = UgTextPrimary)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(UgInputBg),
            contentAlignment = Alignment.Center
        ) {
            if (photoPath != null) {
                LocalPhotoImage(
                    path = photoPath,
                    size = 180.dp,
                    contentDescription = "Foto del paquete",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = onTakePhoto,
                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (photoPath == null) UgNavy else Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, null, tint = if (photoPath == null) Color.White else UgNavy)
                Spacer(Modifier.size(8.dp))
                Text(if (photoPath == null) "Agregar foto" else "Cambiar foto",
                    color = if (photoPath == null) Color.White else UgNavy)
            }
        }
    }
}
