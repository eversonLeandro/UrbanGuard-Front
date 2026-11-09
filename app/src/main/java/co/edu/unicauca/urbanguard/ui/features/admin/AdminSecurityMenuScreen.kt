package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgIconMuted
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyGradient
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun AdminSecurityMenuScreen(
    onNavigateToRegisterGuard: () -> Unit = {},
    onNavigateToGuardManagement: () -> Unit = {},
    onNavigateToPatrolHistory: () -> Unit = {},
    onNavigateToPatrolPoints: () -> Unit = {},
    onNavigateToPatrolRoutes: () -> Unit = {},
    onNavigateToIncidents: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Seguridad"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Gestión de Seguridad",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )

            SecurityOptionCard(
                title = "Registrar guardia de seguridad",
                subtitle = "Crear cuenta y acceso para un nuevo guardia",
                icon = Icons.Default.Security,
                onClick = onNavigateToRegisterGuard
            )

            SecurityOptionCard(
                title = "Gestionar vigilantes",
                subtitle = "Consultar, inactivar o reactivar accesos de este conjunto",
                icon = Icons.Default.Security,
                onClick = onNavigateToGuardManagement
            )

            if (PatrolConstants.PATROL_HISTORY_REMOTE) {
                SecurityOptionCard(
                    title = "Historial de patrullaje",
                    subtitle = "Ver el registro de rondas de los guardias",
                    icon = Icons.Default.History,
                    onClick = onNavigateToPatrolHistory
                )
            }

            SecurityOptionCard(
                title = "Novedades",
                subtitle = "Consultar novedades reportadas por los guardias",
                icon = Icons.Default.ReportProblem,
                onClick = onNavigateToIncidents
            )

            SecurityOptionCard(
                title = "Puntos de control NFC",
                subtitle = "Registrar y editar los puntos del conjunto",
                icon = Icons.Default.Nfc,
                onClick = onNavigateToPatrolPoints
            )

            SecurityOptionCard(
                title = "Rutas de patrullaje",
                subtitle = "Armar rutas con puntos y horarios",
                icon = Icons.Default.Route,
                onClick = onNavigateToPatrolRoutes
            )
        }
    }
}

@Composable
private fun SecurityOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UgNavyGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = UgNavy)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 13.sp, color = UgTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = UgIconMuted)
        }
    }
}
