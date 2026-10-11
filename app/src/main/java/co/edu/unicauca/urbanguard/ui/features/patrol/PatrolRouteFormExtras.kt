package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Piezas visuales del formulario de ruta: resumen vivo, presets de horario
 * y tira de puntos elegidos en orden.
 *
 * Archivo propio para no superar el máximo de funciones de detekt en la
 * pantalla del formulario.
 */

@Composable
fun RouteSummaryHeader(pointCount: Int, scheduleCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(UgNavy.copy(alpha = 0.06f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$pointCount ${if (pointCount == 1) "punto" else "puntos"}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy
        )
        Spacer(Modifier.width(8.dp))
        Text(text = "·", color = UgTextSecondary)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$scheduleCount " +
                if (scheduleCount == 1) "horario" else "horarios",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = UgNavy
        )
    }
}

@Composable
fun SchedulePresetRow(onPreset: (Int, Int, Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = false,
            onClick = { onPreset(8, 0, false) },
            label = { Text("Mañana 08:00", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UgEmerald)
        )
        FilterChip(
            selected = false,
            onClick = { onPreset(22, 0, true) },
            label = { Text("Noche 22:00", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UgEmerald)
        )
    }
}

@Composable
fun SelectedRouteStrip(names: List<String>) {
    if (names.isEmpty()) return
    Column {
        Text(
            text = "Tu ruta",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            names.forEachIndexed { index, name ->
                if (index > 0) {
                    Text(
                        text = "→",
                        color = UgEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Text(
                    text = "${index + 1}. $name",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(UgNavy)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}
