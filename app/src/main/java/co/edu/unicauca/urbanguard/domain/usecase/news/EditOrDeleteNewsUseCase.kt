package co.edu.unicauca.urbanguard.domain.usecase.news

import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import javax.inject.Inject
import okhttp3.RequestBody

class EditOrDeleteNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {

    suspend fun update(
        complexId: String,
        announcementId: String,
        title: String,
        body: String,
        attachment: RequestBody? = null,
        attachmentName: String? = null,
        removeAttachment: Boolean = false
    ): Result<News> {
        val validationError = NewsInputValidator.validate(title, body)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        if (complexId.isBlank()) {
            return Result.failure(IllegalArgumentException(NewsConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (announcementId.isBlank()) {
            return Result.failure(IllegalArgumentException(NewsConstants.MSG_ANNOUNCEMENT_REQUIRED))
        }
        return repository.updateNews(
            complexId = complexId,
            announcementId = announcementId,
            title = title.trim(),
            body = body.trim(),
            attachment = attachment,
            attachmentName = attachmentName,
            removeAttachment = removeAttachment
        )
    }

    suspend fun delete(complexId: String, announcementId: String): Result<Unit> {
        if (complexId.isBlank()) {
            return Result.failure(IllegalArgumentException(NewsConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        if (announcementId.isBlank()) {
            return Result.failure(IllegalArgumentException(NewsConstants.MSG_ANNOUNCEMENT_REQUIRED))
        }
        return repository.deleteNews(complexId, announcementId)
    }
}
