package co.edu.unicauca.urbanguard.ui.features.visitors.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.model.VisitorHistoryPage
import co.edu.unicauca.urbanguard.domain.repository.VisitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

@HiltViewModel
class VisitorHistoryViewModel @Inject constructor(
    private val repository: VisitorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val state = MutableStateFlow(VisitorHistoryUiState())
    internal val uiState = state.asStateFlow()
    private var context: VisitorHistoryContext? = null
    private var generation = 0L
    private var loadJob: Job? = null

    init {
        acceptSession(sessionManager.session.value)
        viewModelScope.launch {
            sessionManager.session.collect { session ->
                if (session?.historyContext() != context) {
                    acceptSession(session)
                } else {
                    state.value = state.value.copy(complexName = sessionManager.getActiveConjunto()?.nombre)
                }
            }
        }
    }

    internal fun onAction(action: HistoryAction) {
        val current = state.value
        if (current.results is HistoryResults.Unavailable ||
            context != sessionManager.session.value?.historyContext()
        ) {
            return
        }
        when (action) {
            is HistoryAction.EditFrom ->
                state.value = current.copy(draft = current.draft.copy(from = action.date), invalidRange = false)
            is HistoryAction.EditTo ->
                state.value = current.copy(draft = current.draft.copy(to = action.date), invalidRange = false)
            HistoryAction.Apply -> applyDates(current.draft)
            HistoryAction.Clear -> applyDates(HistoryDates(), clear = true)
            HistoryAction.Refresh -> {
                val content = current.results as? HistoryResults.Content ?: return
                if (content.operation != HistoryOperation.Refreshing) load(0, content)
            }
            HistoryAction.LoadNext -> {
                val content = current.results as? HistoryResults.Content ?: return
                val page = content.nextPage ?: return
                if (content.operation == HistoryOperation.Idle || content.operation is HistoryOperation.RefreshFailed) {
                    load(page, content)
                }
            }
            HistoryAction.Retry -> retry(current.results)
        }
    }

    private fun acceptSession(session: UserSession?) {
        invalidateRequest()
        val current = session?.historyContext()
        context = current
        val results = when {
            current == null -> HistoryResults.Unavailable(AppError.SessionExpired(AppError.SessionExpired.MESSAGE))
            !current.canAccess -> HistoryResults.Unavailable(AppError.Forbidden(AppError.Forbidden.MESSAGE))
            current.complexId == null -> HistoryResults.NeedsComplex
            else -> HistoryResults.Loading
        }
        state.value = VisitorHistoryUiState(
            complexName = sessionManager.getActiveConjunto()?.nombre,
            results = results,
            listRevision = generation
        )
        if (results == HistoryResults.Loading) load(0)
    }

    private fun applyDates(dates: HistoryDates, clear: Boolean = false) {
        val current = state.value
        if (dates.isInvalid) {
            state.value = current.copy(invalidRange = true)
            return
        }
        if (!clear && dates == current.applied) return
        if (clear && !current.draft.isFiltered && !current.applied.isFiltered) return
        state.value = current.copy(
            draft = dates,
            applied = dates,
            invalidRange = false,
            listRevision = generation + 1
        )
        load(0)
    }

    private fun retry(results: HistoryResults) {
        when (results) {
            is HistoryResults.Failed -> if (!results.error.isHistoryRangeError) load(0) else Unit
            is HistoryResults.Content -> when (val operation = results.operation) {
                is HistoryOperation.RefreshFailed -> load(0, results)
                is HistoryOperation.NextFailed ->
                    load(if (operation.error is AppError.Validation) 0 else operation.page, results)
                else -> Unit
            }
            else -> Unit
        }
    }

    private fun load(page: Int, previous: HistoryResults.Content? = null) {
        val current = context ?: return
        val complexId = current.complexId ?: return
        if (!current.canAccess || current != sessionManager.session.value?.historyContext()) return

        invalidateRequest()
        val requestGeneration = generation
        val dates = state.value.applied
        val operation = if (page == 0) HistoryOperation.Refreshing else HistoryOperation.LoadingNext
        state.value = state.value.copy(results = previous?.copy(operation = operation) ?: HistoryResults.Loading)
        loadJob = viewModelScope.launch {
            val result = repository.getVisitorHistory(complexId, dates.from, dates.to, page, PAGE_SIZE)
            if (requestGeneration != generation || current != sessionManager.session.value?.historyContext() ||
                dates != state.value.applied
            ) {
                return@launch
            }
            result.fold(
                onSuccess = { acceptPage(it, previous) },
                onFailure = { failPage(AppError.from(it), page, previous) }
            )
        }
    }

    private fun acceptPage(page: VisitorHistoryPage, previous: HistoryResults.Content?) {
        val existing = if (page.page > 0) previous?.items.orEmpty() else emptyList()
        val items = (existing + page.items).distinctBy { it.invitationId }
        val nextPage = (page.page + 1).takeIf { it < page.totalPages }
        state.value = state.value.copy(
            results = HistoryResults.Content(items, nextPage),
            listRevision = if (page.page == 0) generation else state.value.listRevision
        )
    }

    private fun failPage(error: AppError, page: Int, previous: HistoryResults.Content?) {
        val result = when {
            error is AppError.SessionExpired || error is AppError.ReauthRequired ||
                error is AppError.Forbidden || error is AppError.NotFound || error is AppError.AccountDisabled ->
                HistoryResults.Unavailable(error)
            previous == null || error.isHistoryRangeError -> HistoryResults.Failed(error)
            page == 0 -> previous.copy(operation = HistoryOperation.RefreshFailed(error))
            else -> previous.copy(operation = HistoryOperation.NextFailed(page, error))
        }
        state.value = state.value.copy(results = result)
    }

    private fun invalidateRequest() {
        generation++
        loadJob?.cancel()
    }
}
