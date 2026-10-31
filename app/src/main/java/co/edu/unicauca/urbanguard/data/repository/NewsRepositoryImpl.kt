package co.edu.unicauca.urbanguard.data.repository

import android.content.Context
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.mapper.createMetadataRequestBody
import co.edu.unicauca.urbanguard.data.mapper.toDomain
import co.edu.unicauca.urbanguard.data.remote.api.service.NewsApi
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    @ApplicationContext private val context: Context
) : NewsRepository {

    override suspend fun getNews(complexId: String): Result<List<News>> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(NewsConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val response = api.getAnnouncements(complexId)
            Result.success(response.map { it.toDomain().withImageAccess(complexId) })
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: DateTimeParseException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override suspend fun publishNews(
        complexId: String,
        title: String,
        body: String,
        attachment: RequestBody?,
        attachmentName: String?
    ): Result<News> {
        if (complexId.isBlank()) {
            return Result.failure(Exception(NewsConstants.MSG_NO_ACTIVE_COMPLEX))
        }
        return try {
            val metadata = createMetadataRequestBody(title = title, body = body)
            val part = if (attachment != null && !attachmentName.isNullOrBlank()) {
                val cleaned = sanitizeFileName(attachmentName)
                MultipartBody.Part.createFormData("attachment", cleaned, attachment)
            } else {
                null
            }
            val response = api.publishAnnouncement(complexId, metadata, part)
            Result.success(response.toDomain())
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: DateTimeParseException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    override suspend fun downloadAttachment(complexId: String, announcementId: String): Result<File> {
        if (complexId.isBlank() || announcementId.isBlank()) {
            return Result.failure(Exception(NewsConstants.ATTACH_DOWNLOAD_ERROR))
        }
        return try {
            withContext(Dispatchers.IO) {
                val target = attachmentTarget(announcementId)
                if (target.exists() && target.length() > 0) {
                    return@withContext Result.success(target)
                }
                val responseBody = api.downloadAttachment(complexId, announcementId)
                responseBody.use { body -> copyToFile(body, target) }
                Result.success(target)
            }
        } catch (e: HttpException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        } catch (e: IOException) {
            Result.failure(Exception(NetworkErrorMapper.map(e), e))
        }
    }

    @Suppress("SwallowedException")
    private suspend fun News.withImageAccess(complexId: String): News {
        val current = attachment ?: return this
        if (!current.contentType.orEmpty().startsWith("image/")) return this
        val url = try {
            api.getAnnouncementImageUrl(complexId, announcementId).url
        } catch (e: HttpException) {
            null
        } catch (e: IOException) {
            null
        }
        return if (url.isNullOrBlank()) this else copy(attachment = current.copy(imageUrl = url))
    }

    private fun attachmentTarget(announcementId: String): File {
        val dir = File(context.cacheDir, NewsConstants.ATTACHMENT_CACHE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "$announcementId.bin")
    }

    private fun copyToFile(body: ResponseBody, target: File) {
        target.outputStream().use { output ->
            body.byteStream().use { input ->
                input.copyTo(output)
            }
        }
    }

    private fun sanitizeFileName(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return if (cleaned.length > NewsConstants.ATTACHMENT_NAME_MAX_LENGTH) {
            cleaned.take(NewsConstants.ATTACHMENT_NAME_MAX_LENGTH)
        } else {
            cleaned.ifBlank { "attachment" }
        }
    }
}
