package co.edu.unicauca.urbanguard.domain.usecase.news

import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(complexId: String): Result<List<News>> {
        return repository.getNews(complexId)
    }
}
