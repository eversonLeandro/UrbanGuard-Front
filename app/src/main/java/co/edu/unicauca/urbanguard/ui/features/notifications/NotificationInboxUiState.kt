package co.edu.unicauca.urbanguard.ui.features.notifications

import co.edu.unicauca.urbanguard.domain.model.NotificationItem

data class NotificationInboxUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val items: List<NotificationItem> = emptyList(),
    val unreadOnly: Boolean = false,
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
    val actionError: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && items.isEmpty()

    val isBusy: Boolean
        get() = isLoading || isRefreshing || isLoadingMore

    val hasUnread: Boolean
        get() = items.any { !it.isRead }
}

/** Evento de un solo uso: abrir el deepLink de un aviso. */
sealed interface NotificationInboxEvent {
    data class OpenDeepLink(val deepLink: String) : NotificationInboxEvent
}
