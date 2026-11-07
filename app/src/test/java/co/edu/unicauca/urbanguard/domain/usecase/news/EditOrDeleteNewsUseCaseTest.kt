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

class EditOrDeleteNewsUseCaseTest {

    @Test
    fun `update fails when title is blank`() = runTest {
        val useCase = EditOrDeleteNewsUseCase(FakeNewsRepository())
        val result = useCase.update("complex-1", "a1", "   ", "body")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_TITLE_REQUIRED, result.exceptionOrNull()?.message)
    }

    @Test
    fun `update fails when body is blank`() = runTest {
        val useCase = EditOrDeleteNewsUseCase(FakeNewsRepository())
        val result = useCase.update("complex-1", "a1", "titulo", "   ")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_BODY_REQUIRED, result.exceptionOrNull()?.message)
    }

    @Test
    fun `update fails without announcementId`() = runTest {
        val useCase = EditOrDeleteNewsUseCase(FakeNewsRepository())
        val result = useCase.update("complex-1", "", "titulo", "body")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_ANNOUNCEMENT_REQUIRED, result.exceptionOrNull()?.message)
    }

    @Test
    fun `update succeeds with valid data`() = runTest {
        val repo = FakeNewsRepository()
        val useCase = EditOrDeleteNewsUseCase(repo)
        val result = useCase.update("complex-1", "a1", "titulo", "body", removeAttachment = true)
        assertTrue(result.isSuccess)
        assertEquals("titulo", result.getOrNull()?.title)
        assertTrue(repo.lastRemoveAttachment)
    }

    @Test
    fun `delete fails without announcementId`() = runTest {
        val useCase = EditOrDeleteNewsUseCase(FakeNewsRepository())
        val result = useCase.delete("complex-1", "")
        assertTrue(result.isFailure)
        assertEquals(NewsConstants.MSG_ANNOUNCEMENT_REQUIRED, result.exceptionOrNull()?.message)
    }

    @Test
    fun `delete removes announcement`() = runTest {
        val repo = FakeNewsRepository()
        val useCase = EditOrDeleteNewsUseCase(repo)
        val result = useCase.delete("complex-1", "a1")
        assertTrue(result.isSuccess)
        assertEquals("a1", repo.deletedId)
    }

    private class FakeNewsRepository : NewsRepository {
        var lastRemoveAttachment: Boolean = false
            private set
        var deletedId: String? = null
            private set

        override suspend fun getNews(complexId: String): Result<List<News>> = Result.success(emptyList())

        override suspend fun publishNews(
            complexId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?
        ): Result<News> = Result.success(sampleNews(title, body))

        override suspend fun updateNews(
            complexId: String,
            announcementId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?,
            removeAttachment: Boolean
        ): Result<News> {
            lastRemoveAttachment = removeAttachment
            return Result.success(sampleNews(title, body, announcementId))
        }

        override suspend fun deleteNews(complexId: String, announcementId: String): Result<Unit> {
            deletedId = announcementId
            return Result.success(Unit)
        }

        override suspend fun downloadAttachment(complexId: String, announcementId: String): Result<File> {
            return Result.failure(RuntimeException("no"))
        }

        private fun sampleNews(
            title: String,
            body: String,
            announcementId: String = "a1"
        ) = News(
            announcementId = announcementId,
            title = title,
            body = body,
            publishedAt = OffsetDateTime.now(),
            attachment = null
        )
    }
}
