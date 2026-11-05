package co.edu.unicauca.urbanguard.domain.usecase.news

import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.OffsetDateTime

class PublishNewsUseCaseTest {

    @Test
    fun `publish fails when title is blank`() = runTest {
        val useCase = PublishNewsUseCase(FakeNewsRepository())
        val result = useCase("complex-1", "   ", "body")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_TITLE_REQUIRED, result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish fails when body is blank`() = runTest {
        val useCase = PublishNewsUseCase(FakeNewsRepository())
        val result = useCase("complex-1", "titulo", "   ")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_BODY_REQUIRED, result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish fails when title too long`() = runTest {
        val useCase = PublishNewsUseCase(FakeNewsRepository())
        val longTitle = "a".repeat(NewsConstants.TITLE_MAX_LENGTH + 1)
        val result = useCase("complex-1", longTitle, "body")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_TITLE_TOO_LONG, result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish fails when body too long`() = runTest {
        val useCase = PublishNewsUseCase(FakeNewsRepository())
        val longBody = "b".repeat(NewsConstants.BODY_MAX_LENGTH + 1)
        val result = useCase("complex-1", "titulo", longBody)
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_BODY_TOO_LONG, result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish fails without active complex`() = runTest {
        val useCase = PublishNewsUseCase(FakeNewsRepository())
        val result = useCase("", "titulo", "body")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_NO_ACTIVE_COMPLEX, result.exceptionOrNull()?.message)
    }

    @Test
    fun `publish succeeds with valid data`() = runTest {
        val repo = FakeNewsRepository()
        val useCase = PublishNewsUseCase(repo)
        val result = useCase("complex-1", "titulo", "body")
        assertTrue(result.isSuccess)
        assertEquals("titulo", result.getOrNull()?.title)
    }

    private class FakeNewsRepository : NewsRepository {
        override suspend fun getNews(complexId: String): Result<List<News>> = Result.success(emptyList())
        override suspend fun publishNews(
            complexId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?
        ): Result<News> {
            return Result.success(
                News(
                    announcementId = "a1",
                    title = title,
                    body = body,
                    publishedAt = OffsetDateTime.now(),
                    attachment = null
                )
            )
        }
        override suspend fun updateNews(
            complexId: String,
            announcementId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?,
            removeAttachment: Boolean
        ): Result<News> {
            return Result.success(
                News(
                    announcementId = announcementId,
                    title = title,
                    body = body,
                    publishedAt = OffsetDateTime.now(),
                    attachment = null
                )
            )
        }
        override suspend fun deleteNews(complexId: String, announcementId: String): Result<Unit> {
            return Result.success(Unit)
        }
        override suspend fun downloadAttachment(complexId: String, announcementId: String): Result<File> {
            return Result.failure(RuntimeException("no"))
        }
    }
}
