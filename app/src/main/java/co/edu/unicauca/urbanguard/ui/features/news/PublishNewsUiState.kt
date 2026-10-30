package co.edu.unicauca.urbanguard.ui.features.news

import android.net.Uri

data class PublishNewsUiState(
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPublished: Boolean = false,
    val attachmentUri: Uri? = null,
    val attachmentName: String? = null,
    val attachmentSize: Long? = null,
    val attachmentMime: String? = null,
    val attachmentError: String? = null
)
