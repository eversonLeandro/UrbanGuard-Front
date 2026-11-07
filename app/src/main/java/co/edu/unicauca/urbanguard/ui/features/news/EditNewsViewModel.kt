package co.edu.unicauca.urbanguard.ui.features.news

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import co.edu.unicauca.urbanguard.domain.usecase.news.EditOrDeleteNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class EditNewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val editOrDeleteNewsUseCase: EditOrDeleteNewsUseCase,
    private val newsRepository: NewsRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val announcementId: String = savedStateHandle.get<String>("announcementId").orEmpty()

    private val _uiState = MutableStateFlow(EditNewsUiState())
    val uiState: StateFlow<EditNewsUiState> = _uiState.asStateFlow()

    private val allowedTypes = NewsAttachmentPreparer.allowedTypes

    fun load() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = NewsConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return
        }
        if (announcementId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = NewsConstants.MSG_ANNOUNCEMENT_REQUIRED
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            newsRepository.getNews(complexId).fold(
                onSuccess = { newsList ->
                    val news = newsList.firstOrNull { it.announcementId == announcementId }
                    _uiState.value = if (news == null) {
                        EditNewsUiState(
                            isLoading = false,
                            errorMessage = NewsConstants.MSG_LOAD_ERROR
                        )
                    } else {
                        EditNewsUiState(
                            title = news.title,
                            body = news.body,
                            isLoading = false,
                            existingAttachmentName = news.attachment?.fileName,
                            existingAttachmentMime = news.attachment?.contentType,
                            existingAttachmentSize = news.attachment?.sizeBytes
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = EditNewsUiState(
                        isLoading = false,
                        errorMessage = error.message ?: NewsConstants.MSG_LOAD_ERROR
                    )
                }
            )
        }
    }

    fun onTitleChange(title: String) {
        val value = title.take(NewsConstants.TITLE_MAX_LENGTH)
        _uiState.value = _uiState.value.copy(title = value, errorMessage = null)
    }

    fun onBodyChange(body: String) {
        val value = body.take(NewsConstants.BODY_MAX_LENGTH)
        _uiState.value = _uiState.value.copy(body = value, errorMessage = null)
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
        val cleaned = name?.let(NewsAttachmentPreparer::sanitize)
        if (cleaned.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(attachmentError = NewsConstants.ATTACH_INVALID_NAME)
            return
        }
        _uiState.value = _uiState.value.copy(
            attachmentUri = uri,
            attachmentName = cleaned,
            attachmentSize = size,
            attachmentMime = mime,
            attachmentError = null,
            removeAttachment = false
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

    fun onRemoveAttachmentChange(remove: Boolean) {
        _uiState.value = _uiState.value.copy(removeAttachment = remove)
    }

    fun save() {
        val state = _uiState.value
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        val validationError = when {
            complexId.isNullOrBlank() -> NewsConstants.MSG_NO_ACTIVE_COMPLEX
            announcementId.isBlank() -> NewsConstants.MSG_ANNOUNCEMENT_REQUIRED
            state.title.trim().isEmpty() -> NewsConstants.MSG_TITLE_REQUIRED
            state.body.trim().isEmpty() -> NewsConstants.MSG_BODY_REQUIRED
            state.attachmentError != null -> state.attachmentError
            else -> null
        }
        if (validationError != null) {
            _uiState.value = state.copy(errorMessage = validationError)
            return
        }
        if (state.isSaving) return
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val payload = prepareAttachment(state) ?: return@launch
            editOrDeleteNewsUseCase.update(
                complexId = complexId.orEmpty(),
                announcementId = announcementId,
                title = state.title,
                body = state.body,
                attachment = payload.body,
                attachmentName = payload.fileName,
                removeAttachment = state.removeAttachment && state.attachmentUri == null
            ).fold(
                onSuccess = {
                    _uiState.value = EditNewsUiState(isLoading = false, isSaved = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: NewsConstants.MSG_UPDATE_ERROR
                    )
                }
            )
        }
    }

    fun resetSaved() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    private suspend fun prepareAttachment(state: EditNewsUiState): AttachmentPayload? {
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
        _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = message)
        return null
    }

    private data class AttachmentPayload(
        val body: RequestBody?,
        val fileName: String?
    )
}
