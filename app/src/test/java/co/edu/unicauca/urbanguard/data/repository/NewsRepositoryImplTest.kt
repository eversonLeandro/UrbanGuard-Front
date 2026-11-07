package co.edu.unicauca.urbanguard.data.repository

import android.content.Context
import co.edu.unicauca.urbanguard.core.network.NetworkErrorMapper
import co.edu.unicauca.urbanguard.data.remote.api.dto.AnnouncementImageAccessDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsAttachmentDto
import co.edu.unicauca.urbanguard.data.remote.api.dto.NewsDto
import co.edu.unicauca.urbanguard.data.remote.api.service.NewsApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class NewsRepositoryImplTest {

    @Test
    fun `getNews returns list mapped`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(listOf(sampleDto())), android.app.Application())
        val result = repo.getNews("complex-1")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `getNews enriches image attachment with temporary url`() = runTest {
        val image = NewsDto(
            announcementId = "img-1",
            title = "titulo",
            body = "cuerpo",
            publishedAt = "2026-10-27T10:00:00Z",
            attachment = NewsAttachmentDto(fileName = "foto.png", contentType = "image/png", sizeBytes = 42)
        )
        val repo = NewsRepositoryImpl(FakeNewsApi(listOf(image)), android.app.Application())
        val result = repo.getNews("complex-1")
        assertTrue(result.isSuccess)
        assertEquals("https://img.test/img-1", result.getOrNull()?.first()?.attachment?.imageUrl)
    }

    @Test
    fun `getNews fails without complexId`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(emptyList()), android.app.Application())
        val result = repo.getNews("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getNews maps http 403`() = runTest {
        val repo = NewsRepositoryImpl(
            FakeNewsApi(error = httpException(403, "No tienes permiso")),
            android.app.Application()
        )
        val result = repo.getNews("complex-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getNews maps http 401`() = runTest {
        val repo = NewsRepositoryImpl(
            FakeNewsApi(error = httpException(401, "Sesión expirada")),
            android.app.Application()
        )
        val result = repo.getNews("complex-1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `publishNews returns mapped news`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(listOf(sampleDto())), android.app.Application())
        val result = repo.publishNews("complex-1", "titulo", "cuerpo")
        assertTrue(result.isSuccess)
        assertEquals("titulo", result.getOrNull()?.title)
    }

    @Test
    fun `publishNews with attachment calls api`() = runTest {
        val api = FakeNewsApi(listOf(sampleDto()))
        val repo = NewsRepositoryImpl(api, android.app.Application())
        val result = repo.publishNews("complex-1", "titulo", "cuerpo", null, "file.pdf")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `publishNews maps http 400`() = runTest {
        val repo = NewsRepositoryImpl(
            FakeNewsApi(error = httpException(400, "Datos inválidos")),
            android.app.Application()
        )
        val result = repo.publishNews("complex-1", "titulo", "cuerpo")
        assertTrue(result.isFailure)
    }

    @Test
    fun `publishNews maps http 413`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(error = httpException(413, "Muy grande")), android.app.Application())
        val result = repo.publishNews("complex-1", "titulo", "cuerpo")
        assertTrue(result.isFailure)
    }

    @Test
    fun `publishNews maps SocketTimeoutException to timeout message`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(error = SocketTimeoutException("timeout")), android.app.Application())
        val result = repo.publishNews("complex-1", "titulo", "cuerpo")
        assertTrue(result.isFailure)
        assertEquals(NetworkErrorMapper.map(SocketTimeoutException("timeout")), result.exceptionOrNull()?.message)
    }

    @Test
    fun `publishNews maps other IOException to no connection message`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(error = IOException("broken pipe")), android.app.Application())
        val result = repo.publishNews("complex-1", "titulo", "cuerpo")
        assertTrue(result.isFailure)
        assertEquals(NetworkErrorMapper.map(IOException("broken pipe")), result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateNews returns mapped news`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(listOf(sampleDto())), android.app.Application())
        val result = repo.updateNews("complex-1", "a1", "titulo", "cuerpo")
        assertTrue(result.isSuccess)
        assertEquals("titulo", result.getOrNull()?.title)
    }

    @Test
    fun `updateNews fails without announcementId`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(listOf(sampleDto())), android.app.Application())
        val result = repo.updateNews("complex-1", "", "titulo", "cuerpo")
        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteNews succeeds`() = runTest {
        val repo = NewsRepositoryImpl(FakeNewsApi(listOf(sampleDto())), android.app.Application())
        val result = repo.deleteNews("complex-1", "a1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteNews maps http error`() = runTest {
        val repo = NewsRepositoryImpl(
            FakeNewsApi(error = httpException(403, "No tienes permiso")),
            android.app.Application()
        )
        val result = repo.deleteNews("complex-1", "a1")
        assertTrue(result.isFailure)
    }

    private fun sampleDto() = NewsDto(
        announcementId = "a1",
        title = "titulo",
        body = "cuerpo",
        publishedAt = "2026-10-27T10:00:00Z",
        attachment = NewsAttachmentDto(fileName = "file.pdf", contentType = "application/pdf", sizeBytes = 123)
    )

    private class FakeNewsApi(
        private val list: List<NewsDto> = emptyList(),
        private val error: Throwable? = null,
        private val published: NewsDto = NewsDto(
            announcementId = "a1",
            title = "titulo",
            body = "cuerpo",
            publishedAt = "2026-10-27T10:00:00Z",
            attachment = null
        )
    ) : NewsApi {
        override suspend fun getAnnouncements(complexId: String): List<NewsDto> {
            error?.let { throw it }
            return list
        }

        override suspend fun publishAnnouncement(
            complexId: String,
            metadata: RequestBody,
            attachment: MultipartBody.Part?
        ): NewsDto {
            error?.let { throw it }
            return published
        }

        override suspend fun updateAnnouncement(
            complexId: String,
            announcementId: String,
            metadata: RequestBody,
            attachment: MultipartBody.Part?,
            removeAttachment: Boolean
        ): NewsDto {
            error?.let { throw it }
            return published
        }

        override suspend fun deleteAnnouncement(complexId: String, announcementId: String) {
            error?.let { throw it }
        }

        override suspend fun getAnnouncementImageUrl(
            complexId: String,
            announcementId: String
        ): AnnouncementImageAccessDto {
            error?.let { throw it }
            return AnnouncementImageAccessDto("https://img.test/$announcementId", null)
        }

        override suspend fun downloadAttachment(complexId: String, announcementId: String): ResponseBody {
            error?.let { throw it }
            return "data".toResponseBody("application/pdf".toMediaType())
        }
    }

    private fun httpException(code: Int, message: String): HttpException {
        val json = """{"status":$code,"error":"Error","message":"$message","path":"/api/complexes/c1/announcements"}"""
        val body = json.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }
}
