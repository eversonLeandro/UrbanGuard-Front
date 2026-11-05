package co.edu.unicauca.urbanguard.ui.features.news

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.usecase.news.EditOrDeleteNewsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.news.GetNewsUseCase
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "NewsFeedViewModel"

@Suppress("LongParameterList")
@HiltViewModel
class NewsFeedViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val editOrDeleteNewsUseCase: EditOrDeleteNewsUseCase,
    private val newsRepository: NewsRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsFeedUiState())
    val uiState: StateFlow<NewsFeedUiState> = _uiState.asStateFlow()

    init {
        loadNews()
    }

    fun loadNews() {
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        val role = sessionManager.currentRole()
        val isAdmin = role == UserRole.ADMIN
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = NewsConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            isLoading = _uiState.value.newsList.isEmpty(),
            isRefreshing = _uiState.value.newsList.isNotEmpty(),
            errorMessage = null,
            isAdmin = isAdmin,
            downloadError = null
        )
        viewModelScope.launch {
            getNewsUseCase(complexId).fold(
                onSuccess = { news ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        newsList = news,
                        errorMessage = null,
                        isAdmin = isAdmin
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: NewsConstants.MSG_LOAD_ERROR,
                        isAdmin = isAdmin
                    )
                }
            )
        }
    }

    fun refresh() {
        loadNews()
    }

    fun requestDelete(news: News) {
        _uiState.value = _uiState.value.copy(deleteTarget = news, deleteError = null)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(deleteTarget = null)
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId
        if (complexId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                deleteTarget = null,
                deleteError = NewsConstants.MSG_NO_ACTIVE_COMPLEX
            )
            return
        }
        if (_uiState.value.deletingId != null) return
        _uiState.value = _uiState.value.copy(
            deletingId = target.announcementId,
            deleteError = null
        )
        viewModelScope.launch {
            editOrDeleteNewsUseCase.delete(complexId, target.announcementId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        newsList = _uiState.value.newsList.filterNot {
                            it.announcementId == target.announcementId
                        },
                        deleteTarget = null,
                        deletingId = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        deleteTarget = null,
                        deletingId = null,
                        deleteError = error.message ?: NewsConstants.MSG_DELETE_ERROR
                    )
                }
            )
        }
    }

    fun clearDeleteError() {
        if (_uiState.value.deleteError != null) {
            _uiState.value = _uiState.value.copy(deleteError = null)
        }
    }

    fun openAttachment(news: News) {
        if (news.attachment == null) return
        val complexId = sessionManager.getActiveConjunto()?.conjuntoId ?: return
        _uiState.value = _uiState.value.copy(downloadingId = news.announcementId, downloadError = null)
        viewModelScope.launch {
            newsRepository.downloadAttachment(complexId, news.announcementId).fold(
                onSuccess = { file ->
                    val opened = openFile(file, news)
                    _uiState.value = _uiState.value.copy(
                        downloadingId = null,
                        downloadError = if (opened) null else NewsConstants.ATTACH_OPEN_ERROR
                    )
                },
                onFailure = { error ->
                    val message = error.message ?: NewsConstants.ATTACH_DOWNLOAD_ERROR
                    _uiState.value = _uiState.value.copy(
                        downloadingId = null,
                        downloadError = message
                    )
                }
            )
        }
    }

    private fun openFile(file: File, news: News): Boolean {
        return try {
            val extension = extensionFor(news.attachment?.contentType)
            val targetName = "${news.announcementId}.$extension"
            val dir = File(context.cacheDir, NewsConstants.ATTACHMENT_CACHE_DIR)
            if (!dir.exists()) dir.mkdirs()
            val target = File(dir, targetName)
            if (file.name != target.name || !target.exists()) {
                file.copyTo(target, overwrite = true)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", target)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, news.attachment?.contentType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No hay app para abrir el adjunto ${news.announcementId}", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "URI inválida para el adjunto ${news.announcementId}", e)
            false
        }
    }

    private fun extensionFor(contentType: String?): String {
        return when (contentType) {
            "application/pdf" -> "pdf"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            else -> "bin"
        }
    }
}
