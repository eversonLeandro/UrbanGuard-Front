package co.edu.unicauca.urbanguard.ui.features.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.Package
import co.edu.unicauca.urbanguard.domain.model.PackageStatus
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.components.StatusBadge
import co.edu.unicauca.urbanguard.ui.components.StatusTone
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.OutlinedTextFieldDefaults
import co.edu.unicauca.urbanguard.ui.components.DateFilterField
import co.edu.unicauca.urbanguard.ui.components.FilterDatePickerDialog
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun PackageListScreen(
    onRegisterPackage: () -> Unit,
    onPackageDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: PackageListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val canRegister = viewModel.canRegister()
    var delivery by remember { mutableStateOf<Package?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedStatus by remember { mutableStateOf<PackageStatus?>(null) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.load() }
    val filteredPackages = remember(
        state.packages,
        searchQuery,
        selectedDate,
        selectedStatus
    ) {
        filterPackages(state.packages, searchQuery, selectedDate, selectedStatus)
    }

    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(title = "Paquetes", onNavigateBack = onNavigateBack)
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp)) {
            PackageFilterBar(
                values = PackageFilterValues(
                    searchQuery = searchQuery,
                    selectedDate = selectedDate,
                    selectedStatus = selectedStatus,
                    statusMenuExpanded = statusMenuExpanded
                ),
                actions = PackageFilterActions(
                    onSearchQueryChange = { searchQuery = it },
                    onClearDate = { selectedDate = null },
                    onShowDatePicker = { showDatePicker = true },
                    onStatusMenuExpandedChange = { statusMenuExpanded = it },
                    onStatusChange = { selectedStatus = it }
                )
            )
        }
        PackageListBody(
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            allPackages = state.packages,
            filteredPackages = filteredPackages,
            canRegister = canRegister,
            onDeliver = { delivery = it },
            onPackageOpen = { onPackageDetail(it.id) }
        )
        if (canRegister) {
            Button(
                onClick = onRegisterPackage,
                colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(20.dp).size(56.dp)
            ) { Text("Registrar ingreso") }
        }
    }
    if (showDatePicker) {
        FilterDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }

    PackageDeliveryDialog(
        parcel = delivery,
        onDismiss = { delivery = null },
        onDeliver = { parcel, deliveredTo ->
            viewModel.deliver(parcel.id, deliveredTo)
            delivery = null
        }
    )
}

@Composable
private fun ColumnScope.PackageListBody(
    isLoading: Boolean,
    errorMessage: String?,
    allPackages: List<Package>,
    filteredPackages: List<Package>,
    canRegister: Boolean,
    onDeliver: (Package) -> Unit,
    onPackageOpen: (Package) -> Unit
) {
    when {
        isLoading -> Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = UgNavy) }
        errorMessage != null -> PackageEmptyState(
            modifier = Modifier.weight(1f),
            message = errorMessage
        )
        filteredPackages.isEmpty() -> PackageEmptyState(
            modifier = Modifier.weight(1f),
            message = if (allPackages.isEmpty()) {
                "No existen paquetes registrados"
            } else {
                "No hay paquetes con esos filtros"
            }
        )
        else -> LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredPackages, key = { it.id }) { parcel ->
                PackageCard(
                    parcel,
                    canRegister && parcel.status == PackageStatus.RECIBIDO,
                    onDeliver = { onDeliver(parcel) },
                    onOpen = { onPackageOpen(parcel) }
                )
            }
        }
    }
}

@Composable
private fun PackageEmptyState(
    modifier: Modifier = Modifier,
    message: String
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(UgSurface, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(34.dp)
            )
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
        Text(
            text = message,
            color = UgNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun PackageCard(parcel: Package, canDeliver: Boolean, onDeliver: () -> Unit, onOpen: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = UgSurface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onOpen
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp)
                        .background(UgEmerald.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Inventory2, null, tint = UgEmerald, modifier = Modifier.size(24.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("PAQUETE", color = UgEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        parcel.description,
                        fontWeight = FontWeight.Bold,
                        color = UgNavy,
                        fontSize = 16.sp,
                        maxLines = 2
                    )
                }
                Icon(Icons.Default.ChevronRight, null, tint = UgTextSecondary)
            }
            HorizontalDivider(color = UgBorder)
            if (!parcel.residentialUnitLabel.isNullOrBlank()) {
                PackageInfoRow(Icons.Default.Apartment, "Unidad", parcel.residentialUnitLabel)
            }
            PackageInfoRow(Icons.Default.Person, "Destinatario", parcel.recipientName)
            PackageInfoRow(Icons.Default.Schedule, "Recibido", parcel.receivedAt.take(16).replace('T', ' '))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Estado", color = UgTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                StatusChip(parcel.status)
            }
            if (canDeliver) {
                Button(
                    onClick = onDeliver,
                    colors = ButtonDefaults.buttonColors(containerColor = UgNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Registrar entrega", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun PackageInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = UgTextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", color = UgTextSecondary, fontSize = 13.sp)
        Text(value, color = UgNavy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusChip(status: PackageStatus) {
    val text = status.name.lowercase().replaceFirstChar { it.uppercase() }
    val tone = when (status) {
        PackageStatus.RECIBIDO -> StatusTone.INFO
        PackageStatus.ENTREGADO -> StatusTone.SUCCESS
        PackageStatus.DEVUELTO -> StatusTone.WARNING
        PackageStatus.CANCELADO -> StatusTone.ERROR
    }
    StatusBadge(text, tone)
}
