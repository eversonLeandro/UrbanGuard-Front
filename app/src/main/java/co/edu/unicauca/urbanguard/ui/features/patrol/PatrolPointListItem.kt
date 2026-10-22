package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.domain.model.PatrolControlPoint
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/** Tarjeta de un checkpoint con su tag y el interruptor de activo. */
@Composable
fun PatrolPointListItem(
    point: PatrolControlPoint,
    actions: PatrolPointActions
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (point.isActive) UgNavy else UgTextSecondary.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Card(
            onClick = actions.onEdit,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.weight(1f)
        ) {
            PointCardBody(point = point, actions = actions)
        }
    }
}

@Composable
private fun PointCardBody(
    point: PatrolControlPoint,
    actions: PatrolPointActions
) {
    Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = point.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy,
                modifier = Modifier.weight(1f)
            )
            PointActiveBadge(isActive = point.isActive)
        }

        if (point.location.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = point.location,
                fontSize = 12.5.sp,
                color = UgTextSecondary
            )
        }

        if (point.description.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = point.description,
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Tag ${point.nfcTagId}",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = UgTextSecondary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(UgBorderSoft)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Activo en rutas",
                fontSize = 13.sp,
                color = UgTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = point.isActive,
                onCheckedChange = actions.onToggleActive,
                colors = SwitchDefaults.colors(checkedTrackColor = UgEmerald)
            )
        }
    }
}

@Composable
private fun PointActiveBadge(isActive: Boolean) {
    val color = if (isActive) UgEmerald else UgTextSecondary
    Text(
        text = if (isActive) "ACTIVO" else "INACTIVO",
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}
