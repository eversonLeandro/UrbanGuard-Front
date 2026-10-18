package co.edu.unicauca.urbanguard.ui.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.DateTimeUtils
import co.edu.unicauca.urbanguard.core.common.NotificationConstants
import co.edu.unicauca.urbanguard.domain.model.NotificationItem
import co.edu.unicauca.urbanguard.ui.components.ErrorBanner
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgDisabledText
import co.edu.unicauca.urbanguard.ui.theme.UgEmerald
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgInputBg
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgSurface
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

private val PADDING = 20.dp
private val GAP = 12.dp
private val CORNER = 16.dp

/**
 * Bandeja de notificaciones in-app. Lista los avisos del usuario (vinculación,
 * visitantes, recorridos), permite filtrar por no leídas, marcar leídas y
 * abrir el deepLink asociado a cada una.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInboxScreen(
    onNavigateBack: () -> Unit,
    onOpenDeepLink: (String) -> Unit,
    viewModel: NotificationInboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationInboxEvent.OpenDeepLink -> onOpenDeepLink(event.deepLink)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = "Notificaciones",
            onNavigateBack = onNavigateBack
        )

        NotificationInboxFilters(
            unreadOnly = uiState.unreadOnly,
            hasUnread = uiState.hasUnread,
            onToggleUnreadOnly = viewModel::onToggleUnreadOnly,
            onMarkAllRead = viewModel::onMarkAllRead
        )

        uiState.actionError?.let { message ->
            Box(modifier = Modifier.padding(horizontal = PADDING, vertical = 4.dp)) {
                ErrorBanner(message = message, onRetry = viewModel::onActionErrorShown)
            }
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(uiState.errorMessage!!, viewModel::onRefresh)
                uiState.isEmpty -> EmptyState()
                else -> NotificationList(
                    items = uiState.items,
                    hasMore = uiState.hasMore,
                    isLoadingMore = uiState.isLoadingMore,
                    onNotificationClick = viewModel::onNotificationClick,
                    onLoadMore = viewModel::onLoadMore
                )
            }
        }
    }
}

@Composable
private fun NotificationInboxFilters(
    unreadOnly: Boolean,
    hasUnread: Boolean,
    onToggleUnreadOnly: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PADDING, vertical = GAP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = !unreadOnly,
            onClick = { if (unreadOnly) onToggleUnreadOnly() },
            label = { Text("Todas") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = UgNavy,
                selectedLabelColor = UgSurface
            )
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = unreadOnly,
            onClick = { if (!unreadOnly) onToggleUnreadOnly() },
            label = { Text("No leídas") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = UgNavy,
                selectedLabelColor = UgSurface
            )
        )

        Spacer(Modifier.weight(1f))

        if (hasUnread) {
            TextButton(onClick = onMarkAllRead) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    tint = UgEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Marcar todas", color = UgEmerald, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun NotificationList(
    items: List<NotificationItem>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onNotificationClick: (NotificationItem) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = PADDING, end = PADDING, bottom = PADDING),
        verticalArrangement = Arrangement.spacedBy(GAP)
    ) {
        items(items, key = { it.id }) { notification ->
            NotificationCard(
                notification = notification,
                onClick = { onNotificationClick(notification) }
            )
        }

        if (hasMore) {
            item {
                LaunchedEffect(items.size) { onLoadMore() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = GAP),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(
                            color = UgEmerald,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CORNER))
            .background(UgSurface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(9.dp)
                .clip(CircleShape)
                .background(if (notification.isRead) UgInputBg else UgEmerald)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                fontSize = 15.sp,
                fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                color = if (notification.isRead) UgTextSecondary else UgTextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = notification.body,
                fontSize = 13.sp,
                color = UgTextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = DateTimeUtils.formatFechaSolicitud(notification.createdAt),
                fontSize = 11.sp,
                color = UgDisabledText
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = UgEmerald)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(UgInputBg, RoundedCornerShape(CORNER)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = UgTextSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(GAP))
        Text(
            text = NotificationConstants.LIST_EMPTY_TITLE,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = UgTextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = NotificationConstants.LIST_EMPTY_SUBTITLE,
            fontSize = 13.sp,
            color = UgTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = UgErrorText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Reintentar", color = UgNavy, fontWeight = FontWeight.Medium)
        }
    }
}
