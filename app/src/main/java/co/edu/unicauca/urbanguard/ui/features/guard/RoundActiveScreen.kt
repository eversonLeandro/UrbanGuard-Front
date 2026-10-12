package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.nfc.NfcReaderEffect
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.domain.model.donePointIds
import co.edu.unicauca.urbanguard.domain.model.progressOf
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Ronda en curso: escucha NFC permanente, valida cada tag contra los puntos
 * y muestra el avance. El orden solo sugiere el siguiente, no bloquea.
 */
@Composable
fun RoundActiveScreen(
    onFinished: () -> Unit,
    onNavigateBack: () -> Unit,
    onReportIncident: (String?) -> Unit = {},
    viewModel: RoundActiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RoundActiveEvent.NoActiveRound -> onNavigateBack()
                RoundActiveEvent.RoundFinished -> onFinished()
            }
        }
    }
    NfcReaderEffect(
        enabled = uiState.round != null,
        onTagRead = viewModel::onTagScanned
    )
    LaunchedEffect(uiState.lastScanAt) {
        if (uiState.lastScanAt > 0 && uiState.lastScanKnown) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val round = uiState.round
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = round?.routeName ?: "Ronda",
            onNavigateBack = onNavigateBack
        )

        when {
            uiState.isLoading || round == null -> ActiveLoading()
            else -> ActiveContent(
                uiState = uiState,
                onFinishRequest = viewModel::onRequestFinish,
                onReportIncident = onReportIncident
            )
        }

        if (uiState.showFinishConfirm) {
            val confirmRound = uiState.round
            if (confirmRound != null) {
                val confirmDone = confirmRound.donePointIds().size
                val confirmTotal = uiState.points.size
                FinishRoundDialog(
                    done = confirmDone,
                    total = confirmTotal,
                    onConfirm = viewModel::onConfirmFinish,
                    onDismiss = viewModel::onDismissFinish
                )
            }
        }
    }
}

@Composable
private fun ActiveLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun ActiveContent(
    uiState: RoundActiveUiState,
    onFinishRequest: () -> Unit,
    onReportIncident: (String?) -> Unit
) {
    val round = uiState.round ?: return
    val total = uiState.points.size
    val done = round.donePointIds().size
    val nextId = uiState.points.firstOrNull { !round.donePointIds().contains(it.id) }?.id

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "progress") {
            ProgressHeader(done = done, total = total, progress = round.progressOf(total))
        }

        item(key = "scanner") {
            ScanListenCard(
                lastScanName = uiState.lastScanName,
                lastScanKnown = uiState.lastScanKnown,
                lastScanAt = uiState.lastScanAt
            )
        }

        uiState.errorMessage?.let { message ->
            item(key = "error") {
                Text(text = message, fontSize = 13.sp, color = UgErrorText)
            }
        }

        item(key = "points-title") {
            Text(
                text = "Puntos de la ruta",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )
        }

        items(uiState.points, key = { it.id }) { point ->
            val isDone = round.donePointIds().contains(point.id)
            RoundPointRow(
                point = point,
                isDone = isDone,
                isNext = !isDone && point.id == nextId
            )
        }

        item(key = "finish") {
            Button(
                onClick = onFinishRequest,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Terminar ronda", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        item(key = "report") {
            TextButton(onClick = { onReportIncident(nextId) }) {
                Text(
                    "Reportar novedad en este punto",
                    color = UgNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ProgressHeader(done: Int, total: Int, progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UgNavyGradient, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$done de $total",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = UgEmerald,
            trackColor = Color.White.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun ScanListenCard(
    lastScanName: String?,
    lastScanKnown: Boolean,
    lastScanAt: Long
) {
    val success = lastScanName != null && lastScanKnown
    val icon = when {
        lastScanName == null -> Icons.Default.Nfc
        success -> Icons.Default.CheckCircle
        else -> Icons.Default.Cancel
    }
    val tint = when {
        lastScanName == null -> UgNavy
        success -> UgEmerald
        else -> UgErrorText
    }
    val container by animateColorAsState(
        targetValue = when {
            success -> UgEmerald.copy(alpha = 0.14f)
            lastScanName != null -> Color(0xFFF57C00).copy(alpha = 0.12f)
            else -> Color.White
        },
        animationSpec = tween(400),
        label = "scanCard"
    )
    val pulse by rememberInfiniteTransition(label = "scanPulse").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )
    val pop = remember { Animatable(1f) }
    LaunchedEffect(lastScanAt) {
        if (lastScanAt > 0L && success) {
            pop.snapTo(0.5f)
            pop.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScanStatusBadge(
                isWaiting = lastScanName == null,
                icon = icon,
                tint = tint,
                iconScale = pop.value,
                pulseScale = pulse
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val feedback = scanFeedback(lastScanName, lastScanKnown, lastScanAt)
                Text(
                    text = feedback.first,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgTextPrimary
                )
                Text(
                    text = feedback.second,
                    fontSize = 12.5.sp,
                    color = UgTextSecondary
                )
            }
        }
    }
}

@Composable
private fun ScanStatusBadge(
    isWaiting: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    iconScale: Float,
    pulseScale: Float
) {
    Box(contentAlignment = Alignment.Center) {
        if (isWaiting) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    .background(UgNavy.copy(alpha = 0.12f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                .background(tint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/** Título y subtítulo del feedback según la última lectura. */
private fun scanFeedback(
    lastScanName: String?,
    known: Boolean,
    lastScanAt: Long
): Pair<String, String> {
    if (lastScanName == null) {
        return "Escuchando tags…" to "Acerca un tag de la ruta al equipo."
    }
    if (!known) {
        return "Tag no registrado" to
            "Ese tag no pertenece a ninguna ruta del conjunto."
    }
    return lastScanName to "Registrado a las ${scanTimeLabel(lastScanAt)}."
}

private fun scanTimeLabel(millis: Long): String {
    if (millis <= 0L) return ""
    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        .withZone(java.time.ZoneId.systemDefault())
    return formatter.format(java.time.Instant.ofEpochMilli(millis))
}

@Composable
private fun RoundPointRow(point: PatrolControlPoint, isDone: Boolean, isNext: Boolean) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) UgEmerald.copy(alpha = 0.10f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        when {
                            isDone -> UgEmerald
                            isNext -> UgNavy
                            else -> UgTextSecondary.copy(alpha = 0.25f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${point.order + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.name,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UgTextPrimary
                )
                if (point.location.isNotBlank()) {
                    Text(
                        text = point.location,
                        fontSize = 12.sp,
                        color = UgTextSecondary
                    )
                }
            }
            if (isNext && !isDone) {
                Text(
                    text = "SIGUIENTE",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = UgNavy,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(UgNavy.copy(alpha = 0.10f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FinishRoundDialog(
    done: Int,
    total: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val incomplete = total > 0 && done < total
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terminar ronda", fontWeight = FontWeight.Bold, color = UgNavy) },
        text = {
            Text(
                if (incomplete) {
                    "Te faltan ${total - done} de $total puntos. " +
                        "Si terminas ahora, la ronda quedará como incompleta."
                } else {
                    "Se cerrará la ronda con las lecturas registradas."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    if (incomplete) "Terminar de todos modos" else "Terminar",
                    color = if (incomplete) UgErrorText else UgNavy,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Seguir", color = UgTextSecondary) }
        }
    )
}
