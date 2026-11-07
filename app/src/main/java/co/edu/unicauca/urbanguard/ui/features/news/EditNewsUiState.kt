package co.edu.unicauca.urbanguard.ui.features.news

import android.net.Uri

data class EditNewsUiState(
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val existingAttachmentName: String? = null,
    val existingAttachmentMime: String? = null,
    val existingAttachmentSize: Long? = null,
    val removeAttachment: Boolean = false,
    val attachmentUri: Uri? = null,
    val attachmentName: String? = null,
    val attachmentSize: Long? = null,
    val attachmentMime: String? = null,
    val attachmentError: String? = null
)
