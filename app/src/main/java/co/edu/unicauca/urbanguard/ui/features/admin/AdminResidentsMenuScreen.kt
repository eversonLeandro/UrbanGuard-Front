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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.ui.components.AdminMenuOptionCard
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground

@Composable
internal fun AdminResidentsMenuScreen(
    onOpenCensus: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Residentes")
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminMenuOptionCard(
                title = "Censo de apartamentos",
                subtitle = "Consulta los apartamentos y sus residentes registrados",
                icon = Icons.Default.Apartment,
                onClick = onOpenCensus
            )
            AdminMenuOptionCard(
                title = "Historial de visitantes",
                subtitle = "Consulta los ingresos de visitantes del conjunto",
                icon = Icons.Default.History,
                onClick = onOpenHistory
            )
        }
    }
}
