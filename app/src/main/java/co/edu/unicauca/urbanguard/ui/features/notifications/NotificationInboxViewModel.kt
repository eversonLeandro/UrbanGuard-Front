package co.edu.unicauca.urbanguard.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.NotificationConstants
import co.edu.unicauca.urbanguard.domain.model.NotificationItem
import co.edu.unicauca.urbanguard.domain.usecase.notifications.GetNotificationsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.notifications.MarkAllNotificationsReadUseCase
import co.edu.unicauca.urbanguard.domain.usecase.notifications.MarkNotificationReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * Bandeja de notificaciones in-app: lista paginada, filtro de no leídas,
 * marcar leída y marcar todas. Al marcar, actualiza el contador compartido de
 * la insignia y emite la navegación por deepLink del aviso.
 */
@HiltViewModel
class NotificationInboxViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase,
    private val badgeStore: NotificationBadgeStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationInboxUiState())
    val uiState: StateFlow<NotificationInboxUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<NotificationInboxEvent>(Channel.BUFFERED)
    val events: Flow<NotificationInboxEvent> = eventChannel.receiveAsFlow()

    private var currentPage = 0
    private var loadJob: Job? = null

    init {
        onRefresh()
    }

    fun onRefresh() {
        loadJob?.cancel()
        val firstLoad = _uiState.value.items.isEmpty()
        _uiState.value = _uiState.value.copy(
            isLoading = firstLoad,
            isRefreshing = !firstLoad,
            errorMessage = null,
            actionError = null
        )
        loadJob = viewModelScope.launch {
            getNotificationsUseCase(0, NotificationConstants.PAGE_SIZE, _uiState.value.unreadOnly).fold(
                onSuccess = { page ->
                    currentPage = page.page
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = page.items,
                        hasMore = page.hasMore,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: NotificationConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            val nextPage = currentPage + 1
            getNotificationsUseCase(nextPage, NotificationConstants.PAGE_SIZE, _uiState.value.unreadOnly).fold(
                onSuccess = { page ->
                    currentPage = page.page
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        items = _uiState.value.items + page.items,
                        hasMore = page.hasMore
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        actionError = error.message ?: NotificationConstants.MSG_LIST_ERROR
                    )
                }
            )
        }
    }

    fun onToggleUnreadOnly() {
        _uiState.value = _uiState.value.copy(unreadOnly = !_uiState.value.unreadOnly, items = emptyList())
        onRefresh()
    }

    /**
     * Marca el aviso como leído (optimista) y navega a su deepLink si lo tiene.
     * Si el marcado falla, revierte el estado local y avisa con un banner.
     */
    fun onNotificationClick(item: NotificationItem) {
        if (!item.isRead) {
            setReadLocally(item.id, readAt = Instant.now().toString())
            viewModelScope.launch {
                markNotificationReadUseCase(item.id).fold(
                    onSuccess = { badgeStore.decrement() },
                    onFailure = { error ->
                        setReadLocally(item.id, readAt = null)
                        _uiState.value = _uiState.value.copy(
                            actionError = error.message ?: NotificationConstants.MSG_MARK_READ_ERROR
                        )
                    }
                )
            }
        }
        item.deepLink?.takeIf { it.isNotBlank() }?.let { deepLink ->
            viewModelScope.launch { eventChannel.send(NotificationInboxEvent.OpenDeepLink(deepLink)) }
        }
    }

    fun onMarkAllRead() {
        if (!_uiState.value.hasUnread) return
        viewModelScope.launch {
            markAllNotificationsReadUseCase().fold(
                onSuccess = {
                    val now = Instant.now().toString()
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.map { it.copy(readAt = it.readAt ?: now) },
                        actionError = null
                    )
                    badgeStore.set(0)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        actionError = error.message ?: NotificationConstants.MSG_MARK_READ_ERROR
                    )
                }
            )
        }
    }

    fun onActionErrorShown() {
        _uiState.value = _uiState.value.copy(actionError = null)
    }

    private fun setReadLocally(id: String, readAt: String?) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == id) it.copy(readAt = readAt) else it
            }
        )
    }
}
