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
        val validationError = NewsInputValidator.validate(title, body)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        if (complexId.isBlank()) {
            return Result.failure(IllegalArgumentException(NewsConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return repository.publishNews(complexId, title.trim(), body.trim(), attachment, attachmentName)
    }
}
