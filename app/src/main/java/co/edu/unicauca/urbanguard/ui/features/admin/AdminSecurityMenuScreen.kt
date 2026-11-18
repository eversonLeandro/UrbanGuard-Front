package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.core.common.PatrolConstants
import co.edu.unicauca.urbanguard.ui.components.AdminMenuOptionCard
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgNavy

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

            AdminMenuOptionCard(
                title = "Registrar guardia de seguridad",
                subtitle = "Crear cuenta y acceso para un nuevo guardia",
                icon = Icons.Default.Security,
                onClick = onNavigateToRegisterGuard
            )

            AdminMenuOptionCard(
                title = "Gestionar vigilantes",
                subtitle = "Consultar, inactivar o reactivar accesos de este conjunto",
                icon = Icons.Default.ManageAccounts,
                onClick = onNavigateToGuardManagement
            )

            if (PatrolConstants.PATROL_HISTORY_REMOTE) {
                AdminMenuOptionCard(
                    title = "Historial de patrullaje",
                    subtitle = "Ver el registro de rondas de los guardias",
                    icon = Icons.Default.History,
                    onClick = onNavigateToPatrolHistory
                )
            }

            AdminMenuOptionCard(
                title = "Novedades",
                subtitle = "Consultar novedades reportadas por los guardias",
                icon = Icons.Default.ReportProblem,
                onClick = onNavigateToIncidents
            )

            AdminMenuOptionCard(
                title = "Puntos de control NFC",
                subtitle = "Registrar y editar los puntos del conjunto",
                icon = Icons.Default.Nfc,
                onClick = onNavigateToPatrolPoints
            )

            AdminMenuOptionCard(
                title = "Rutas de patrullaje",
                subtitle = "Armar rutas con puntos y horarios",
                icon = Icons.Default.Route,
                onClick = onNavigateToPatrolRoutes
            )
        }
    }
}
