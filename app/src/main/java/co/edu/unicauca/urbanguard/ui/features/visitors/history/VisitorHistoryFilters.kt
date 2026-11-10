package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.ui.components.DateFilterField
import co.edu.unicauca.urbanguard.ui.components.FilterDatePickerDialog
import java.time.LocalDate

private enum class HistoryDateField(val label: Int, val clearLabel: Int) {
    FROM(R.string.visitor_history_from, R.string.visitor_history_remove_from),
    TO(R.string.visitor_history_to, R.string.visitor_history_remove_to)
}

@Composable
internal fun VisitorHistoryFilters(
    state: VisitorHistoryUiState,
    onAction: (HistoryAction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryDateFields(state.draft, onAction)
        val rangeError = (state.results as? HistoryResults.Failed)?.error?.takeIf { it.isHistoryRangeError }
        if (state.invalidRange || rangeError != null) {
            Text(
                text = rangeError?.message ?: stringResource(R.string.visitor_history_invalid_range),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onAction(HistoryAction.Apply) },
                enabled = state.draft != state.applied,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.visitor_history_apply))
            }
            if (state.draft.isFiltered || state.applied.isFiltered) {
                TextButton(
                    onClick = { onAction(HistoryAction.Clear) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.visitor_history_clear))
                }
            }
        }
        AppliedDates(state.applied)
    }
}

@Composable
private fun HistoryDateFields(dates: HistoryDates, onAction: (HistoryAction) -> Unit) {
    var picker by rememberSaveable { mutableStateOf<HistoryDateField?>(null) }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
        FlowRow(
            maxItemsInEachRow = if (stack) 1 else 2,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryDateInput(
                field = HistoryDateField.FROM,
                date = dates.from,
                open = { picker = HistoryDateField.FROM },
                clear = { onAction(HistoryAction.EditFrom(null)) },
                modifier = if (stack) Modifier.fillMaxWidth() else Modifier.weight(1f)
            )
            HistoryDateInput(
                field = HistoryDateField.TO,
                date = dates.to,
                open = { picker = HistoryDateField.TO },
                clear = { onAction(HistoryAction.EditTo(null)) },
                modifier = if (stack) Modifier.fillMaxWidth() else Modifier.weight(1f)
            )
        }
    }
    picker?.let { field ->
        FilterDatePickerDialog(
            initialDate = if (field == HistoryDateField.FROM) dates.from else dates.to,
            onDateSelected = { date ->
                val action = when (field) {
                    HistoryDateField.FROM -> HistoryAction.EditFrom(date)
                    HistoryDateField.TO -> HistoryAction.EditTo(date)
                }
                onAction(action)
            },
            onDismiss = { picker = null }
        )
    }
}

@Composable
private fun HistoryDateInput(
    field: HistoryDateField,
    date: LocalDate?,
    open: () -> Unit,
    clear: () -> Unit,
    modifier: Modifier
) {
    val label = stringResource(field.label)
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            DateFilterField(
                date = date,
                onShowDatePicker = open,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                placeholder = stringResource(R.string.visitor_history_choose_date),
                dateFormat = VisitorHistoryDates.dateFormat,
                label = label
            )
            if (date != null) {
                IconButton(onClick = clear, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(field.clearLabel))
                }
            }
        }
    }
}

@Composable
private fun AppliedDates(dates: HistoryDates) {
    val summary = when {
        dates.from != null && dates.to != null -> VisitorHistoryDates.range(dates.from, dates.to)
        dates.from != null -> stringResource(
            R.string.visitor_history_applied_from,
            VisitorHistoryDates.dateFormat.format(dates.from)
        )
        dates.to != null -> stringResource(
            R.string.visitor_history_applied_to,
            VisitorHistoryDates.dateFormat.format(dates.to)
        )
        else -> return
    }
    Text(summary, style = MaterialTheme.typography.bodyMedium)
}
