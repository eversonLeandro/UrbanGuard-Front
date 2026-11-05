package co.edu.unicauca.urbanguard.ui.features.news

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.ui.components.EmptyState
import co.edu.unicauca.urbanguard.ui.components.ErrorView
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LIST_PADDING = 20.dp
private val CARD_PADDING = 16.dp
private val CARD_CORNER = 16.dp
private val ITEM_GAP = 12.dp

@Composable
fun NewsFeedScreen(
    onPublishClick: () -> Unit = {},
    onEditClick: (News) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: NewsFeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    LaunchedEffect(uiState.downloadError) {
        if (uiState.downloadError != null) {
            snackbarHostState.showSnackbar(uiState.downloadError.orEmpty())
        }
    }

    LaunchedEffect(uiState.deleteError) {
        if (uiState.deleteError != null) {
            snackbarHostState.showSnackbar(uiState.deleteError.orEmpty())
            viewModel.clearDeleteError()
        }
    }

    Scaffold(
        topBar = {
            UrbanGuardTopBar(
                title = NewsConstants.NEWS_SCREEN_TITLE,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (uiState.isAdmin) {
                FloatingActionButton(
                    onClick = onPublishClick,
                    containerColor = UgNavy
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = NewsConstants.PUBLISH_BUTTON_TEXT,
                        tint = Color.White
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = UgBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(UgBackground)
        ) {
            when {
                uiState.isLoading -> LoadingNewsState()
                uiState.errorMessage != null && uiState.newsList.isEmpty() -> ErrorNewsState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = viewModel::loadNews
                )
                uiState.newsList.isEmpty() -> EmptyNewsState()
                else -> NewsListContent(
                    uiState = uiState,
                    onAttachmentClick = viewModel::openAttachment,
                    onEditClick = onEditClick,
                    onDeleteClick = viewModel::requestDelete
                )
            }
        }
    }

    uiState.deleteTarget?.let { target ->
        DeleteNewsDialog(
            isDeleting = uiState.deletingId == target.announcementId,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun DeleteNewsDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(NewsConstants.DELETE_CONFIRM_TITLE) },
        text = {
            Text(
                text = NewsConstants.DELETE_CONFIRM_BODY,
                color = UgTextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(NewsConstants.DELETE_CONFIRM_ACTION, color = co.edu.unicauca.urbanguard.ui.theme.UgErrorText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(NewsConstants.CANCEL_ACTION)
            }
        },
        titleContentColor = UgTextPrimary,
        containerColor = Color.White
    )
}

@Composable
private fun LoadingNewsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = UgNavy)
    }
}

@Composable
private fun ErrorNewsState(message: String, onRetry: () -> Unit) {
    ErrorView(
        error = co.edu.unicauca.urbanguard.domain.model.AppError.Validation(message),
        onRetry = onRetry
    )
}

@Composable
private fun EmptyNewsState() {
    EmptyState(
        title = NewsConstants.MSG_EMPTY_TITLE,
        description = NewsConstants.MSG_EMPTY_SUBTITLE,
        icon = Icons.Default.Add
    )
}

@Composable
private fun NewsListContent(
    uiState: NewsFeedUiState,
    onAttachmentClick: (News) -> Unit,
    onEditClick: (News) -> Unit,
    onDeleteClick: (News) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = co.edu.unicauca.urbanguard.ui.theme.UgErrorText,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = LIST_PADDING, vertical = 8.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = LIST_PADDING, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_GAP)
        ) {
            items(uiState.newsList) { news ->
                NewsCard(
                    news = news,
                    isDownloading = uiState.downloadingId == news.announcementId,
                    isAdmin = uiState.isAdmin,
                    isDeleting = uiState.deletingId == news.announcementId,
                    onAttachmentClick = { onAttachmentClick(news) },
                    onEditClick = { onEditClick(news) },
                    onDeleteClick = { onDeleteClick(news) }
                )
            }
        }
    }
}

@Composable
private fun NewsCard(
    news: News,
    isDownloading: Boolean,
    isAdmin: Boolean,
    isDeleting: Boolean,
    onAttachmentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val localDateTime = news.publishedAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(CARD_CORNER))
            .padding(CARD_PADDING)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = news.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = UgTextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (isAdmin) {
                IconButton(onClick = onEditClick, enabled = !isDeleting) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = NewsConstants.EDIT_BUTTON_TEXT,
                        tint = UgNavy
                    )
                }
                IconButton(onClick = onDeleteClick, enabled = !isDeleting) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = NewsConstants.DELETE_BUTTON_TEXT,
                            tint = co.edu.unicauca.urbanguard.ui.theme.UgErrorText
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = localDateTime.format(formatter),
            fontSize = 12.sp,
            color = UgTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = news.body,
            fontSize = 14.sp,
            color = UgTextPrimary
        )
        if (news.attachment != null) {
            Spacer(modifier = Modifier.height(12.dp))
            AttachmentChip(
                news = news,
                isDownloading = isDownloading,
                onClick = onAttachmentClick
            )
        }
    }
}

@Composable
private fun AttachmentChip(
    news: News,
    isDownloading: Boolean,
    onClick: () -> Unit
) {
    val attachment = news.attachment ?: return
    val imageUrl = attachment.imageUrl
    if (!imageUrl.isNullOrBlank()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = attachment.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UgBackground)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = attachment.fileName ?: "Adjunto",
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }
        return
    }
    val icon = if (attachment.contentType == "application/pdf") Icons.Default.Description else Icons.Default.Image
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(UgBackground, RoundedCornerShape(12.dp))
            .clickable(enabled = !isDownloading) { onClick() }
            .padding(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = UgNavy)
        Spacer(modifier = Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.fileName ?: "Adjunto",
                fontSize = 13.sp,
                color = UgTextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatSize(attachment.sizeBytes),
                fontSize = 12.sp,
                color = UgTextSecondary
            )
        }
        if (isDownloading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.AttachFile, contentDescription = null, tint = UgTextSecondary)
        }
    }
}

private fun formatSize(sizeBytes: Long?): String {
    if (sizeBytes == null) return ""
    val kb = sizeBytes / 1024L
    val mb = kb / 1024L
    return if (mb >= 1) "$mb MB" else "$kb KB"
}
