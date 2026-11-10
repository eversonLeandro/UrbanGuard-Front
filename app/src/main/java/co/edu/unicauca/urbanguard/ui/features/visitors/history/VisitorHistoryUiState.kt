package co.edu.unicauca.urbanguard.ui.features.visitors.history

import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryItem
import java.time.LocalDate

internal data class HistoryDates(val from: LocalDate? = null, val to: LocalDate? = null) {
    val isFiltered: Boolean get() = from != null || to != null
    val isInvalid: Boolean get() = from != null && to != null && from > to
}

internal data class VisitorHistoryUiState(
    val complexName: String? = null,
    val draft: HistoryDates = HistoryDates(),
    val applied: HistoryDates = HistoryDates(),
    val invalidRange: Boolean = false,
    val results: HistoryResults = HistoryResults.Loading,
    val listRevision: Long = 0
)

internal sealed interface HistoryResults {
    data object NeedsComplex : HistoryResults
    data object Loading : HistoryResults
    data class Unavailable(val error: AppError) : HistoryResults
    data class Failed(val error: AppError) : HistoryResults
    data class Content(
        val items: List<VisitorHistoryItem>,
        val nextPage: Int?,
        val operation: HistoryOperation = HistoryOperation.Idle
    ) : HistoryResults
}

internal sealed interface HistoryOperation {
    data object Idle : HistoryOperation
    data object Refreshing : HistoryOperation
    data object LoadingNext : HistoryOperation
    data class RefreshFailed(val error: AppError) : HistoryOperation
    data class NextFailed(val page: Int, val error: AppError) : HistoryOperation
}

internal sealed interface HistoryAction {
    data class EditFrom(val date: LocalDate?) : HistoryAction
    data class EditTo(val date: LocalDate?) : HistoryAction
    data object Apply : HistoryAction
    data object Clear : HistoryAction
    data object Refresh : HistoryAction
    data object LoadNext : HistoryAction
    data object Retry : HistoryAction
}

internal val AppError.isHistoryRangeError: Boolean
    get() = this is AppError.Validation && message == "Rango de fechas inválido"
