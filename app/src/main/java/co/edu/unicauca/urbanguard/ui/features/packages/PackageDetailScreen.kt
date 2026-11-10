package co.edu.unicauca.urbanguard.ui.features.packages

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import androidx.compose.material3.HorizontalDivider
import co.edu.unicauca.urbanguard.domain.model.PackageStatus

@Composable
fun PackageDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PackageDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Detalle del paquete", onNavigateBack = onNavigateBack)
        when {
            state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UgNavy)
            }
            state.errorMessage != null -> Box(
                Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(state.errorMessage ?: "", color = UgTextSecondary)
            }
            state.parcel != null -> PackageDetailContent(state.parcel!!)
        }
    }
}

@Composable
private fun PackageDetailContent(parcel: Package) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!parcel.evidenceUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(parcel.evidenceUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto del paquete",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(18.dp))
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(UgNavy.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory2, null, tint = UgNavy, modifier = Modifier.size(42.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Sin foto registrada", color = UgTextSecondary, fontSize = 13.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = UgSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Destinatario", fontSize = 12.sp, color = UgTextSecondary)
                        Text(parcel.recipientName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UgNavy)
                    }
                    PackageStatusBadge(parcel.status.name)
                }
                HorizontalDivider(color = UgBorder)
                DetailRow(Icons.Default.Apartment, "Unidad", parcel.residentialUnitLabel ?: "No registrada")
                DetailRow(Icons.Default.LocalShipping, "Remitente", parcel.sender ?: "No registrado")
                DetailRow(Icons.Default.Schedule, "Recepción", formatDateTime(parcel.receivedAt))
                DetailRow(Icons.Default.Person, "Entregado a", parcel.deliveredTo ?: "Pendiente de entrega")
            }
        }

        DetailCard("Descripción", Icons.Default.Description, parcel.description ?: "Sin descripción")
        DetailCard("Observaciones", Icons.Default.Description, parcel.observations ?: "Sin observaciones")
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = UgNavy, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = UgTextSecondary)
            Text(value, fontSize = 14.sp, color = UgTextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PackageStatusBadge(status: String) {
    val packageStatus = runCatching { PackageStatus.valueOf(status) }.getOrNull()
    val tone = when (packageStatus) {
        PackageStatus.RECIBIDO -> StatusTone.INFO
        PackageStatus.ENTREGADO -> StatusTone.SUCCESS
        PackageStatus.DEVUELTO -> StatusTone.WARNING
        PackageStatus.CANCELADO -> StatusTone.ERROR
        null -> StatusTone.NEUTRAL
    }
    StatusBadge(status.lowercase().replaceFirstChar { it.uppercase() }, tone)
}

@Composable
private fun DetailCard(title: String, icon: ImageVector, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = UgEmerald, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = UgNavy, fontSize = 15.sp)
            }
            Text(value, color = UgTextPrimary, lineHeight = 21.sp)
        }
    }
}

private fun formatDateTime(value: String): String {
    return value.replace('T', ' ').take(16)
}
