package co.edu.unicauca.urbanguard.ui.features.residential

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.ResidentialUnit
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgAmberSoft
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessBg
import co.edu.unicauca.urbanguard.ui.theme.UgSuccessText
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun CensusScreen(viewModel: CensusViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val visibleUnits = remember(state.units, state.searchQuery, state.filter) {
        state.visibleUnits()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
            .imePadding()
    ) {
        UrbanGuardTopBar(title = "Censo de apartamentos")
        when {
            state.isLoading && state.units.isEmpty() -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = UgEmerald)
            }

            state.error != null -> ErrorView(
                error = state.error!!,
                modifier = Modifier.weight(1f),
                onRetry = viewModel::onRefresh
            )

            state.isEmpty -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                CensusStatistics(state)
                EmptyCensus(modifier = Modifier.weight(1f))
            }

            else -> {
                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = UgEmerald
                    )
                }
                CensusContent(
                    state = state,
                    visibleUnits = visibleUnits,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onFilterChange = viewModel::onFilterChange,
                    onUnitSelected = viewModel::onUnitSelected
                )
            }
        }
    }

    state.selectedUnit?.let { selectedUnit ->
        if (!state.isLoading && state.error == null) {
            UnitDetailSheet(
                unit = selectedUnit,
                onDismiss = viewModel::onDismissUnitDetail
            )
        }
    }
}

@Composable
private fun CensusSearchAndFilters(
    state: CensusUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (CensusUnitFilter) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        placeholder = { Text("Buscar apartamento o residente") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar")
        },
        trailingIcon = {
            if (state.searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = UgEmerald,
            unfocusedIndicatorColor = UgBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CensusUnitFilter.entries.forEach { filter ->
            FilterChip(
                selected = state.filter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.label, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
private fun CensusContent(
    state: CensusUiState,
    visibleUnits: List<ResidentialUnit>,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (CensusUnitFilter) -> Unit,
    onUnitSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        CensusStatistics(state = state)
        Spacer(Modifier.height(12.dp))
        CensusSearchAndFilters(
            state = state,
            onSearchQueryChange = onSearchQueryChange,
            onFilterChange = onFilterChange
        )
        Spacer(Modifier.height(8.dp))

        if (visibleUnits.isEmpty()) {
            NoCensusMatches(
                query = state.searchQuery,
                onClear = {
                    onSearchQueryChange("")
                    onFilterChange(CensusUnitFilter.ALL)
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val columns = if (maxWidth >= 520.dp) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleUnits, key = ResidentialUnit::id) { unit ->
                        ApartmentGridItem(
                            unit = unit,
                            onClick = { onUnitSelected(unit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CensusStatistics(state: CensusUiState) {
    val occupiedUnits = remember(state.units) { state.occupiedUnits }
    val unoccupiedUnits = remember(state.units) { state.unoccupiedUnits }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CensusStatistic(
            value = state.units.size,
            label = "Total",
            tint = UgNavy,
            modifier = Modifier.weight(1f)
        )
        CensusStatistic(
            value = occupiedUnits,
            label = "Asociados",
            tint = UgSuccessText,
            modifier = Modifier.weight(1f)
        )
        CensusStatistic(
            value = unoccupiedUnits,
            label = "Sin asociar",
            tint = CensusVacantText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CensusStatistic(
    value: Int,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, UgBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
                lineHeight = 20.sp
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = UgTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ApartmentGridItem(
    unit: ResidentialUnit,
    onClick: () -> Unit
) {
    val isOccupied = unit.residents.isNotEmpty()
    val accent = if (isOccupied) UgSuccessText else CensusVacantText
    val surface = if (isOccupied) UgSuccessBg else UgAmberSoft
    val status = if (isOccupied) "Asociado" else "Sin asociar"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isOccupied) Icons.Default.Group else Icons.Default.Apartment,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = unit.identifier,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = UgNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = status,
                fontSize = 12.sp,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NoCensusMatches(
    query: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = UgTextSecondary,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (query.isNotBlank()) {
                "No hay apartamentos o residentes que coincidan con “${query.trim()}”."
            } else {
                "No hay apartamentos que coincidan con este filtro."
            },
            color = UgTextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
        TextButton(onClick = onClear) {
            Text("Limpiar búsqueda y filtros")
        }
    }
}

@Composable
private fun EmptyCensus(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Este conjunto aún no tiene apartamentos registrados.",
            color = UgTextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UnitDetailSheet(
    unit: ResidentialUnit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Apartamento ${unit.identifier}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = UgNavy
            )
            if (unit.residents.isEmpty()) {
                Text(
                    text = "Sin residentes asociados",
                    color = CensusVacantText,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = if (unit.residents.size == 1) "Residente asociado" else "Residentes asociados",
                    color = UgTextSecondary,
                    style = MaterialTheme.typography.titleMedium
                )
                unit.residents.forEach { resident ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = UgSuccessText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(resident.fullName, color = UgNavy)
                    }
                }
            }
        }
    }
}

private val CensusVacantText = Color(0xFF92400E)
