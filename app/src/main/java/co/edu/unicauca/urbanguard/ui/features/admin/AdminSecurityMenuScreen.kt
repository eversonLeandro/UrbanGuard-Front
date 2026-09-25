package co.edu.unicauca.urbanguard.ui.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.*

@Composable
fun AdminSecurityMenuScreen(
    onNavigateToRegisterGuard: () -> Unit = {},
    onNavigateToPatrolHistory: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Seguridad",
            showMenu = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                title = "Historial de patrullaje",
                subtitle = "Ver el registro de rondas de los guardias",
                icon = Icons.Default.History,
                onClick = onNavigateToPatrolHistory
            )

            SecurityOptionCard(
                title = "Hacer ronda de patrullaje",
                subtitle = "Registrar puntos de control mediante NFC",
                icon = Icons.Default.Nfc,
                onClick = { /* TODO */ }
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
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
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
