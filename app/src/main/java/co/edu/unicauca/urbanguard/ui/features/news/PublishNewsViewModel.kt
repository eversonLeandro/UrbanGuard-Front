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
        val size = getSize(uri)
        if (size != null && size > NewsConstants.ATTACHMENT_MAX_BYTES) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_TOO_LARGE)
            return
        }
        val name = getName(uri)
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
        if (complexId.isNullOrBlank()) {
            _uiState.value = state.copy(errorMessage = NewsConstants.MSG_NO_ACTIVE_COMPLEX)
            return
        }
        if (state.title.trim().isEmpty()) {
            _uiState.value = state.copy(errorMessage = NewsConstants.MSG_TITLE_REQUIRED)
            return
        }
        if (state.body.trim().isEmpty()) {
            _uiState.value = state.copy(errorMessage = NewsConstants.MSG_BODY_REQUIRED)
            return
        }
        if (state.attachmentError != null) {
            _uiState.value = state.copy(errorMessage = state.attachmentError)
            return
        }
        if (state.isLoading) return
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val (requestBody, finalAttachmentName) = try {
                val size = state.attachmentSize ?: state.attachmentUri?.let { getSize(it) }
                if (size != null && size > NewsConstants.ATTACHMENT_MAX_BYTES) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = NewsConstants.ATTACH_TOO_LARGE
                    )
                    return@launch
                }
                if (state.attachmentUri == null) {
                    Pair(null, null)
                } else {
                    val uri = state.attachmentUri
                    val mime = state.attachmentMime ?: "application/octet-stream"
                    if (mime == "application/pdf") {
                        val bytes = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                        if (bytes == null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = NewsConstants.ATTACH_OPEN_ERROR
                            )
                            return@launch
                        }
                        if (bytes.size.toLong() > NewsConstants.ATTACHMENT_MAX_BYTES) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = NewsConstants.ATTACH_TOO_LARGE
                            )
                            return@launch
                        }
                        Pair(bytes.toRequestBody(mime.toMediaTypeOrNull()), state.attachmentName)
                    } else {
                        val prepared = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                AnnouncementImageUpload.cacheAndAsRequestBody(
                                    context.cacheDir,
                                    stream,
                                    state.attachmentName ?: "imagen"
                                )
                            }
                        }
                        if (prepared == null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = NewsConstants.ATTACH_OPEN_ERROR
                            )
                            return@launch
                        }
                        if (prepared.body.contentLength() > NewsConstants.ATTACHMENT_MAX_BYTES) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = NewsConstants.ATTACH_TOO_LARGE
                            )
                            return@launch
                        }
                        Pair(prepared.body, prepared.fileName)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = NewsConstants.ATTACH_OPEN_ERROR
                )
                return@launch
            }
            publishNewsUseCase(complexId, state.title, state.body, requestBody, finalAttachmentName).fold(
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

    fun resetPublished() {
        _uiState.value = PublishNewsUiState()
    }

    private fun getSize(uri: Uri): Long? {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && !it.isNull(idx)) {
                    return it.getLong(idx)
                }
            }
        }
        return null
    }

    private fun getName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return it.getString(idx)
            }
        }
        return uri.lastPathSegment
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
