package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgInfoIcon
import co.edu.unicauca.urbanguard.ui.theme.UgNavy

@Composable
internal fun GuardHomeContent(
    userName: String,
    photoUrl: String?,
    onImageError: (String) -> Unit,
    onScanQr: () -> Unit,
    onReportIncident: () -> Unit,
    onPackages: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GuardWelcomeCard(userName = userName, photoUrl = photoUrl, onImageError = onImageError)
        GuardShiftSummary()
        GuardQuickActionsSection(onScanQr, onReportIncident, onPackages)
        GuardRecentActivitySection()
    }
}

@Composable
private fun GuardShiftSummary() {
    Text("Resumen del Turno", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UgNavy)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GuardMetricCard(
            count = "14",
            label = "Visitantes",
            icon = Icons.Default.People,
            color = UgEmerald,
            modifier = Modifier.weight(1f)
        )
        GuardMetricCard(
            count = "3",
            label = "Paquetes",
            icon = Icons.Default.Inventory2,
            color = UgInfoIcon,
            modifier = Modifier.weight(1f)
        )
        GuardMetricCard(
            count = "4",
            label = "Rondas",
            icon = Icons.Default.Shield,
            color = UgAmber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GuardQuickActionsSection(
    onScanQr: () -> Unit,
    onReportIncident: () -> Unit,
    onPackages: () -> Unit
) {
    Text("Acciones Rápidas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UgNavy)
    GuardQuickActions(onScanQr, onReportIncident, onPackages)
}

@Composable
private fun GuardRecentActivitySection() {
    Text(
        "Actividad Reciente en Portería",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = UgNavy
    )
    GuardRecentActivityCard()
}
