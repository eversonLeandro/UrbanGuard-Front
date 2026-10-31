package co.edu.unicauca.urbanguard.ui.features.incidents

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.model.label
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorActionButton
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val detailDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM, HH:mm").withZone(ZoneId.systemDefault())

/**
 * Detalle de la novedad: foto grande, categoría, descripción, punto y hora.
 */
@Composable
fun IncidentDetailScreen(
    reportId: String,
    onBackClick: () -> Unit,
    isAdmin: Boolean = false,
    viewModel: IncidentDetailViewModel = hiltViewModel()
) {
    val incident by viewModel.incident.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    LaunchedEffect(reportId) { viewModel.load(reportId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Novedad",
            onNavigateBack = onBackClick
        )

        val current = incident
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            IncidentLoadError(errorMessage.orEmpty(), onBackClick)
        } else if (current == null) {
            IncidentMissingContent(onBackClick = onBackClick)
        } else {
            IncidentDetailContent(incident = current, onBackClick = onBackClick, isAdmin = isAdmin)
        }
    }
}

@Composable
private fun IncidentDetailContent(
    incident: SecurityIncident,
    onBackClick: () -> Unit,
    isAdmin: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            val request = remember(incident.imageUrl) {
                ImageRequest.Builder(context)
                    .data(incident.imageUrl)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = "Foto de la novedad",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(16.dp))

        IncidentDetailBody(incident = incident, isAdmin = isAdmin)

        Spacer(Modifier.height(24.dp))

        VisitorActionButton(text = "Volver", onClick = onBackClick)
    }
}

@Composable
private fun IncidentDetailBody(incident: SecurityIncident, isAdmin: Boolean) {
    val pointLabel = incident.pointName ?: incident.pointId?.let { "Punto ${it.take(8)}" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            DetailInfoRow(icon = Icons.Default.Category, label = "Categoría", value = incident.category.label())
            Spacer(Modifier.height(14.dp))
            DetailInfoRow(icon = Icons.Default.Description, label = "Descripción", value = incident.description)
            Spacer(Modifier.height(14.dp))
            DetailInfoRow(
                icon = Icons.Default.Schedule,
                label = "Fecha y hora",
                value = detailDateFormat.format(incident.createdAt)
            )
            if (!pointLabel.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                DetailInfoRow(icon = Icons.Default.LocationOn, label = "Punto de vigilancia", value = pointLabel)
            }
            if (isAdmin) {
                Spacer(Modifier.height(14.dp))
                DetailInfoRow(
                    icon = Icons.Default.Person,
                    label = "Guardia reportante",
                    value = incident.guardName ?: "Guardia ${incident.guardId.take(8)}…"
                )
            }
        }
    }
}

@Composable
private fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(UgEmerald.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = UgEmerald, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = UgTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(value, fontSize = 14.sp, color = UgTextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun IncidentLoadError(message: String, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = UgTextPrimary)
        Spacer(Modifier.height(20.dp))
        VisitorActionButton(text = "Volver", onClick = onBackClick)
    }
}

@Composable
private fun IncidentMissingContent(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No se encontró la novedad.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary
            )
            Spacer(Modifier.height(20.dp))
            VisitorActionButton(text = "Volver", onClick = onBackClick)
        }
    }
}
