package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PackageMetrics
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgAmber
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInfoText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgWarningText

@Composable
fun PackageMetricsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PackageMetricsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var periodMenuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Métricas de paquetería", onNavigateBack = onNavigateBack)
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UgNavy)
            }
            state.errorMessage != null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(state.errorMessage ?: "", color = UgTextSecondary)
            }
            state.metrics != null -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = UgSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Filtrar por", color = UgTextSecondary, fontSize = 12.sp)
                        Box {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                            .background(UgSurface).border(1.dp, UgBorder, RoundedCornerShape(12.dp))
                            .clickable { periodMenuExpanded = true }.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state.period.label, color = UgNavy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = UgTextSecondary)
                    }
                    DropdownMenu(expanded = periodMenuExpanded, onDismissRequest = { periodMenuExpanded = false }) {
                        PackageMetricsPeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.label) },
                                onClick = { periodMenuExpanded = false; viewModel.load(period) }
                            )
                        }
                    }
                        }
                    }
                }
                MetricsContent(state.metrics!!)
            }
        }
    }
}

@Composable
private fun MetricsContent(metrics: PackageMetrics) {
    val total = metrics.receivedCount.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Resumen de paquetes",
            color = UgNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                "Recibidos", metrics.receivedCount.toString(), Icons.Default.Inventory2,
                UgInfoText, Modifier.weight(1f)
            )
            MetricCard(
                "Entregados", metrics.deliveredCount.toString(), Icons.Default.Inventory2,
                UgEmerald, Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                "Pendientes", metrics.pendingCount.toString(), Icons.Default.Schedule,
                UgAmber, Modifier.weight(1f)
            )
            MetricCard(
                "Devueltos", metrics.returnedCount.toString(), Icons.Default.Replay,
                UgWarningText, Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                "Cancelados", metrics.canceledCount.toString(), Icons.Default.Cancel,
                UgErrorText, Modifier.weight(1f)
            )
            MetricCard(
                "Total gestionado",
                managedPackageCount(metrics).toString(),
                Icons.Default.GridView,
                UgNavy,
                Modifier.weight(1f)
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = UgSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Paquetes por estado", color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Estados actuales de los paquetes", color = UgTextSecondary, fontSize = 12.sp)
                StatusMetricRow("Recibidos", metrics.pendingCount, UgInfoText, total)
                StatusMetricRow("Entregados", metrics.deliveredCount, UgEmerald, total)
                StatusMetricRow("Devueltos", metrics.returnedCount, UgAmber, total)
                StatusMetricRow("Cancelados", metrics.canceledCount, UgErrorText, total)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = UgSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Actividad reciente", color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (metrics.trend.isEmpty()) {
                    Text("No hay actividad registrada en este periodo.", color = UgTextSecondary, fontSize = 13.sp)
                } else {
                    val maxActivity = metrics.trend
                        .maxOfOrNull { it.receivedCount + it.deliveredCount }
                        ?.coerceAtLeast(1) ?: 1
                    metrics.trend.forEach { point ->
                        ActivityRow(point, maxActivity)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(38.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(value, color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(title, color = UgTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatusMetricRow(label: String, count: Long, color: Color, total: Long) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = UgTextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Box(Modifier.weight(1.5f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(UgBorder)) {
            Box(
                Modifier.fillMaxWidth(count.toFloat() / total.toFloat())
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(count.toString(), color = UgNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun ActivityRow(point: co.edu.unicauca.urbanguard.domain.model.PackageTrendMetric, maxActivity: Long) {
    val total = point.receivedCount + point.deliveredCount
    val fraction = (total.toFloat() / maxActivity.toFloat()).coerceAtLeast(0.08f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(58.dp)) {
            Text(
                point.date.takeLast(5).replace('-', '/'),
                color = UgNavy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Text("${total} mov.", color = UgTextSecondary, fontSize = 10.sp)
        }

        Box(Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(8.dp)).background(UgBorder)) {
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(UgInfoText, RoundedCornerShape(8.dp)))
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("${point.receivedCount} recib.", color = UgInfoText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("${point.deliveredCount} entreg.", color = UgEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }

}

private fun managedPackageCount(
    metrics: co.edu.unicauca.urbanguard.domain.model.PackageMetrics
): Long = metrics.pendingCount +
    metrics.deliveredCount +
    metrics.returnedCount +
    metrics.canceledCount
