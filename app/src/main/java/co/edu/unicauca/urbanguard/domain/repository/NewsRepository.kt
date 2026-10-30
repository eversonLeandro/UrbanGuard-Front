package co.edu.unicauca.urbanguard.domain.repository

import co.edu.unicauca.urbanguard.domain.model.News
import okhttp3.RequestBody

interface NewsRepository {
    suspend fun getNews(complexId: String): Result<List<News>>
    suspend fun publishNews(
        complexId: String,
        title: String,
        body: String,
        attachment: RequestBody? = null,
        attachmentName: String? = null
    ): Result<News>
    suspend fun downloadAttachment(complexId: String, announcementId: String): Result<java.io.File>
}
