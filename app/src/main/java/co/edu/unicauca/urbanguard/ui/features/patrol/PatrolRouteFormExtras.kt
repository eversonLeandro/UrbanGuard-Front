package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

/**
 * Piezas visuales del formulario de ruta: resumen vivo
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
fun SelectedRouteStrip(
    points: List<PatrolPointOption>,
    onMove: (String, Int) -> Unit
) {
    if (points.isEmpty()) return
    Column {
        Text(
            text = "Orden de la ruta",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextSecondary
        )
        Spacer(Modifier.height(6.dp))
        points.forEachIndexed { index, point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(UgNavy)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = point.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UgNavy,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onMove(point.id, -1) },
                    enabled = index > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Subir parada",
                        tint = if (index > 0) UgNavy else UgTextSecondary.copy(alpha = 0.4f)
                    )
                }
                IconButton(
                    onClick = { onMove(point.id, 1) },
                    enabled = index < points.lastIndex
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Bajar parada",
                        tint = if (index < points.lastIndex) {
                            UgNavy
                        } else {
                            UgTextSecondary.copy(alpha = 0.4f)
                        }
                    )
                }
            }
        }
    }
}
