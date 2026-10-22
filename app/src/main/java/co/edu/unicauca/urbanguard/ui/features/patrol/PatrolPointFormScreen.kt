package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.core.nfc.NfcReaderEffect
import co.edu.unicauca.urbanguard.core.nfc.isNfcAvailable
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private val wizardSteps = listOf("Datos", "Tag NFC", "Revisar")

/**
 * Formulario de punto de control como asistente de 3 pasos.
 *
 * Con [pointId] nulo crea; con valor, edita el punto existente. La barra
 * inferior fija evita el scroll hasta el botón y muestra Atrás / Continuar /
 * Guardar según el paso.
 */
@Composable
fun PatrolPointFormScreen(
    pointId: String?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PatrolPointFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val nfcSupported = remember { isNfcAvailable(context) }

    LaunchedEffect(pointId) {
        viewModel.load(pointId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event == PatrolPointFormEvent.Saved) onSaved()
        }
    }
    NfcReaderEffect(
        enabled = uiState.nfcState == NfcReadState.WAITING,
        onTagRead = viewModel::onTagIdChange
    )

    Scaffold(
        topBar = {
            UrbanGuardTopBar(
                title = if (pointId.isNullOrBlank()) "Nuevo punto" else "Editar punto",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            WizardBottomBar(
                step = uiState.step,
                isSaving = uiState.isSaving,
                onBack = viewModel::onBack,
                onNext = viewModel::onNext,
                onSave = viewModel::onSaveClick
            )
        },
        containerColor = UgBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp)
        ) {
            StepIndicator(step = uiState.step)

            Spacer(Modifier.height(18.dp))

            when (uiState.step) {
                0 -> DatosStep(
                    name = uiState.name,
                    nameError = uiState.nameError,
                    location = uiState.location,
                    description = uiState.description,
                    onNameChange = { viewModel.onFieldChange(name = it) },
                    onLocationChange = { viewModel.onFieldChange(location = it) },
                    onDescriptionChange = { viewModel.onFieldChange(description = it) }
                )
                1 -> NfcStep(
                    nfcState = uiState.nfcState,
                    tagId = uiState.nfcTagId,
                    tagError = uiState.tagError,
                    nfcSupported = nfcSupported,
                    onStartRead = { viewModel.onStartNfcRead(nfcSupported) },
                    onTagIdChange = viewModel::onTagIdChange
                )
                else -> ReviewStep(
                    name = uiState.name,
                    location = uiState.location,
                    description = uiState.description,
                    tagId = uiState.nfcTagId,
                    isActive = uiState.isActive,
                    onActiveChange = viewModel::onActiveChange
                )
            }

            uiState.errorMessage?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(text = message, fontSize = 13.sp, color = UgErrorText)
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        wizardSteps.forEachIndexed { index, label ->
            val done = index < step
            val current = index == step
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            when {
                                done || current -> UgEmerald
                                else -> UgBorder
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                    color = if (current || done) UgNavy else UgTextSecondary
                )
            }
            if (index < wizardSteps.lastIndex) {
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.dp)
                        .background(if (done) UgEmerald else UgBorder)
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun DatosStep(
    name: String,
    nameError: String?,
    location: String,
    description: String,
    onNameChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nombre del punto") },
        placeholder = { Text("Portería norte") },
        isError = nameError != null,
        supportingText = nameError?.let { { Text(it) } },
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

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = location,
        onValueChange = onLocationChange,
        label = { Text("Ubicación (opcional)") },
        placeholder = { Text("Torre 2, piso 1") },
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

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Descripción (opcional)") },
        placeholder = { Text("Entrada de vehículos") },
        minLines = 2,
        maxLines = 3,
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
private fun NfcStep(
    nfcState: NfcReadState,
    tagId: String,
    tagError: String?,
    nfcSupported: Boolean,
    onStartRead: () -> Unit,
    onTagIdChange: (String) -> Unit
) {
    NfcReaderCard(
        state = nfcState,
        tagId = tagId,
        nfcSupported = nfcSupported,
        tagError = tagError,
        onStartRead = onStartRead
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = tagId,
        onValueChange = onTagIdChange,
        label = { Text("ID del tag") },
        placeholder = { Text("04A3F2…") },
        isError = tagError != null,
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
private fun ReviewStep(
    name: String,
    location: String,
    description: String,
    tagId: String,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "Así se verá en la ruta",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = name.ifBlank { "—" },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy
        )
        if (location.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(text = location, fontSize = 13.sp, color = UgTextSecondary)
        }
        if (description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(text = description, fontSize = 12.5.sp, color = UgTextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Tag $tagId",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = UgTextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(UgBackground)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Punto activo en la ruta",
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
}

@Composable
private fun WizardBottomBar(
    step: Int,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (step > 0) {
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Atrás", color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        Button(
            onClick = { if (step < 2) onNext() else onSave() },
            enabled = !isSaving,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = if (step < 2) "Continuar" else "Guardar punto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun NfcReaderCard(
    state: NfcReadState,
    tagId: String,
    nfcSupported: Boolean,
    tagError: String?,
    onStartRead: () -> Unit
) {
    val pulse by rememberInfiniteTransition(label = "nfc").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    val waiting = state == NfcReadState.WAITING

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgNavyGradient, RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (waiting) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer(scaleX = pulse, scaleY = pulse)
                        .background(Color.White.copy(alpha = 0.16f), CircleShape)
                )
            }
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = PatrolConstants.CD_NFC_ICON,
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Tag NFC del punto",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(4.dp))

        NfcReaderStatus(
            state = state,
            tagId = tagId,
            nfcSupported = nfcSupported,
            waiting = waiting,
            onStartRead = onStartRead
        )

        tagError?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(text = error, fontSize = 12.5.sp, color = Color(0xFFFFCDD2))
        }
    }
}

@Composable
private fun NfcReaderStatus(
    state: NfcReadState,
    tagId: String,
    nfcSupported: Boolean,
    waiting: Boolean,
    onStartRead: () -> Unit
) {
    when {
        !nfcSupported && state != NfcReadState.READ -> {
            Text(
                text = PatrolConstants.NFC_NO_HARDWARE +
                    " Escribe el ID del tag abajo.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        waiting -> {
            Text(
                text = PatrolConstants.NFC_WAITING,
                fontSize = 13.5.sp,
                color = Color.White
            )
        }
        state == NfcReadState.READ && tagId.isNotBlank() -> {
            Text(
                text = PatrolConstants.NFC_READ_TITLE,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = tagId,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(10.dp))
            NfcGhostButton(text = "Volver a leer", onClick = onStartRead)
        }
        else -> {
            Text(
                text = "Acerca un tag al equipo para vincularlo.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(12.dp))
            NfcGhostButton(text = "Leer tag NFC", onClick = onStartRead)
        }
    }
}

@Composable
private fun NfcGhostButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White
        ),
        modifier = Modifier.height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Nfc,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
