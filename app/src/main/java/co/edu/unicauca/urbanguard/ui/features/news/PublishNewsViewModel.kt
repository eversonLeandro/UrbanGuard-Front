package co.edu.unicauca.urbanguard.ui.features.news

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.usecase.news.PublishNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class PublishNewsViewModel @Inject constructor(
    private val publishNewsUseCase: PublishNewsUseCase,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishNewsUiState())
    val uiState: StateFlow<PublishNewsUiState> = _uiState.asStateFlow()

    private val allowedTypes = NewsAttachmentPreparer.allowedTypes

    fun onTitleChange(title: String) {
        if (title.length <= NewsConstants.TITLE_MAX_LENGTH) {
            _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
        } else {
            val trimmed = title.take(NewsConstants.TITLE_MAX_LENGTH)
            _uiState.value = _uiState.value.copy(title = trimmed, errorMessage = null)
        }
    }

    fun onBodyChange(body: String) {
        if (body.length <= NewsConstants.BODY_MAX_LENGTH) {
            _uiState.value = _uiState.value.copy(body = body, errorMessage = null)
        } else {
            val trimmedBody = body.take(NewsConstants.BODY_MAX_LENGTH)
            _uiState.value = _uiState.value.copy(body = trimmedBody, errorMessage = null)
        }
    }

    fun onAttachmentSelected(uri: Uri?) {
        if (uri == null) {
            clearAttachment()
            return
        }
        val mime = context.contentResolver.getType(uri)
        if (mime !in allowedTypes) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_INVALID_TYPE)
            return
        }
        val size = NewsAttachmentPreparer.queryColumn(context, uri, OpenableColumns.SIZE)?.toLongOrNull()
        if (size != null && size > NewsConstants.ATTACHMENT_MAX_BYTES) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_TOO_LARGE)
            return
        }
        val name = NewsAttachmentPreparer.queryColumn(context, uri, OpenableColumns.DISPLAY_NAME)
            ?: uri.lastPathSegment
        if (name.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_INVALID_NAME)
            return
        }
        val cleaned = NewsAttachmentPreparer.sanitize(name)
        if (cleaned.isBlank()) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_INVALID_NAME)
            return
        }
        _uiState.value = _uiState.value.copy(
            attachmentUri = uri,
            attachmentName = cleaned,
            attachmentSize = size,
            attachmentMime = mime,
            attachmentError = null
        )
    }

    fun clearAttachment() {
        _uiState.value = _uiState.value.copy(
            attachmentUri = null,
            attachmentName = null,
            attachmentSize = null,
            attachmentMime = null,
            attachmentError = null
        )
    }

    fun publish() {
        val state = _uiState.value
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        val validationError = when {
            complexId.isNullOrBlank() -> NewsConstants.MSG_NO_ACTIVE_COMPLEX
            state.title.trim().isEmpty() -> NewsConstants.MSG_TITLE_REQUIRED
            state.body.trim().isEmpty() -> NewsConstants.MSG_BODY_REQUIRED
            state.attachmentError != null -> state.attachmentError
            else -> null
        }
        if (validationError != null) {
            _uiState.value = state.copy(errorMessage = validationError)
            return
        }
        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val payload = prepareAttachment(state) ?: return@launch
            publishNewsUseCase(complexId.orEmpty(), state.title, state.body, payload.body, payload.fileName).fold(
                onSuccess = {
                    _uiState.value = PublishNewsUiState(isPublished = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: NewsConstants.MSG_PUBLISH_ERROR
                    )
                }
            )
        }
    }

    private suspend fun prepareAttachment(state: PublishNewsUiState): AttachmentPayload? {
        return when (val result = NewsAttachmentPreparer.prepare(
            context = context,
            uri = state.attachmentUri,
            mime = state.attachmentMime,
            name = state.attachmentName,
            sizeHint = state.attachmentSize
        )) {
            is NewsAttachmentPreparer.Result.Ready -> AttachmentPayload(result.body, result.fileName)
            is NewsAttachmentPreparer.Result.Failed -> failAttachment(result.message)
        }
    }

    private fun failAttachment(message: String): AttachmentPayload? {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
        return null
    }

    private data class AttachmentPayload(
        val body: RequestBody?,
        val fileName: String?
    )

    fun resetPublished() {
        _uiState.value = PublishNewsUiState()
    }
}
