package co.edu.unicauca.urbanguard.ui.features.guard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgBorderSoft
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBg
import co.edu.unicauca.urbanguard.ui.theme.UgErrorBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgNavyLight
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun GuardVisitantesScreen(
    navController: NavHostController,
    onNotificationsClick: (() -> Unit)? = null,
    onScanQr: () -> Unit = {},
    viewModel: GuardVisitantesViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(0) } // 0: Todos, 1: En Predio, 2: Finalizados

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Control de Visitantes",
            onNotificationsClick = onNotificationsClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Buscador ─────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Buscar por nombre o apto...",
                        fontSize = 13.5.sp,
                        color = UgTextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = UgNavy
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = UgNavy,
                    unfocusedBorderColor = UgBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = UgNavy.copy(alpha = 0.08f)
                    )
            )

            // ── Filtros ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 },
                    label = { Text("Todos (3)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UgNavy,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 },
                    label = {
                        Text(
                            "En Predio (2)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UgEmerald,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == 2,
                    onClick = { selectedFilter = 2 },
                    label = { Text("Salidas (1)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UgNavyLight,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // ── Fecha del día ────────────────────────────────────
            val todayDate = java.time.LocalDate.now()
                .format(
                    java.time.format.DateTimeFormatter.ofPattern(
                        "dd 'de' MMMM yyyy",
                        java.util.Locale("es", "CO")
                    )
                )
            Text(
                text = "Visitantes de hoy — $todayDate",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = UgTextSecondary
            )

            // ── Botón Escanear QR ────────────────────────────────────
            Button(
                onClick = onScanQr,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgEmerald),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Escanear Código QR de Visitante",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            // ── Botón Nuevo Registro ────────────────────────────────
            Button(
                onClick = {},
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Registrar Nuevo Visitante Manual",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            // ── Lista de Tarjetas de Visitantes ─────────────────────
            VisitorCard(
                name = "Carlos Andrés Mendoza",
                destination = "Apto 504 - Residente: Ana Silva",
                entryTime = "10:15 AM",
                status = "DENTRO EN PREDIO",
                statusColor = UgEmerald,
                onActionClick = {}
            )

            VisitorCard(
                name = "María Fernanda Gómez",
                destination = "Apto 201 - Residente: Pedro López",
                entryTime = "09:30 AM",
                status = "DENTRO EN PREDIO",
                statusColor = UgEmerald,
                onActionClick = {}
            )

            VisitorCard(
                name = "Repartidor MercadoLibre",
                destination = "Apto 302 - Residente: Carlos Ruiz",
                entryTime = "08:45 AM - Salida: 08:52 AM",
                status = "SALIDA REGISTRADA",
                statusColor = UgTextSecondary,
                isCompleted = true,
                onActionClick = {}
            )
        }
    }
}

@Composable
private fun VisitorCard(
    name: String,
    destination: String,
    entryTime: String,
    status: String,
    statusColor: Color,
    isCompleted: Boolean = false,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status badge placed at the top above the name
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(status, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(UgInputBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = UgNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = UgNavy)
            }

            HorizontalDivider(color = UgBorderSoft)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    destination,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = UgNavy
                )
                Text("Ingreso: $entryTime", fontSize = 11.5.sp, color = UgTextSecondary)
            }

            if (!isCompleted) {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UgErrorBg,
                        contentColor = UgErrorText
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UgErrorBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text("Marcar Salida de Predio", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
