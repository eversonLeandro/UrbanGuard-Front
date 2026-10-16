package co.edu.unicauca.urbanguard.ui.features.patrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.domain.model.PatrolShiftType
import co.edu.unicauca.urbanguard.ui.components.FilterDatePickerDialog
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import java.time.LocalDate

private val CONTENT_PADDING = 20.dp

/** Campo del rango que se está editando en el selector de fecha. */
private enum class DateField { FROM, TO }

/**
 * Historial de patrullaje por turno (HU-034).
 *
 * Lista los turnos que el backend devuelve para el rango elegido (por defecto
 * los últimos 7 días) y permite filtrar por incumplimientos. Pulsar un turno
 * abre su detalle.
 */
@Composable
fun PatrolHistoryScreen(
    onNavigateBack: () -> Unit,
    onShiftClick: (PatrolShiftType, LocalDate, LocalDate, LocalDate) -> Unit,
    viewModel: PatrolHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingField by remember { mutableStateOf<DateField?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Historial de patrullaje",
            onNavigateBack = onNavigateBack
        )

        HistoryContent(
            uiState = uiState,
            onRetry = viewModel::onRefresh,
            onFromClick = { editingField = DateField.FROM },
            onToClick = { editingField = DateField.TO },
            onOnlyIncompleteChange = viewModel::onOnlyIncompleteChange,
            onShiftClick = { shiftType, date, from, to ->
                onShiftClick(shiftType, date, from, to)
            }
        )
    }

    when (editingField) {
        DateField.FROM -> FilterDatePickerDialog(
            initialDate = uiState.from,
            onDateSelected = viewModel::onFromSelected,
            onDismiss = { editingField = null }
        )
        DateField.TO -> FilterDatePickerDialog(
            initialDate = uiState.to,
            onDateSelected = viewModel::onToSelected,
            onDismiss = { editingField = null }
        )
        null -> Unit
    }
}

@Composable
private fun HistoryContent(
    uiState: PatrolHistoryUiState,
    onRetry: () -> Unit,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onOnlyIncompleteChange: (Boolean) -> Unit,
    onShiftClick: (PatrolShiftType, LocalDate, LocalDate, LocalDate) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))
        HistoryFilterHeader(
            uiState = uiState,
            onFromClick = onFromClick,
            onToClick = onToClick,
            onOnlyIncompleteChange = onOnlyIncompleteChange
        )
        Spacer(Modifier.height(16.dp))
        HistoryShiftList(
            uiState = uiState,
            onRetry = onRetry,
            onShiftClick = onShiftClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HistoryFilterHeader(
    uiState: PatrolHistoryUiState,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onOnlyIncompleteChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = CONTENT_PADDING)) {
        Text(
            text = "Turnos por rango de fechas",
            fontSize = 15.sp,
            color = UgNavy
        )
        Spacer(Modifier.height(12.dp))
        HistoryFilters(
            from = uiState.from,
            to = uiState.to,
            onlyIncomplete = uiState.onlyIncomplete,
            onFromClick = onFromClick,
            onToClick = onToClick,
            onOnlyIncompleteChange = onOnlyIncompleteChange
        )
        val rangeError = uiState.rangeError
        if (rangeError != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = rangeError, fontSize = 12.sp, color = UgErrorText)
        }
    }
}

@Composable
private fun HistoryShiftList(
    uiState: PatrolHistoryUiState,
    onRetry: () -> Unit,
    onShiftClick: (PatrolShiftType, LocalDate, LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val error = uiState.errorMessage
        when {
            uiState.isLoading -> HistoryLoading()
            error != null -> HistoryError(message = error, onRetryClick = onRetry)
            uiState.isEmpty -> HistoryEmpty()
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = CONTENT_PADDING,
                    end = CONTENT_PADDING,
                    bottom = CONTENT_PADDING
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.shifts, key = { "${it.shiftType}-${it.date}" }) { shift ->
                    ShiftSummaryCard(shift = shift) {
                        onShiftClick(shift.shiftType, shift.date, uiState.from, uiState.to)
                    }
                }
            }
        }
    }
}
