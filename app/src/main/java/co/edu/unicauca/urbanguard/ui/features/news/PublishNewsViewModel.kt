package co.edu.unicauca.urbanguard.ui.features.news

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.data.remote.api.AnnouncementImageUpload
import co.edu.unicauca.urbanguard.domain.usecase.news.PublishNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PublishNewsViewModel @Inject constructor(
    private val publishNewsUseCase: PublishNewsUseCase,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishNewsUiState())
    val uiState: StateFlow<PublishNewsUiState> = _uiState.asStateFlow()

    private val allowedTypes = setOf("application/pdf", "image/jpeg", "image/png")

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
        val size = queryColumn(uri, OpenableColumns.SIZE)?.toLongOrNull()
        if (size != null && size > NewsConstants.ATTACHMENT_MAX_BYTES) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_TOO_LARGE)
            return
        }
        val name = queryColumn(uri, OpenableColumns.DISPLAY_NAME) ?: uri.lastPathSegment
        if (name.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_INVALID_NAME)
            return
        }
        val cleaned = sanitize(name)
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

    @Suppress("SwallowedException", "CyclomaticComplexMethod")
    private suspend fun prepareAttachment(state: PublishNewsUiState): AttachmentPayload? {
        val size = state.attachmentSize
            ?: state.attachmentUri?.let { queryColumn(it, OpenableColumns.SIZE)?.toLongOrNull() }
        if (size != null && size > NewsConstants.ATTACHMENT_MAX_BYTES) {
            return failAttachment(NewsConstants.ATTACH_TOO_LARGE)
        }
        val uri = state.attachmentUri ?: return AttachmentPayload(null, null)
        val mime = state.attachmentMime ?: "application/octet-stream"
        return try {
            if (mime == "application/pdf") {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: return failAttachment(NewsConstants.ATTACH_OPEN_ERROR)
                if (bytes.size.toLong() > NewsConstants.ATTACHMENT_MAX_BYTES) {
                    return failAttachment(NewsConstants.ATTACH_TOO_LARGE)
                }
                AttachmentPayload(bytes.toRequestBody(mime.toMediaTypeOrNull()), state.attachmentName)
            } else {
                val prepared = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        AnnouncementImageUpload.cacheAndAsRequestBody(
                            context.cacheDir,
                            stream,
                            state.attachmentName ?: "imagen"
                        )
                    }
                } ?: return failAttachment(NewsConstants.ATTACH_OPEN_ERROR)
                if (prepared.body.contentLength() > NewsConstants.ATTACHMENT_MAX_BYTES) {
                    return failAttachment(NewsConstants.ATTACH_TOO_LARGE)
                }
                AttachmentPayload(prepared.body, prepared.fileName)
            }
        } catch (e: IOException) {
            failAttachment(NewsConstants.ATTACH_OPEN_ERROR)
        } catch (e: SecurityException) {
            failAttachment(NewsConstants.ATTACH_OPEN_ERROR)
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

    private fun queryColumn(uri: Uri, column: String): String? {
        val cursor = context.contentResolver.query(uri, arrayOf(column), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(column)
                if (idx >= 0 && !it.isNull(idx)) {
                    return it.getString(idx)
                }
            }
        }
        return null
    }

    private fun sanitize(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return if (cleaned.length > NewsConstants.ATTACHMENT_NAME_MAX_LENGTH) {
            cleaned.take(NewsConstants.ATTACHMENT_NAME_MAX_LENGTH)
        } else {
            cleaned
        }
    }
}
