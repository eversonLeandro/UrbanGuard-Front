package co.edu.unicauca.urbanguard.domain.usecase.news

import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import javax.inject.Inject
import okhttp3.RequestBody

class PublishNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(
        complexId: String,
        title: String,
        body: String,
        attachment: RequestBody? = null,
        attachmentName: String? = null
    ): Result<News> {
        val validationError = validate(title, body)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        if (complexId.isBlank()) {
            return Result.failure(IllegalArgumentException(NewsConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return repository.publishNews(complexId, title.trim(), body.trim(), attachment, attachmentName)
    }

    private fun validate(title: String, body: String): String? {
        val trimmedTitle = title.trim()
        val trimmedBody = body.trim()
        if (trimmedTitle.isEmpty()) return NewsConstants.MSG_TITLE_REQUIRED
        if (trimmedTitle.length > NewsConstants.TITLE_MAX_LENGTH) {
            return NewsConstants.MSG_TITLE_TOO_LONG
        }
        if (trimmedBody.isEmpty()) return NewsConstants.MSG_BODY_REQUIRED
        if (trimmedBody.length > NewsConstants.BODY_MAX_LENGTH) {
            return NewsConstants.MSG_BODY_TOO_LONG
        }
        if (containsControlCharacters(trimmedTitle) || containsControlCharacters(trimmedBody)) {
            return NewsConstants.MSG_INVALID_CHARS
        }
        return null
    }

    private fun containsControlCharacters(text: String): Boolean {
        return text.any { char ->
            Character.isISOControl(char)
        }
    }
}
