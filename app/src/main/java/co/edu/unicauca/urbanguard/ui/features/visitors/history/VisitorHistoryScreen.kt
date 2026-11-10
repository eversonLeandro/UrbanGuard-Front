package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.R
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.ui.components.EmptyState
import co.edu.unicauca.urbanguard.ui.components.ErrorBanner
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground

@Composable
fun VisitorHistoryScreen(
    onNavigateBack: () -> Unit,
    onSelectComplex: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: VisitorHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    VisitorHistoryContent(state, viewModel::onAction, onNavigateBack, onSelectComplex, onSessionExpired)
}

@Composable
internal fun VisitorHistoryContent(
    state: VisitorHistoryUiState,
    onAction: (HistoryAction) -> Unit,
    onNavigateBack: () -> Unit,
    onSelectComplex: () -> Unit,
    onSessionExpired: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(UgBackground)) {
        UrbanGuardTopBar(
            title = stringResource(R.string.visitor_history_title),
            onNavigateBack = onNavigateBack
        )
        key(state.listRevision) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (val results = state.results) {
                    HistoryResults.NeedsComplex -> item { ChooseComplex(onSelectComplex) }
                    is HistoryResults.Unavailable -> item { AccessError(results.error, onSessionExpired) }
                    else -> historyResults(state, results, onAction)
                }
            }
        }
    }
}

private fun LazyListScope.historyResults(
    state: VisitorHistoryUiState,
    results: HistoryResults,
    onAction: (HistoryAction) -> Unit
) {
    item(key = "filters") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.complexName?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            VisitorHistoryFilters(state, onAction)
        }
    }
    when (results) {
        HistoryResults.Loading -> item { HistoryProgress(R.string.visitor_history_loading) }
        is HistoryResults.Failed -> if (!results.error.isHistoryRangeError) {
            item {
                ErrorView(
                    error = readableHistoryError(results.error),
                    onRetry = { onAction(HistoryAction.Retry) }
                )
            }
        }
        is HistoryResults.Content -> historyPage(results, state.applied.isFiltered, onAction)
        else -> Unit
    }
}

private fun LazyListScope.historyPage(
    content: HistoryResults.Content,
    filtered: Boolean,
    onAction: (HistoryAction) -> Unit
) {
    item(key = "refresh") { HistoryRefresh(content.operation, onAction) }
    if (content.items.isEmpty()) {
        item(key = "empty") {
            EmptyState(
                title = stringResource(
                    if (filtered) R.string.visitor_history_filtered_empty else R.string.visitor_history_empty
                ),
                description = "",
                icon = Icons.Default.History
            )
        }
    } else {
        items(content.items, key = { it.invitationId }) { VisitorHistoryCard(it) }
    }
    item(key = "page") { HistoryPageFooter(content, onAction) }
}

@Composable
private fun HistoryRefresh(operation: HistoryOperation, onAction: (HistoryAction) -> Unit) {
    Column {
        TextButton(
            onClick = { onAction(HistoryAction.Refresh) },
            enabled = operation != HistoryOperation.Refreshing,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.visitor_history_refresh))
        }
        when (operation) {
            HistoryOperation.Refreshing -> HistoryProgress(R.string.visitor_history_refreshing)
            is HistoryOperation.RefreshFailed -> HistoryErrorBanner(operation.error, onAction)
            else -> Unit
        }
    }
}

@Composable
private fun HistoryPageFooter(content: HistoryResults.Content, onAction: (HistoryAction) -> Unit) {
    when (val operation = content.operation) {
        HistoryOperation.LoadingNext -> HistoryProgress(R.string.visitor_history_loading_next, compact = true)
        is HistoryOperation.NextFailed -> HistoryPageError(operation.error, onAction)
        else -> if (content.nextPage != null) {
            OutlinedButton(
                onClick = { onAction(HistoryAction.LoadNext) },
                enabled = operation != HistoryOperation.Refreshing,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.visitor_history_next))
            }
        }
    }
}

@Composable
private fun HistoryErrorBanner(error: AppError, onAction: (HistoryAction) -> Unit) {
    ErrorBanner(
        message = readableHistoryError(error).message,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        onRetry = { onAction(HistoryAction.Retry) }
    )
}

@Composable
private fun readableHistoryError(error: AppError): AppError =
    if (error is AppError.Timeout) AppError.Timeout(stringResource(R.string.visitor_history_timeout)) else error
