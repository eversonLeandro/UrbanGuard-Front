package co.edu.unicauca.urbanguard.ui.features.news

import android.content.Context
import co.edu.unicauca.urbanguard.core.session.SessionManager
import co.edu.unicauca.urbanguard.domain.model.ConjuntoUserAssociation
import co.edu.unicauca.urbanguard.domain.model.News
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.domain.model.UserSession
import co.edu.unicauca.urbanguard.domain.repository.NewsRepository
import co.edu.unicauca.urbanguard.domain.usecase.news.EditOrDeleteNewsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.news.GetNewsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.RequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.OffsetDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class NewsFeedViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNews shows list for resident`() = runTest {
        val sessionManager = SessionManager(FakeRolePersistence()).apply {
            saveSession(
                UserSession(
                    token = "t",
                    userId = "u1",
                    name = "User",
                    role = UserRole.RESIDENT,
                    conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "Conjunto", UserRole.RESIDENT)),
                    conjuntoActivoId = "c1"
                )
            )
        }
        val repo = FakeNewsRepository(listOf(sampleNews()))
        val viewModel = newsFeedViewModel(repo, sessionManager)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.newsList.size)
        assertFalse(state.isAdmin)
        assertNull(state.errorMessage)
    }

    @Test
    fun `confirmDelete removes announcement from list`() = runTest {
        val sessionManager = adminSession()
        val repo = FakeNewsRepository(listOf(sampleNews()))
        val viewModel = newsFeedViewModel(repo, sessionManager)
        advanceUntilIdle()
        viewModel.requestDelete(sampleNews())
        viewModel.confirmDelete()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.newsList.isEmpty())
        assertNull(state.deleteTarget)
        assertNull(state.deleteError)
        assertEquals("a1", repo.deletedId)
    }

    @Test
    fun `confirmDelete failure sets deleteError`() = runTest {
        val sessionManager = adminSession()
        val repo = FakeNewsRepository(listOf(sampleNews()), deleteError = RuntimeException("fallo"))
        val viewModel = newsFeedViewModel(repo, sessionManager)
        advanceUntilIdle()
        viewModel.requestDelete(sampleNews())
        viewModel.confirmDelete()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("fallo", state.deleteError)
        assertEquals(1, state.newsList.size)
        assertNull(state.deletingId)
    }

    private fun newsFeedViewModel(repo: NewsRepository, sessionManager: SessionManager): NewsFeedViewModel {
        return NewsFeedViewModel(
            GetNewsUseCase(repo),
            EditOrDeleteNewsUseCase(repo),
            repo,
            sessionManager,
            android.app.Application()
        )
    }

    private fun adminSession(): SessionManager {
        return SessionManager(FakeRolePersistence()).apply {
            saveSession(
                UserSession(
                    token = "t",
                    userId = "u1",
                    name = "User",
                    role = UserRole.ADMIN,
                    conjuntosAsociados = listOf(ConjuntoUserAssociation("c1", "Conjunto", UserRole.ADMIN)),
                    conjuntoActivoId = "c1"
                )
            )
        }
    }

    private fun sampleNews() = News(
        announcementId = "a1",
        title = "titulo",
        body = "cuerpo",
        publishedAt = OffsetDateTime.now(),
        attachment = null
    )

    private class FakeNewsRepository(
        private val list: List<News> = emptyList(),
        private val error: Throwable? = null,
        private val deleteError: Throwable? = null
    ) : NewsRepository {
        var deletedId: String? = null
            private set
        override suspend fun getNews(complexId: String): Result<List<News>> {
            error?.let { return Result.failure(it) }
            return Result.success(list)
        }
        override suspend fun publishNews(
            complexId: String,
            title: String,
            body: String,
            attachment: RequestBody?,
            attachmentName: String?
        ): Result<News> {
            error?.let { return Result.failure(it) }
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
            deleteError?.let { return Result.failure(it) }
            deletedId = announcementId
            return Result.success(Unit)
        }
        override suspend fun downloadAttachment(complexId: String, announcementId: String): Result<File> {
            return Result.failure(RuntimeException("no"))
        }
    }

    private class FakeRolePersistence : co.edu.unicauca.urbanguard.core.session.RolePersistence {
        private var stored: co.edu.unicauca.urbanguard.core.session.PersistedRole? = null
        override fun save(role: co.edu.unicauca.urbanguard.core.session.PersistedRole) { stored = role }
        override fun read(): co.edu.unicauca.urbanguard.core.session.PersistedRole? = stored
        override fun clear() { stored = null }
    }
}
