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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.IncidentSyncState
import co.edu.unicauca.urbanguard.domain.model.SecurityIncident
import co.edu.unicauca.urbanguard.domain.model.label
import co.edu.unicauca.urbanguard.ui.components.LocalPhotoImage
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.features.visitors.VisitorActionButton
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
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
    onBackClick: () -> Unit,
    viewModel: IncidentDetailViewModel = hiltViewModel()
) {
    val incident by viewModel.incident.collectAsStateWithLifecycle()

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
        if (current == null) {
            IncidentMissingContent(onBackClick = onBackClick)
        } else {
            IncidentDetailContent(incident = current, onBackClick = onBackClick)
        }
    }
}

@Composable
private fun IncidentDetailContent(incident: SecurityIncident, onBackClick: () -> Unit) {
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
            LocalPhotoImage(
                path = incident.photoPath,
                size = 260.dp,
                contentDescription = "Foto de la novedad",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        IncidentDetailBody(incident = incident)

        Spacer(Modifier.height(24.dp))

        VisitorActionButton(text = "Volver", onClick = onBackClick)
    }
}

@Composable
private fun IncidentDetailBody(incident: SecurityIncident) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = incident.category.label(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(UgNavy)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (incident.syncState == IncidentSyncState.SYNCED) {
                    "ENVIADA"
                } else {
                    "PENDIENTE"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (incident.syncState == IncidentSyncState.SYNCED) {
                    UgEmerald
                } else {
                    UgAmber
                }
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = incident.description,
            fontSize = 15.sp,
            color = UgTextPrimary
        )

        if (!incident.pointName.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = incident.pointName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UgNavy
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = detailDateFormat.format(incident.createdAt),
            fontSize = 12.5.sp,
            color = UgTextSecondary
        )
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
